package com.epam.deltix.gflog.core.layout.template;

import com.epam.deltix.gflog.core.LogRecord;
import com.epam.deltix.gflog.core.util.Buffer;
import com.epam.deltix.gflog.core.util.Formatting;
import com.epam.deltix.gflog.core.util.MutableBuffer;


final class ShortLogNameTemplate extends Template {

    private final int precision;

    ShortLogNameTemplate(final int precision) {
        if (precision < 1) {
            throw new IllegalArgumentException("precision must be positive: " + precision);
        }

        this.precision = precision;
    }

    @Override
    public int size(final LogRecord record) {
        final Buffer name = record.getLogName();
        final int start = findStart(name, precision);

        return name.capacity() - start;
    }

    @Override
    public int format(final LogRecord record, final MutableBuffer buffer, final int offset) {
        final Buffer name = record.getLogName();
        final int start = findStart(name, precision);
        final int length = name.capacity() - start;

        return Formatting.formatBytes(name, start, length, buffer, offset);
    }

    private static int findStart(final Buffer sequence, final int dotsLimit) {
        int start = 0;

        for (int i = sequence.capacity() - 1, dots = 0; i >= 0; i--) {
            final byte c = sequence.getByte(i);

            if (c == Formatting.DOT && ++dots >= dotsLimit) {
                start = i + 1;
                break;
            }
        }

        return start;
    }

}
