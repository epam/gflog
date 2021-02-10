package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.core.util.Buffer;
import com.epam.deltix.gflog.core.util.MutableBuffer;
import com.epam.deltix.gflog.core.util.UnsafeBuffer;
import com.epam.deltix.gflog.core.util.Util;

import static com.epam.deltix.gflog.core.util.Util.UNSAFE;


final class LogBuffer {

    private static final int MIN_CAPACITY = 64 * 1024;
    private static final int MAX_CAPACITY = 1024 * 1024 * 1024;

    private static final int TAIL_OFFSET = Util.DOUBLE_CACHE_LINE_SIZE - Util.SIZE_OF_LONG;
    private static final int HEAD_OFFSET = TAIL_OFFSET + Util.DOUBLE_CACHE_LINE_SIZE;
    private static final int HEAD_CACHE_OFFSET = HEAD_OFFSET + Util.DOUBLE_CACHE_LINE_SIZE;

    private static final int TRAILER_LENGTH = HEAD_CACHE_OFFSET + Util.SIZE_OF_LONG + Util.DOUBLE_CACHE_LINE_SIZE;
    private static final int INSUFFICIENT_SPACE = -1;

    private final UnsafeBuffer buffer;

    private final int capacity;
    private final int mask;
    private final int maxRecordLength;

    private final long dataAddress;
    private final long tailAddress;
    private final long headAddress;
    private final long headCacheAddress;

    LogBuffer(int capacity) {
        capacity = findCapacity(capacity);

        final UnsafeBuffer buffer = UnsafeBuffer.allocateDirectAligned(capacity + TRAILER_LENGTH, Util.DOUBLE_CACHE_LINE_SIZE);
        buffer.wrap(buffer, 0, capacity);

        final long dataAddress = buffer.address();
        final long trailerAddress = dataAddress + capacity;

        this.capacity = capacity;
        this.mask = capacity - 1;
        this.maxRecordLength = capacity >>> 3;

        this.buffer = buffer;
        this.dataAddress = dataAddress;
        this.tailAddress = trailerAddress + TAIL_OFFSET;
        this.headAddress = trailerAddress + HEAD_OFFSET;
        this.headCacheAddress = trailerAddress + HEAD_CACHE_OFFSET;
    }

    public MutableBuffer buffer() {
        return buffer;
    }

    public int capacity() {
        return capacity;
    }

    public int maxRecordLength() {
        return maxRecordLength;
    }

    public long dataAddress() {
        return dataAddress;
    }

    public int size() {
        final long head = UNSAFE.getLongVolatile(null, headAddress);
        final long tail = UNSAFE.getLongVolatile(null, tailAddress);

        return (int) (tail - head);
    }

    public boolean isEmpty() {
        final long head = UNSAFE.getLongVolatile(null, headAddress);
        final long tail = UNSAFE.getLongVolatile(null, tailAddress);

        return tail == head;
    }

    public int claim(final int length) {
        final int aligned = Util.align(length, LogRecordEncoder.ALIGNMENT);

        long head = UNSAFE.getLongVolatile(null, headCacheAddress);
        long tail;
        long tailNext;

        int offset;
        int padding;

        do {
            tail = UNSAFE.getLongVolatile(null, tailAddress);
            tailNext = tail + aligned;

            if (tailNext - head > capacity) {
                head = UNSAFE.getLongVolatile(null, headAddress);

                if (tailNext - head > capacity) {
                    return INSUFFICIENT_SPACE;
                }

                UNSAFE.putOrderedLong(null, headCacheAddress, head);
            }

            padding = 0;
            offset = (int) tail & mask;

            final int continuous = capacity - offset;

            if (aligned > continuous) {
                tailNext += continuous;

                if (tailNext - head > capacity) {
                    head = UNSAFE.getLongVolatile(null, headAddress);

                    if (tailNext - head > capacity) {
                        return INSUFFICIENT_SPACE;
                    }

                    UNSAFE.putOrderedLong(null, headCacheAddress, head);
                }

                padding = continuous;
            }

        } while (!UNSAFE.compareAndSwapLong(null, tailAddress, tail, tailNext));

        if (padding != 0) {
            UNSAFE.putOrderedInt(null, dataAddress + offset, -padding);
            offset = 0;
        }

        return offset;
    }

    public void commit(final int offset, final int length) {
        UNSAFE.putOrderedInt(null, dataAddress + offset, length);
    }

    public void abort(final int offset, final int length) {
        final int padding = Util.align(length, LogRecordEncoder.ALIGNMENT);
        UNSAFE.putOrderedInt(null, dataAddress + offset, -padding);
    }

    public int read(final RecordHandler handler) {
        final long head = UNSAFE.getLong(headAddress);

        final int index = (int) head & mask;
        final int limit = Math.min(maxRecordLength, capacity - index);

        int read = 0;

        try {
            do {
                final int offset = index + read;
                final int length = UNSAFE.getIntVolatile(null, dataAddress + offset);

                if (length == 0) {
                    break;
                }

                if (length < 0) {
                    read += -length;
                    continue;
                }

                read += Util.align(length, LogRecordEncoder.ALIGNMENT);
                handler.onRecord(buffer, offset, length);
            } while (read < limit);
        } finally {
            if (read != 0) {
                UNSAFE.putByte(dataAddress + index, (byte) 0);
                UNSAFE.setMemory(dataAddress + index + 1, read - 1, (byte) 0);
                UNSAFE.putOrderedLong(null, headAddress, head + read);
            }
        }

        return read;
    }

    private static int findCapacity(final int capacity) {
        if (capacity < MIN_CAPACITY) {
            return MIN_CAPACITY;
        }

        if (capacity > MAX_CAPACITY) {
            return MAX_CAPACITY;
        }

        return Util.nextPowerOfTwo(capacity);
    }

    public interface RecordHandler {

        void onRecord(final Buffer buffer, final int offset, final int length);

    }

}
