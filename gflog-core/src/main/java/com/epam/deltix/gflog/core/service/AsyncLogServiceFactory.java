package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.core.Logger;
import com.epam.deltix.gflog.core.appender.Appender;
import com.epam.deltix.gflog.core.clock.Clock;
import com.epam.deltix.gflog.core.idle.IdleStrategy;
import com.epam.deltix.gflog.core.idle.IdleStrategyFactory;
import com.epam.deltix.gflog.core.metric.Counter;
import com.epam.deltix.gflog.core.metric.NoOpCounter;
import com.epam.deltix.gflog.core.util.PropertyUtil;

import java.util.concurrent.ThreadFactory;


public final class AsyncLogServiceFactory extends LogServiceFactory {

    protected static final int BUFFER_CAPACITY = PropertyUtil.getMemory("gflog.log.buffer.capacity", 8 * 1024 * 1024);

    protected ThreadFactory threadFactory;
    protected IdleStrategy idleStrategy;
    protected OverflowStrategy overflowStrategy;

    protected int bufferCapacity;

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

        if (bufferCapacity <= 0) {
            bufferCapacity = BUFFER_CAPACITY;
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
                threadFactory,
                idleStrategy,
                overflowStrategy,
                failedOffersCounter
        );
    }

}
