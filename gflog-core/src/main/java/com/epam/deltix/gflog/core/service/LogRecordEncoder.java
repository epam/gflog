package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.core.util.Buffer;
import com.epam.deltix.gflog.core.util.Util;


final class LogRecordEncoder {

    static final int LENGTH_OFFSET = 0;
    static final int LOG_NAME_OFFSET = 4;
    static final int TIMESTAMP_OFFSET = 8;
    static final int APPENDER_MASK_OFFSET = 16;
    static final int LOG_LEVEL_OFFSET = 24;
    static final int THREAD_NAME_LENGTH_OFFSET = 25;

    static final int ROOT_BLOCK_SIZE = 26;
    static final int MIN_SIZE = ROOT_BLOCK_SIZE + Byte.MAX_VALUE;
    static final int ALIGNMENT = 8;

    public static int size(final Buffer threadName, final int messageLength) {
        return ROOT_BLOCK_SIZE + threadName.capacity() + messageLength;
    }

    public static void encode(final long timestamp,
                              final long appenderMask,
                              final int logName,
                              final int logLevel,
                              final Buffer threadName,
                              final byte[] messageBuffer,
                              final int messageLength,
                              final long address) {

        final int threadNameLength = threadName.capacity();
        final long threadNameAddress = address + ROOT_BLOCK_SIZE;
        final long messageAddress = threadNameAddress + threadNameLength;

        Util.UNSAFE.putInt(address + LOG_NAME_OFFSET, logName);
        Util.UNSAFE.putLong(address + TIMESTAMP_OFFSET, timestamp);
        Util.UNSAFE.putLong(address + APPENDER_MASK_OFFSET, appenderMask);
        Util.UNSAFE.putByte(address + LOG_LEVEL_OFFSET, (byte) logLevel);
        Util.UNSAFE.putByte(address + THREAD_NAME_LENGTH_OFFSET, (byte) threadNameLength);

        Util.UNSAFE.copyMemory(threadName.byteArray(), threadName.address(), null, threadNameAddress, threadNameLength);
        Util.UNSAFE.copyMemory(messageBuffer, Util.ARRAY_BYTE_BASE_OFFSET, null, messageAddress, messageLength);
    }

}
