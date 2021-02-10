package com.epam.deltix.gflog.core.layout;

import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.LogRecord;
import com.epam.deltix.gflog.core.util.Buffer;
import com.epam.deltix.gflog.core.util.Formatting;
import com.epam.deltix.gflog.core.util.MutableBuffer;
import com.epam.deltix.gflog.core.util.UnsafeBuffer;

import java.util.Map;


public class GelfLayout extends Layout {

    protected static final String HEAD = "{" +
            "\"version\":\"1.1\"," +
            "\"level\":*," +
            "\"host\":\"";

    protected static final String MESSAGE_FIELD = "\",\"short_message\":\"";
    protected static final String LOGGER_FIELD = "\",\"_logger\":\"";
    protected static final String THREAD_FIELD = "\",\"_thread\":\"";
    protected static final String SEQUENCE_FIELD = "\",\"_sequence\":";
    protected static final String TIMESTAMP_FIELD = ",\"timestamp\":";
    protected static final String TAIL = "}\u0000";

    protected static final int LEVEL_INDEX = HEAD.indexOf('*');

    protected final MutableBuffer head;
    protected final int lengthBase;

    protected long sequenceNext;

    public GelfLayout(final String host, final Map<String, String> additionalFields, final long sequenceBase) {
        head = buildHead(host, additionalFields);
        lengthBase = computeLengthBase(head);
        sequenceNext = sequenceBase;
    }

    @Override
    public int size(final LogRecord record) {
        // the worst case
        return lengthBase + 2 * (record.getMessage().capacity() + record.getLogName().capacity() + record.getThreadName().capacity());
    }

    @Override
    public int format(final LogRecord record, final MutableBuffer buffer, int offset) {
        offset = formatHead(record, buffer, offset);
        offset = formatMessage(record, buffer, offset);
        offset = formatLogger(record, buffer, offset);
        offset = formatThread(record, buffer, offset);
        offset = formatSequence(record, buffer, offset);
        offset = formatTimestamp(record, buffer, offset);
        offset = formatTail(record, buffer, offset);
        return offset;
    }

    protected int formatHead(final LogRecord record, final MutableBuffer buffer, final int offset) {
        final LogLevel level = record.getLogLevel();
        head.putByte(LEVEL_INDEX, getSyslogLevel(level));

        return Formatting.formatBytes(head, 0, head.capacity(), buffer, offset);
    }

    protected int formatMessage(final LogRecord record, final MutableBuffer buffer, final int offset) {
        return formatWithEscape(record.getMessage(), buffer, offset);
    }

    protected int formatLogger(final LogRecord record, final MutableBuffer buffer, int offset) {
        offset = Formatting.formatAsciiCharSequence(LOGGER_FIELD, buffer, offset);
        offset = formatWithEscape(record.getLogName(), buffer, offset);

        return offset;
    }

    protected int formatThread(final LogRecord record, final MutableBuffer buffer, int offset) {
        offset = Formatting.formatAsciiCharSequence(THREAD_FIELD, buffer, offset);
        offset = formatWithEscape(record.getThreadName(), buffer, offset);

        return offset;
    }

    protected int formatSequence(final LogRecord record, final MutableBuffer buffer, int offset) {
        offset = Formatting.formatAsciiCharSequence(SEQUENCE_FIELD, buffer, offset);
        offset = Formatting.formatLong(sequenceNext++, buffer, offset);

        return offset;
    }

    protected int formatTimestamp(final LogRecord record, final MutableBuffer buffer, int offset) {
        offset = Formatting.formatAsciiCharSequence(TIMESTAMP_FIELD, buffer, offset);

        long time = record.getTimestamp();

        if (time < 0) {
            time = Math.max(time, Long.MIN_VALUE + 1);
            time = -time;

            offset = Formatting.formatByte((byte) '-', buffer, offset);
        }

        final long seconds = time / 1000_000_000;
        final long nanos = time % 1000_000_000;

        offset = Formatting.formatLong(seconds, buffer, offset);
        offset = Formatting.formatByte((byte) '.', buffer, offset);
        offset = Formatting.formatUInt9Digits((int) nanos, buffer, offset);

        return offset;
    }

    protected int formatTail(final LogRecord record, final MutableBuffer buffer, final int offset) {
        return Formatting.formatAsciiCharSequence(TAIL, buffer, offset);
    }

    protected static int formatWithEscape(final Buffer value, final MutableBuffer buffer, int offset) {
        final int length = value.capacity();

        int i, j;

        for (i = 0, j = 0; i < length; i++) {
            final byte b = value.getByte(i);
            final byte e = escape(b);

            if (e != 0) {
                offset = Formatting.formatBytes(value, j, i - j, buffer, offset);

                buffer.putByte(offset++, (byte) '\\');
                buffer.putByte(offset++, e);

                j = i + 1;
            }
        }

        return Formatting.formatBytes(value, j, i - j, buffer, offset);
    }

    protected static byte getSyslogLevel(final LogLevel level) {
        switch (level) {
            case TRACE:
            case DEBUG:
                return '7'; // debug

            case INFO:
                return '6'; // info

            case WARN:
                return '4'; // warn

            case ERROR:
                return '3'; // error
            case FATAL:
                return '2'; // critical
            default:
                return '0'; // should not happen
        }
    }

    protected static byte escape(final byte b) {
        switch (b) {
            case '\b':
                return 'b';

            case '\f':
                return 'f';

            case '\n':
                return 'n';

            case '\r':
                return 'r';

            case '\t':
                return 't';

            case '"':
            case '\\':
            case '/':
                return b;

            default:
                return 0;
        }
    }

    protected static String escape(final String value) {
        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            final byte b = (byte) value.charAt(i);
            final byte e = escape(b);

            if (e != 0) {
                builder.append('\\');
            }

            builder.append((char) (e == 0 ? b : e));
        }

        return builder.toString();
    }

    protected static MutableBuffer buildHead(final String host, final Map<String, String> additionalFields) {
        final StringBuilder builder = new StringBuilder();
        builder.append(HEAD).append(escape(host));

        additionalFields.forEach((key, value) -> {
            builder.append("\",\"_")
                    .append(escape(key))
                    .append("\":\"")
                    .append(escape(value));
        });

        builder.append(MESSAGE_FIELD);

        final String head = builder.toString();
        return new UnsafeBuffer(head.getBytes());
    }

    protected int computeLengthBase(final Buffer head) {
        return head.capacity() +
                MESSAGE_FIELD.length() +
                LOGGER_FIELD.length() +
                THREAD_FIELD.length() +
                SEQUENCE_FIELD.length() +
                TIMESTAMP_FIELD.length() +
                TAIL.length() +
                20 +  // sequence
                21;   // timestamp
    }

}
