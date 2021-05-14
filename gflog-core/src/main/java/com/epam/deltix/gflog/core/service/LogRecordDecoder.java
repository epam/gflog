package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.LogRecord;
import com.epam.deltix.gflog.core.LogRecordBean;
import com.epam.deltix.gflog.core.util.Buffer;
import com.epam.deltix.gflog.core.util.UnsafeBuffer;


final class LogRecordDecoder {

    private final LogRecordBean record = new LogRecordBean();

    private final UnsafeBuffer threadName = new UnsafeBuffer();
    private final UnsafeBuffer message = new UnsafeBuffer();

    private final LogLimitedEntry entry;
    private final LogIndex logIndex;
    private final ExceptionIndex exceptionIndex;

    LogRecordDecoder(final LogLimitedEntry entry, final LogIndex logIndex, final ExceptionIndex exceptionIndex) {
        record.setThreadName(threadName);
        record.setMessage(message);

        this.entry = entry;
        this.logIndex = logIndex;
        this.exceptionIndex = exceptionIndex;
    }

    public LogRecord decode(final Buffer buffer, int offset, int length) {
        if (hasException(buffer, offset)) {
            return decodeException(buffer, offset, length);
        }

        final long timestamp = buffer.getLong(offset + LogRecordEncoder.TIMESTAMP_OFFSET);
        final long appenderMask = buffer.getLong(offset + LogRecordEncoder.APPENDER_MASK_OFFSET);
        final Buffer logName = logIndex.get(buffer.getInt(offset + LogRecordEncoder.LOG_NAME_OFFSET));
        final LogLevel logLevel = LogLevel.valueOf(buffer.getByte(offset + LogRecordEncoder.LOG_LEVEL_OFFSET));

        record.setTimestamp(timestamp);
        record.setAppenderMask(appenderMask);
        record.setLogName(logName);
        record.setLogLevel(logLevel);

        final byte threadNameLength = buffer.getByte(offset + LogRecordEncoder.THREAD_NAME_LENGTH_OFFSET);
        offset = wrap(buffer, offset + LogRecordEncoder.ROOT_BLOCK_SIZE, threadNameLength, threadName);

        length -= LogRecordEncoder.ROOT_BLOCK_SIZE + threadNameLength;
        message.wrap(buffer, offset, length);

        return record;
    }

    private LogRecord decodeException(final Buffer buffer, int offset, int length) {
        final Throwable exception = exceptionIndex.remove(offset);
        final int exceptionPosition = buffer.getInt(offset + length - LogRecordEncoder.EXCEPTION_POSITION_OFFSET);

        length = buffer.getInt(offset + length - LogRecordEncoder.EXCEPTION_REAL_LENGTH_OFFSET);

        final long timestamp = buffer.getLong(offset + LogRecordEncoder.TIMESTAMP_OFFSET);
        final long appenderMask = buffer.getLong(offset + LogRecordEncoder.APPENDER_MASK_OFFSET);
        final Buffer logName = logIndex.get(buffer.getInt(offset + LogRecordEncoder.LOG_NAME_OFFSET));
        final LogLevel logLevel = LogLevel.valueOf(~buffer.getByte(offset + LogRecordEncoder.LOG_LEVEL_OFFSET));

        record.setTimestamp(timestamp);
        record.setAppenderMask(appenderMask);
        record.setLogName(logName);
        record.setLogLevel(logLevel);

        final byte threadNameLength = buffer.getByte(offset + LogRecordEncoder.THREAD_NAME_LENGTH_OFFSET);
        offset = wrap(buffer, offset + LogRecordEncoder.ROOT_BLOCK_SIZE, threadNameLength, threadName);
        length -= LogRecordEncoder.ROOT_BLOCK_SIZE + threadNameLength;

        formatException(exception, exceptionPosition, buffer, offset, length);
        message.wrap(entry.array(), 0, entry.length());

        return record;
    }

    private void formatException(final Throwable exception,
                                 final int exceptionPosition,
                                 final Buffer buffer,
                                 final int offset,
                                 final int length) {

        entry.reset(0);
        entry.appendUtf8Bytes(buffer, offset, exceptionPosition);
        entry.append(exception);
        entry.appendUtf8Bytes(buffer, offset + exceptionPosition, length - exceptionPosition);
    }

    private static boolean hasException(final Buffer buffer, int offset) {
        final byte logLevel = buffer.getByte(offset + LogRecordEncoder.LOG_LEVEL_OFFSET);
        return logLevel < 0;
    }

    private static int wrap(final Buffer buffer, final int offset, final int length, final UnsafeBuffer flyweight) {
        flyweight.wrap(buffer, offset, length);
        return offset + length;
    }

}
