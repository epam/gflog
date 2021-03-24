package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.core.LogRecord;
import com.epam.deltix.gflog.core.appender.Appender;
import com.epam.deltix.gflog.core.util.Buffer;


final class AsyncLogProcessor extends LogProcessor implements LogBuffer.RecordHandler {

    private final LogBuffer buffer;
    private final LogRecordDecoder decoder;

    private volatile boolean active = true;

    AsyncLogProcessor(final LogBuffer buffer, final LogRecordDecoder decoder, final Appender[] appenders) {
        super(appenders);

        this.buffer = buffer;
        this.decoder = decoder;
    }

    @Override
    public void close() {
        buffer.unblock();
        super.close();
    }

    public boolean active() {
        return active || !buffer.isEmpty();
    }

    public void deactivate() {
        active = false;
    }

    public int work() {
        int work = buffer.read(this);

        if (work == 0) {
            work = flush();
        }

        return work;
    }

    @Override
    public void onRecord(final Buffer buffer, final int offset, final int length) {
        final LogRecord record = decoder.decode(buffer, offset, length);
        process(record);
    }

}
