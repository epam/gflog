package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.core.util.Util;


final class ExceptionIndex {

    static final int MIN_CAPACITY = 256;
    static final int MIN_SEGMENT = 256;

    private final Throwable[] map;
    private final int segment;
    private final int shift;

    ExceptionIndex(final int indexCapacity, final int bufferCapacity) {
        verify(indexCapacity, bufferCapacity);

        this.map = new Throwable[indexCapacity];
        this.segment = bufferCapacity / indexCapacity;
        this.shift = Integer.numberOfTrailingZeros(segment);
    }

    int segment() {
        return segment;
    }

    void put(final int recordOffset, final Throwable exception) {
        final int index = recordOffset >> shift;
        map[index] = exception;
    }

    Throwable remove(final int recordOffset) {
        final int index = recordOffset >> shift;

        final Throwable exception = map[index];
        map[index] = null;

        return exception;
    }

    private static void verify(final int indexCapacity, final int bufferCapacity) {
        if (!Util.isPowerOfTwo(indexCapacity)) {
            throw new IllegalArgumentException("exception index capacity is not power of two: " + indexCapacity);
        }

        if (!Util.isPowerOfTwo(bufferCapacity)) {
            throw new IllegalArgumentException("log buffer capacity is not power of two: " + bufferCapacity);
        }

        if (indexCapacity < MIN_CAPACITY) {
            throw new IllegalArgumentException("exception index capacity: " + indexCapacity +
                    " is less than min: " + MIN_CAPACITY);
        }

        if (indexCapacity > bufferCapacity) {
            throw new IllegalArgumentException("log buffer capacity: " + bufferCapacity +
                    " is less than exception index capacity: " + indexCapacity);
        }
    }

}
