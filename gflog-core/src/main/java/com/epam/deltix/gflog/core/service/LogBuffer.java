package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.core.util.Buffer;
import com.epam.deltix.gflog.core.util.MutableBuffer;
import com.epam.deltix.gflog.core.util.UnsafeBuffer;
import com.epam.deltix.gflog.core.util.Util;

import static com.epam.deltix.gflog.core.util.Util.UNSAFE;


final class LogBuffer {

    static final int MIN_CAPACITY = 64 * 1024;
    static final int MAX_CAPACITY = 1024 * 1024 * 1024;

    private static final int MAX_READ_LENGTH = 64 * 1024;

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

    LogBuffer(final int capacity) {
        verify(capacity);

        final UnsafeBuffer buffer = UnsafeBuffer.allocateDirectedAlignedPadded(capacity + TRAILER_LENGTH, Util.DOUBLE_CACHE_LINE_SIZE);
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

    // region Producers

    public int tryClaim(final int length) {
        final int required = Util.align(length, LogRecordEncoder.ALIGNMENT);
        long head = UNSAFE.getLongVolatile(null, headCacheAddress);

        int offset;
        int padding;

        while (true) {
            final long tail = UNSAFE.getLongVolatile(null, tailAddress);
            offset = (int) tail & mask;

            final int continuous = capacity - offset;

            padding = (required > continuous) ? continuous : 0;
            final long tailNext = tail + required + padding;

            if (tailNext - head > capacity) {
                head = UNSAFE.getLongVolatile(null, headAddress);

                if (tailNext - head > capacity) {
                    return INSUFFICIENT_SPACE;
                }

                UNSAFE.putOrderedLong(null, headCacheAddress, head);
            }

            if (UNSAFE.compareAndSwapLong(null, tailAddress, tail, tailNext)) {
                break;
            }
        }

        if (padding != 0) {
            UNSAFE.putOrderedInt(null, dataAddress + offset, -padding);
            offset = 0;
        }

        return offset;
    }

    public int claim(final int length, final BackpressureCallback callback) {
        final int required = Util.align(length, LogRecordEncoder.ALIGNMENT);

        while (true) {
            final long tail = UNSAFE.getAndAddLong(null, tailAddress, required);
            final long tailNext = tail + required;

            final int offset = (int) tail & mask;
            final int continuous = capacity - offset;

            long head = UNSAFE.getLongVolatile(null, headCacheAddress);

            if (tailNext - head > capacity) {
                head = UNSAFE.getLongVolatile(null, headAddress);

                while (tailNext - head > capacity) {
                    callback.onBackpressure();
                    head = UNSAFE.getLongVolatile(null, headAddress);
                }

                UNSAFE.putOrderedLong(null, headCacheAddress, head);
            }

            if (required > continuous) {
                UNSAFE.putOrderedInt(null, dataAddress + offset, -continuous);
                UNSAFE.putOrderedInt(null, dataAddress, continuous - required);
                continue;
            }

            return offset;
        }
    }

    public void commit(final int offset, final int length) {
        UNSAFE.putOrderedInt(null, dataAddress + offset, length);
    }

    public void abort(final int offset, final int length) {
        final int padding = Util.align(length, LogRecordEncoder.ALIGNMENT);
        UNSAFE.putOrderedInt(null, dataAddress + offset, -padding);
    }

    // endregion

    // region Consumer

    public int read(final RecordHandler handler) {
        final long head = UNSAFE.getLong(headAddress);

        final int index = (int) head & mask;
        final int limit = Math.min(capacity - index, MAX_READ_LENGTH);

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

    public void unblock() {
        final long head = UNSAFE.getLong(null, headAddress);
        UNSAFE.putOrderedLong(null, headAddress, head + (1L << 60));
    }

    public boolean isEmpty() {
        final long head = UNSAFE.getLong(headAddress);
        final long tail = UNSAFE.getLongVolatile(null, tailAddress);

        return tail == head;
    }

    // endregion

    private static void verify(final int capacity) {
        if (capacity < MIN_CAPACITY) {
            throw new IllegalArgumentException("buffer capacity: " + capacity + " is less than min: " + MIN_CAPACITY);
        }

        if (capacity > MAX_CAPACITY) {
            throw new IllegalArgumentException("buffer capacity: " + capacity + " is more than max: " + MAX_CAPACITY);
        }

        if (!Util.isPowerOfTwo(capacity)) {
            throw new IllegalArgumentException("buffer capacity: " + capacity + " is not power of two");
        }
    }

    public interface BackpressureCallback {

        void onBackpressure();

    }

    public interface RecordHandler {

        void onRecord(final Buffer buffer, final int offset, final int length);

    }

}
