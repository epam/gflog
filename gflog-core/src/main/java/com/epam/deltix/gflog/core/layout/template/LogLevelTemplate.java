package com.epam.deltix.gflog.core.layout.template;

import com.epam.deltix.gflog.core.LogRecord;
import com.epam.deltix.gflog.core.util.Formatting;
import com.epam.deltix.gflog.core.util.MutableBuffer;


final class LogLevelTemplate extends Template {

    @Override
    public int size(final LogRecord record) {
        return record.getLogLevel().name().length();
    }

    @Override
    public int format(final LogRecord record, final MutableBuffer buffer, final int offset) {
        return Formatting.formatAsciiCharSequence(record.getLogLevel().name(), buffer, offset);
    }

}
