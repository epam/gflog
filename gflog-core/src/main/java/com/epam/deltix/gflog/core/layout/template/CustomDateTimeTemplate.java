package com.epam.deltix.gflog.core.layout.template;

import com.epam.deltix.gflog.core.LogRecord;
import com.epam.deltix.gflog.core.util.MutableBuffer;

import java.nio.ByteBuffer;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;


final class CustomDateTimeTemplate extends Template {

    private final FastDateFormat format;
    private final ByteBuffer byteBuffer;

    private long timestampLast = Long.MIN_VALUE;

    CustomDateTimeTemplate(final String template, final ZoneId zoneId) {
        this.format = FastDateFormat.getInstance(template, TimeZone.getTimeZone(zoneId), Locale.ENGLISH);
        this.byteBuffer = ByteBuffer.allocate(format.getMaxLengthEstimate());
    }

    @Override
    public int size(final LogRecord record) {
        return format.getMaxLengthEstimate();
    }

    @Override
    public int format(final LogRecord record, final MutableBuffer buffer, final int offset) {
        final long timestamp = record.getTimestamp();

        if (timestampLast != timestamp) {
            byteBuffer.clear();
            format.formatNanos(timestamp, byteBuffer);
            timestampLast = timestamp;
        }

        final int length = byteBuffer.position();
        buffer.putBytes(offset, byteBuffer, 0, length);

        return offset + length;
    }

}
