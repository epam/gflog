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

    private final LogIndex logIndex;

    LogRecordDecoder(final LogIndex index) {
        record.setThreadName(threadName);
        record.setMessage(message);

        logIndex = index;
    }

    public LogRecord decode(final Buffer buffer, int offset, int length) {
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

    private static int wrap(final Buffer buffer, final int offset, final int length, final UnsafeBuffer flyweight) {
        flyweight.wrap(buffer, offset, length);
        return offset + length;
    }

}
