package com.epam.deltix.gflog.core.layout;

import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.LogRecordBean;
import com.epam.deltix.gflog.core.util.MutableBuffer;
import com.epam.deltix.gflog.core.util.UnsafeBuffer;
import com.epam.deltix.gflog.core.util.Util;
import org.junit.Assert;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


public abstract class AbstractLayoutTest {

    protected static final DateTimeFormatter TIMESTAMP_NS_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSSSSSSSS");
    protected static final ZoneId UTC = ZoneId.of("UTC");

    protected final Layout layout;
    protected final MutableBuffer buffer = UnsafeBuffer.allocateHeap(1024);

    public AbstractLayoutTest(final Layout layout) {
        this.layout = layout;
    }

    protected void verify(final LogLevel level, final long timestamp, final String message, final String expected) {
        final LogRecordBean record = new LogRecordBean();
        record.setLogLevel(level);
        record.setTimestamp(timestamp);
        record.setMessage(new UnsafeBuffer(message.getBytes()));
        record.setLogName(Util.fromUtf8String("java.util.Object"));
        record.setThreadName(Util.fromUtf8String("main"));

        final int size = layout.format(record, buffer, 0);
        final byte[] actual = new byte[size];
        buffer.getBytes(0, actual);

        Assert.assertEquals(expected, new String(actual));
    }

    public static long timestamp(final String timestamp) {
        final Instant instant = LocalDateTime.parse(timestamp, TIMESTAMP_NS_FORMATTER)
                .atZone(UTC)
                .toInstant();

        return instant.getEpochSecond() * 1000000000 + instant.getNano();
    }

}
