package com.epam.deltix.gflog.core.layout.template;

import com.epam.deltix.gflog.core.LogRecord;
import com.epam.deltix.gflog.core.util.Formatting;
import com.epam.deltix.gflog.core.util.MutableBuffer;


final class ThreadNameTemplate extends Template {

    @Override
    public int size(final LogRecord record) {
        return record.getThreadName().capacity();
    }

    @Override
    public int format(final LogRecord record, final MutableBuffer buffer, final int offset) {
        return Formatting.formatBytes(record.getThreadName(), buffer, offset);
    }

}
