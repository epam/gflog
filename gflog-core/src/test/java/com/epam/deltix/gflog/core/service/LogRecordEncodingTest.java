package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.LogRecord;
import com.epam.deltix.gflog.core.util.MutableBuffer;
import com.epam.deltix.gflog.core.util.UnsafeBuffer;
import com.epam.deltix.gflog.core.util.Util;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;


public class LogRecordEncodingTest {

    private static final int OFFSET = 8;
    private static final int CAPACITY = 300;

    private final LogIndex index = new LogIndex();
    private final LogRecordDecoder decoder = new LogRecordDecoder(null, index, null);
    private final MutableBuffer buffer = UnsafeBuffer.allocateDirect(CAPACITY + OFFSET);

    @Test
    public void testSimple() {
        verify("thread #1", "log #1", "message #1");
    }

    @Test
    public void testRandom() {
        for (int i = 0; i < 1000; i++) {
            verify("" + randomLong(), "" + randomLong(), "" + randomLong());
        }
    }

    private void verify(final String threadName,
                        final String logName,
                        final String message) {

        final int logIndex = randomInt(1000);
        index.put(logName, logIndex);

        final UnsafeBuffer thread = Util.fromUtf8String(threadName);
        final byte[] text = message.getBytes(StandardCharsets.UTF_8);
        final LogLevel level = randomLogLevel();
        final long timestamp = randomLong();
        final long appenders = randomLong();

        final int size = LogRecordEncoder.size(thread, text.length);

        LogRecordEncoder.encode(
                timestamp,
                appenders,
                logIndex,
                level.ordinal(),
                thread,
                text,
                text.length,
                buffer.address() + OFFSET
        );

        final LogRecord actual = decoder.decode(buffer, OFFSET, size);

        Assert.assertEquals(threadName, Util.toUtf8String(actual.getThreadName()));
        Assert.assertEquals(logName, Util.toUtf8String(actual.getLogName()));
        Assert.assertEquals(level, actual.getLogLevel());
        Assert.assertEquals(timestamp, actual.getTimestamp());
        Assert.assertEquals(appenders, actual.getAppenderMask());
        Assert.assertEquals(message, Util.toUtf8String(actual.getMessage()));
    }

    private static long randomLong() {
        return ThreadLocalRandom.current().nextLong();
    }

    private static int randomInt(final int bound) {
        return ThreadLocalRandom.current().nextInt(0, bound);
    }

    private static LogLevel randomLogLevel() {
        final LogLevel[] constants = LogLevel.class.getEnumConstants();
        final int index = ThreadLocalRandom.current().nextInt(0, constants.length);
        return constants[index];
    }

}
