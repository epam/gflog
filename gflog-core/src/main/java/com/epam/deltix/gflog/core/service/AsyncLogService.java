package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.api.LogDebug;
import com.epam.deltix.gflog.core.Logger;
import com.epam.deltix.gflog.core.appender.Appender;
import com.epam.deltix.gflog.core.clock.Clock;
import com.epam.deltix.gflog.core.idle.IdleStrategy;
import com.epam.deltix.gflog.core.metric.Counter;

import java.util.concurrent.ThreadFactory;


final class AsyncLogService extends LogService {

    protected final LogBuffer buffer;
    protected final OverflowStrategy strategy;
    protected final Counter failedOffersCounter;
    protected final LogProcessorRunner runner;

    AsyncLogService(final Logger[] loggers,
                    final Appender[] appenders,
                    final Clock clock,
                    final String entryTruncationSuffix,
                    final int entryInitialCapacity,
                    final int entryMaxCapacity,
                    final boolean entryUtf8,
                    final LogBuffer buffer,
                    final ThreadFactory threadFactory,
                    final IdleStrategy idleStrategy,
                    final OverflowStrategy overflowStrategy,
                    final Counter failedOffersCounter) {
        super(loggers, appenders, clock, entryTruncationSuffix, entryInitialCapacity, entryMaxCapacity, entryUtf8);

        final AsyncLogProcessor handler = new AsyncLogProcessor(buffer, logIndex, appenders);

        this.buffer = buffer;
        this.strategy = overflowStrategy;
        this.failedOffersCounter = failedOffersCounter;
        this.runner = new LogProcessorRunner(handler, threadFactory, idleStrategy);
    }

    @Override
    public void open() {
        runner.open();
    }

    @Override
    public void close() {
        closed = true;
        runner.close();
    }

    @Override
    public void commit(final LogLocalEntry entry) {
        final LogBuffer buffer = this.buffer;
        final int required = entry.length();

        int offset;

        while ((offset = buffer.claim(required)) < 0) {
            failedOffersCounter.increment();

            if (strategy == OverflowStrategy.DISCARD || closed) {
                return;
            }

            Thread.yield();
        }

        try {
            entry.onCommit(clock.nanoTime());
            entry.copyTo(buffer.dataAddress() + offset);

            buffer.commit(offset, required);
        } catch (final Throwable e) {
            LogDebug.warn("error committing log entry to log buffer", e);
            buffer.abort(offset, required);
        }
    }

}
