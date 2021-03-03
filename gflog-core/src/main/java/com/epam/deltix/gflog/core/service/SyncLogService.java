package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.core.LogRecord;
import com.epam.deltix.gflog.core.Logger;
import com.epam.deltix.gflog.core.appender.Appender;
import com.epam.deltix.gflog.core.clock.Clock;
import com.epam.deltix.gflog.core.util.UnsafeBuffer;


final class SyncLogService extends LogService {

    private final UnsafeBuffer buffer = new UnsafeBuffer();

    private final LogRecordDecoder decoder;
    private final LogProcessor processor;

    private boolean closed;

    SyncLogService(final Logger[] loggers,
                   final Appender[] appenders,
                   final Clock clock,
                   final String entryTruncationSuffix,
                   final int entryInitialCapacity,
                   final int entryMaxCapacity,
                   final boolean entryUtf8) {
        super(loggers, appenders, clock, entryTruncationSuffix, entryInitialCapacity, entryMaxCapacity, entryUtf8);

        this.decoder = new LogRecordDecoder(logIndex);
        this.processor = new LogProcessor(appenders);
    }

    @Override
    public synchronized void open() {
        processor.open();
    }

    @Override
    public synchronized void close() {
        closed = true;
        processor.close();
    }

    @Override
    public synchronized void commit(final LogLocalEntry entry) {
        if (!closed) {
            entry.onCommit(clock.nanoTime());
            entry.wrapTo(buffer);

            final LogRecord record = decoder.decode(buffer, 0, buffer.capacity());

            processor.process(record);
            processor.flush();
        }
    }

}
