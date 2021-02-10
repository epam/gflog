package com.epam.deltix.gflog.core.layout.template;

import com.epam.deltix.gflog.core.LogRecord;
import com.epam.deltix.gflog.core.util.Formatting;
import com.epam.deltix.gflog.core.util.MutableBuffer;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.concurrent.TimeUnit;


final class DefaultDateTimeTemplate extends Template {

    private static final String TEMPLATE_PREFIX = "yyyy-MM-dd HH:mm:ss.";
    private static final String TEMPLATE_DEFAULT = "yyyy-MM-dd HH:mm:ss.SSS";

    private static final int MAX_SIZE = TEMPLATE_PREFIX.length() + 9;

    private final byte[] array = new byte[MAX_SIZE];
    private final int size;
    private final ZoneRules rules;

    private long dayHigh = Long.MIN_VALUE;
    private long dayLow = Long.MIN_VALUE;

    private long offsetLow = Long.MIN_VALUE;
    private long offsetHigh = Long.MIN_VALUE;

    private long offset;
    private long previous = Long.MIN_VALUE;

    DefaultDateTimeTemplate(final String template, final ZoneId zoneId) {
        verify(template);

        this.size = (template == null) ? TEMPLATE_DEFAULT.length() : template.length();
        this.rules = zoneId.getRules();

        Formatting.formatByte(Formatting.SPACE, array, 10);
        Formatting.formatByte(Formatting.COLON, array, 13);
        Formatting.formatByte(Formatting.COLON, array, 16);
        Formatting.formatByte(Formatting.DOT, array, 19);

        formatDateTime(TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()));
    }

    @Override
    public int size(final LogRecord record) {
        return size;
    }

    @Override
    public int format(final LogRecord record, final MutableBuffer buffer, final int offset) {
        final long timestamp = record.getTimestamp();

        if (timestamp != previous) {
            formatDateTime(timestamp);
            previous = timestamp;
        }

        return Formatting.formatBytes(array, 0, size, buffer, offset);
    }

    private void formatDateTime(long timestamp) {
        if (timestamp < offsetLow || timestamp >= offsetHigh) {
            adjustOffset(timestamp);
        }

        timestamp += offset;

        if (timestamp < dayLow || timestamp >= dayHigh) {
            Formatting.verifyTimestampNs(timestamp);
            adjustDate(timestamp);
        }

        formatTime(timestamp);
    }

    private void adjustOffset(final long timestamp) {
        final Instant instant = Instant.ofEpochSecond(0, timestamp);

        if (rules.isFixedOffset()) {
            offsetLow = Long.MIN_VALUE;
            offsetHigh = Long.MAX_VALUE;
        } else {
            final ZoneOffsetTransition previous = rules.previousTransition(instant);
            final ZoneOffsetTransition next = rules.nextTransition(instant);

            offsetLow = (previous == null) ? Long.MIN_VALUE : toNanos(previous.getInstant());
            offsetHigh = (next == null) ? Long.MAX_VALUE : toNanos(next.getInstant());
        }

        offset = toNanos(rules.getOffset(instant));
    }

    private void adjustDate(final long timestamp) {
        Formatting.formatDateNs(timestamp, array, 0);

        dayLow = timestamp - timestamp % Formatting.DAY_NS;
        dayHigh = dayLow + Formatting.DAY_NS;
    }

    private void formatTime(final long timestamp) {
        long ns = timestamp % Formatting.DAY_NS;

        final long hour = ns / Formatting.HOUR_NS;
        ns -= hour * Formatting.HOUR_NS;

        final long minute = ns / Formatting.MINUTE_NS;
        ns -= minute * Formatting.MINUTE_NS;

        final long second = ns / Formatting.SECOND_NS;
        ns -= second * Formatting.SECOND_NS;

        Formatting.formatUInt2Digits((int) hour, array, 11);
        Formatting.formatUInt2Digits((int) minute, array, 14);
        Formatting.formatUInt2Digits((int) second, array, 17);
        Formatting.formatUInt9Digits((int) ns, array, 20);
    }

    public static boolean matches(final String template) {
        if (template == null) {
            return true;
        }

        if (template.startsWith(TEMPLATE_PREFIX) && template.length() <= TEMPLATE_PREFIX.length() + 9) {
            for (int i = TEMPLATE_PREFIX.length(), end = template.length(); i < end; i++) {
                if (template.charAt(i) != 'S') {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    private static void verify(final String template) {
        if (!matches(template)) {
            throw new IllegalArgumentException("Unsupported template: " + template);
        }
    }

    private static long toNanos(final Instant instant) {
        final long seconds = TimeUnit.SECONDS.toNanos(instant.getEpochSecond());
        final int nanoseconds = instant.getNano();

        if (seconds == Long.MIN_VALUE) {
            return Long.MIN_VALUE;
        }

        if (seconds > Long.MAX_VALUE - nanoseconds) {
            return Long.MAX_VALUE;
        }

        return seconds + nanoseconds;
    }

    private static long toNanos(final ZoneOffset offset) {
        return TimeUnit.SECONDS.toNanos(offset.getTotalSeconds());
    }

}
