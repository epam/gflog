package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.core.Logger;
import com.epam.deltix.gflog.core.appender.Appender;
import com.epam.deltix.gflog.core.clock.Clock;
import com.epam.deltix.gflog.core.idle.IdleStrategy;
import com.epam.deltix.gflog.core.idle.IdleStrategyFactory;
import com.epam.deltix.gflog.core.metric.Counter;
import com.epam.deltix.gflog.core.metric.NoOpCounter;
import com.epam.deltix.gflog.core.util.Util;

import java.util.concurrent.ThreadFactory;

import static com.epam.deltix.gflog.core.util.PropertyUtil.getMemory;


public final class AsyncLogServiceFactory extends LogServiceFactory {

    protected static final int BUFFER_DEFAULT_CAPACITY = getMemory("gflog.log.buffer.capacity", 8 * 1024 * 1024);
    protected static final int EXCEPTION_INDEX_DEFAULT_CAPACITY = getMemory("gflog.exception.index.capacity", 4 * 1024);

    protected ThreadFactory threadFactory;
    protected IdleStrategy idleStrategy;
    protected OverflowStrategy overflowStrategy;

    protected int bufferCapacity = BUFFER_DEFAULT_CAPACITY;
    protected int exceptionIndexCapacity = EXCEPTION_INDEX_DEFAULT_CAPACITY;

    protected Counter failedOffersCounter;

    public void setThreadFactory(final ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    public void setIdleStrategy(final IdleStrategy idleStrategy) {
        this.idleStrategy = idleStrategy;
    }

    public IdleStrategy getIdleStrategy() {
        return idleStrategy;
    }

    public void setOverflowStrategy(final OverflowStrategy overflowStrategy) {
        this.overflowStrategy = overflowStrategy;
    }

    public OverflowStrategy getOverflowStrategy() {
        return overflowStrategy;
    }

    public void setBufferCapacity(final int bufferCapacity) {
        this.bufferCapacity = bufferCapacity;
    }

    public int getBufferCapacity() {
        return bufferCapacity;
    }

    public void setFailedOffersCounter(final Counter failedOffersCounter) {
        this.failedOffersCounter = failedOffersCounter;
    }

    public Counter getFailedOffersCounter() {
        return failedOffersCounter;
    }

    @Override
    protected void conclude() {
        super.conclude();

        if (threadFactory == null) {
            threadFactory = r -> new Thread(r, "gflog");
        }

        if (idleStrategy == null) {
            idleStrategy = new IdleStrategyFactory().create();
        }

        if (overflowStrategy == null) {
            overflowStrategy = OverflowStrategy.WAIT;
        }

        if (bufferCapacity <= LogBuffer.MIN_CAPACITY) {
            bufferCapacity = LogBuffer.MIN_CAPACITY;
        }

        if (bufferCapacity >= LogBuffer.MAX_CAPACITY) {
            bufferCapacity = LogBuffer.MAX_CAPACITY;
        }

        bufferCapacity = Util.nextPowerOfTwo(bufferCapacity);

        if (exceptionIndexCapacity > 0) {
            final int maxCapacity = bufferCapacity / ExceptionIndex.MIN_SEGMENT;
            final int minCapacity = ExceptionIndex.MIN_CAPACITY;

            if (exceptionIndexCapacity < minCapacity) {
                exceptionIndexCapacity = minCapacity;
            }

            if (exceptionIndexCapacity > maxCapacity) {
                exceptionIndexCapacity = maxCapacity;
            }

            exceptionIndexCapacity = Util.nextPowerOfTwo(exceptionIndexCapacity);
        }

        if (failedOffersCounter == null) {
            failedOffersCounter = NoOpCounter.INSTANCE;
        }
    }

    @Override
    protected LogService createService(final Logger[] loggers,
                                       final Appender[] appenders,
                                       final Clock clock,
                                       final String entryTruncationSuffix,
                                       final int entryInitialCapacity,
                                       final int entryMaxCapacity,
                                       final boolean entryUtf8) {

        final LogBuffer buffer = new LogBuffer(bufferCapacity);
        final ExceptionIndex index = (exceptionIndexCapacity > 0) ?
                new ExceptionIndex(exceptionIndexCapacity, bufferCapacity) :
                null;

        final int messageMaxCapacity = buffer.maxRecordLength() - LogRecordEncoder.MIN_SIZE - entryTruncationSuffix.length();
        final int effectiveEntryMaxCapacity = Math.min(entryMaxCapacity, messageMaxCapacity);
        final int effectiveEntryInitialCapacity = Math.min(entryInitialCapacity, effectiveEntryMaxCapacity);

        return new AsyncLogService(
                loggers,
                appenders,
                clock,
                entryTruncationSuffix,
                effectiveEntryInitialCapacity,
                effectiveEntryMaxCapacity,
                entryUtf8,
                buffer,
                index,
                threadFactory,
                idleStrategy,
                overflowStrategy,
                failedOffersCounter
        );
    }

}
