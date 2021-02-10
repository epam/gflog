package com.epam.deltix.gflog.core.layout.template;

import com.epam.deltix.gflog.core.LogRecordBean;
import com.epam.deltix.gflog.core.util.MutableBuffer;
import com.epam.deltix.gflog.core.util.UnsafeBuffer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


@RunWith(Parameterized.class)
public class DefaultDateTimeTemplateTest {

    protected static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSSSSSSSS");

    private final ZoneId zoneId;
    private final DefaultDateTimeTemplate template;
    private final LogRecordBean record;
    private final MutableBuffer buffer;

    public DefaultDateTimeTemplateTest(final String zone) {
        zoneId = ZoneId.of(zone);
        template = new DefaultDateTimeTemplate("yyyy-MM-dd HH:mm:ss.SSSSSSSSS", zoneId);
        record = new LogRecordBean();
        buffer = UnsafeBuffer.allocateHeap(29);
    }

    @Parameterized.Parameters(name = "zoneId={0}")
    public static Collection<?> parameters() {
        return ZoneId.getAvailableZoneIds();
    }

    @Test
    public void testSimple() {
        valid(timestamp("1970-01-01 00:00:00.000000000"));
        valid(timestamp("2000-01-01 00:00:00.000000000"));
        valid(timestamp("2020-01-01 00:00:00.000000000"));
        valid(timestamp("2100-01-01 00:00:00.000000000") - 1);
        valid(TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()));

        invalid(timestamp("1970-01-01 00:00:00.000000000") - 1);
        invalid(timestamp("2100-01-01 00:00:00.000000000"));
    }

    @Test
    public void testRandom() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();

        final long begin = timestamp("1970-01-01 00:00:00.000000000");
        final long end = timestamp("2100-01-01 00:00:00.000000000");

        for (int i = 0; i < 10000; i++) {
            final long timestamp = random.nextLong(begin, end);
            valid(timestamp);
        }
    }

    @Test
    public void testClockShift() {
        final ZoneRules rules = zoneId.getRules();

        if (!rules.isFixedOffset()) {
            final long begin = timestamp("1970-01-01 00:00:00.000000000");
            final long end = timestamp("2100-01-01 00:00:00.000000000");

            long timestamp = begin;

            while (true) {
                final Instant instant = Instant.ofEpochSecond(0, timestamp);
                final ZoneOffsetTransition transition = rules.nextTransition(instant);

                if (transition == null) {
                    break;
                }

                timestamp = TimeUnit.SECONDS.toNanos(transition.getInstant().getEpochSecond());

                if (timestamp >= end) {
                    break;
                }

                valid(timestamp - 1);
                valid(timestamp);
                valid(timestamp + 1);
            }
        }
    }

    private void valid(final long timestamp) {
        record.setTimestamp(timestamp);

        final int size = template.size(record);
        final int offset = template.format(record, buffer, 0);

        Assert.assertEquals(size, offset);

        final byte[] array = new byte[size];
        buffer.getBytes(0, array);

        final String expected = Instant.ofEpochSecond(0, timestamp).atZone(zoneId).format(FORMATTER);
        final String actual = new String(array);

        Assert.assertEquals(expected, actual);
    }

    private void invalid(final long timestamp) {
        try {
            valid(timestamp);
        } catch (final IllegalArgumentException e) {
            // skipp
        }
    }

    private long timestamp(final String timestamp) {
        final Instant instant = LocalDateTime.parse(timestamp, FORMATTER)
                .atZone(zoneId)
                .toInstant();

        return TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano();
    }

}
