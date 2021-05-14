package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.api.LogDebug;
import com.epam.deltix.gflog.core.Logger;
import com.epam.deltix.gflog.core.appender.Appender;
import com.epam.deltix.gflog.core.clock.Clock;
import com.epam.deltix.gflog.core.idle.IdleStrategy;
import com.epam.deltix.gflog.core.metric.Counter;
import com.epam.deltix.gflog.core.service.LogBuffer.BackpressureCallback;
import com.epam.deltix.gflog.core.util.Util;

import java.util.concurrent.ThreadFactory;

import static com.epam.deltix.gflog.core.util.Util.SIZE_OF_LONG;
import static com.epam.deltix.gflog.core.util.Util.UNSAFE;


final class AsyncLogService extends LogService {

    private final BackpressureCallback backpressure = this::onBackpressure;
    private final LogBuffer buffer;
    private final OverflowStrategy strategy;
    private final Counter failedOffersCounter;
    private final LogProcessorRunner runner;
    private final ExceptionIndex exceptionIndex;

    AsyncLogService(final Logger[] loggers,
                    final Appender[] appenders,
                    final Clock clock,
                    final String entryTruncationSuffix,
                    final int entryInitialCapacity,
                    final int entryMaxCapacity,
                    final boolean entryUtf8,
                    final LogBuffer buffer,
                    final ExceptionIndex exceptionIndex,
                    final ThreadFactory threadFactory,
                    final IdleStrategy idleStrategy,
                    final OverflowStrategy overflowStrategy,
                    final Counter failedOffersCounter) {
        super(loggers, appenders, clock, entryTruncationSuffix, entryInitialCapacity, entryMaxCapacity, entryUtf8, exceptionIndex != null);

        final LogLimitedEntry entry = (exceptionIndex == null) ?
                null : entryUtf8 ?
                new LogUtf8Entry(entryTruncationSuffix, entryInitialCapacity, entryMaxCapacity) :
                new LogAsciiEntry(entryTruncationSuffix, entryInitialCapacity, entryMaxCapacity);

        final LogRecordDecoder decoder = new LogRecordDecoder(entry, logIndex, exceptionIndex);
        final AsyncLogProcessor handler = new AsyncLogProcessor(buffer, decoder, appenders);

        this.buffer = buffer;
        this.strategy = overflowStrategy;
        this.failedOffersCounter = failedOffersCounter;
        this.exceptionIndex = exceptionIndex;
        this.runner = new LogProcessorRunner(handler, threadFactory, idleStrategy);
    }

    @Override
    public void open() {
        runner.open();
    }

    @Override
    public void close() {
        try {
            runner.close();
        } finally {
            logEntry.remove();
        }
    }

    @Override
    public void commit(final LogLocalEntry entry) {
        final int required = entry.length();
        final int offset;

        if (strategy == OverflowStrategy.WAIT) {
            offset = buffer.claim(required, backpressure);
        } else {
            offset = buffer.tryClaim(required);

            if (offset < 0) {
                failedOffersCounter.increment();
                return;
            }
        }

        try {
            final long timestamp = clock.nanoTime();
            final long address = buffer.dataAddress() + offset;

            entry.onCommit(timestamp);
            entry.copyTo(address);

            buffer.commit(offset, required);
        } catch (final Throwable e) {
            LogDebug.warn("error committing log entry to log buffer", e);
            buffer.abort(offset, required);
        }
    }

    @Override
    void commit(final LogLocalEntry entry, final Throwable exception, final int exceptionPosition) {
        final int length = entry.length();
        final int min = exceptionIndex.segment();
        final int max = Math.max(length + SIZE_OF_LONG, min);

        final int required = Util.align(max, SIZE_OF_LONG);
        final int offset;

        if (strategy == OverflowStrategy.WAIT) {
            offset = buffer.claim(required, backpressure);
        } else {
            offset = buffer.tryClaim(required);

            if (offset < 0) {
                failedOffersCounter.increment();
                return;
            }
        }

        try {
            final long timestamp = clock.nanoTime();
            final long address = buffer.dataAddress() + offset;

            entry.onCommit(timestamp);
            entry.copyTo(address);

            final byte logLevel = UNSAFE.getByte(address + LogRecordEncoder.LOG_LEVEL_OFFSET);
            UNSAFE.putByte(address + LogRecordEncoder.LOG_LEVEL_OFFSET, (byte) ~logLevel);

            UNSAFE.putInt(address + required - LogRecordEncoder.EXCEPTION_POSITION_OFFSET, exceptionPosition);
            UNSAFE.putInt(address + required - LogRecordEncoder.EXCEPTION_REAL_LENGTH_OFFSET, length);

            exceptionIndex.put(offset, exception);
            buffer.commit(offset, required);
        } catch (final Throwable e) {
            LogDebug.warn("error committing log entry to log buffer", e);
            buffer.abort(offset, required);
        }
    }

    private void onBackpressure() {
        try {
            failedOffersCounter.increment();
            Thread.yield();
        } catch (final Throwable e) {
            LogDebug.warn("error on backpressure callback", e);
        }
    }

}
