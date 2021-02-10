package com.epam.deltix.gflog.core.layout.template;

import com.epam.deltix.gflog.core.LogRecord;
import com.epam.deltix.gflog.core.util.Buffer;
import com.epam.deltix.gflog.core.util.Formatting;
import com.epam.deltix.gflog.core.util.MutableBuffer;


final class MessageTemplate extends Template {

    @Override
    public int size(final LogRecord record) {
        return record.getMessage().capacity();
    }

    @Override
    public int format(final LogRecord record, final MutableBuffer buffer, final int offset) {
        final Buffer message = record.getMessage();
        return Formatting.formatBytes(message, 0, message.capacity(), buffer, offset);
    }

}
