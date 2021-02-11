package com.epam.deltix.gflog.core.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;


public final class Util {

    public static final Unsafe UNSAFE;
    public static final long ARRAY_BYTE_BASE_OFFSET;
    public static final long ARRAY_SHORT_BASE_OFFSET;
    public static final long ARRAY_INT_BASE_OFFSET;
    public static final long ARRAY_LONG_BASE_OFFSET;

    private static final long BYTE_BUFFER_HB_FIELD_OFFSET;
    private static final long BYTE_BUFFER_OFFSET_FIELD_OFFSET;

    static {
        try {
            final PrivilegedExceptionAction<Unsafe> action = () -> {
                final Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                return (Unsafe) f.get(null);
            };

            UNSAFE = AccessController.doPrivileged(action);
            ARRAY_BYTE_BASE_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
            ARRAY_SHORT_BASE_OFFSET = UNSAFE.arrayBaseOffset(short[].class);
            ARRAY_INT_BASE_OFFSET = UNSAFE.arrayBaseOffset(int[].class);
            ARRAY_LONG_BASE_OFFSET = UNSAFE.arrayBaseOffset(long[].class);
            BYTE_BUFFER_HB_FIELD_OFFSET = UNSAFE.objectFieldOffset(ByteBuffer.class.getDeclaredField("hb"));
            BYTE_BUFFER_OFFSET_FIELD_OFFSET = UNSAFE.objectFieldOffset(ByteBuffer.class.getDeclaredField("offset"));
        } catch (final Exception ex) {
            throw new Error(ex);
        }
    }

    public static final int SIZE_OF_BYTE = 1;
    public static final int SIZE_OF_CHAR = 2;
    public static final int SIZE_OF_SHORT = 2;
    public static final int SIZE_OF_INT = 4;
    public static final int SIZE_OF_LONG = 8;

    public static final int CACHE_LINE_SIZE = 64;
    public static final int DOUBLE_CACHE_LINE_SIZE = 2 * CACHE_LINE_SIZE;

    public static final ByteOrder NATIVE_BYTE_ORDER = ByteOrder.nativeOrder();
    public static final boolean BOUNDS_CHECK_ENABLED = PropertyUtil.getBoolean("gflog.bounds.check", true);

    public static final String LINE_SEPARATOR = System.lineSeparator();

    private Util() {
    }

    public static void rethrow(final Throwable e) {
        throwUnchecked(e);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwUnchecked(final Throwable e) throws T {
        throw (T) e;
    }

    public static boolean isOutOfBounds(final int offset, final int capacity) {
        return Integer.toUnsignedLong(offset) >= capacity;
    }

    public static boolean isOutOfBounds(final int offset, final int length, final int capacity) {
        return Integer.toUnsignedLong(offset) + Integer.toUnsignedLong(length) > capacity;
    }

    public static void verifyBounds(final int offset, final int capacity) {
        if (isOutOfBounds(offset, capacity)) {
            throw new IndexOutOfBoundsException("offset=" + offset + ", capacity=" + capacity);
        }
    }

    public static void verifyBounds(final int offset, final int length, final int capacity) {
        if (isOutOfBounds(offset, length, capacity)) {
            throw new IndexOutOfBoundsException("offset=" + offset + ", length=" + length + ", capacity=" + capacity);
        }
    }

    public static void verifyBounds(final byte[] array, final int offset, final int length) {
        final int capacity = array.length;
        if (isOutOfBounds(offset, length, capacity)) {
            throw new IndexOutOfBoundsException("offset=" + offset + ", length=" + length + ", array.length=" + capacity);
        }
    }

    public static void verifyBounds(final char[] array, final int offset, final int length) {
        final int capacity = array.length;
        if (isOutOfBounds(offset, length, capacity)) {
            throw new IndexOutOfBoundsException("offset=" + offset + ", length=" + length + ", array.length=" + capacity);
        }
    }

    public static void verifyBounds(final ByteBuffer buffer, final int offset, final int length) {
        final int capacity = buffer.capacity();
        if (isOutOfBounds(offset, length, capacity)) {
            throw new IndexOutOfBoundsException("offset=" + offset + ", length=" + length + ", buffer.capacity=" + capacity);
        }
    }

    public static int nextPowerOfTwo(final int value) {
        return 1 << (32 - Integer.numberOfLeadingZeros(value - 1));
    }

    public static long align(final long value, final long alignment) {
        final long mask = alignment - 1;
        return (value + mask) & ~mask;
    }

    public static int align(final int value, final int alignment) {
        final int mask = alignment - 1;
        return (value + mask) & ~mask;
    }

    public static boolean isPowerOfTwo(final int value) {
        return (value > 0) && (Integer.bitCount(value) == 1);
    }

    /**
     * Makes the platform dependent short.
     */
    public static short makeShort(final int b1, final int b0) {
        return (NATIVE_BYTE_ORDER == ByteOrder.LITTLE_ENDIAN) ?
                (short) ((b0 << 8) | (b1)) :
                (short) ((b1 << 8) | (b0));
    }

    /**
     * Makes the short depending on the platform.
     */
    public static short makeShort(final int b1, final int b0, final ByteOrder byteOrder) {
        return (byteOrder == ByteOrder.LITTLE_ENDIAN) ?
                (short) ((b0 << 8) | (b1)) :
                (short) ((b1 << 8) | (b0));
    }

    /**
     * Makes the platform dependent int.
     */
    public static int makeInt(final int b3, final int b2, final int b1, final int b0) {
        return (NATIVE_BYTE_ORDER == ByteOrder.LITTLE_ENDIAN) ?
                ((b0 << 24) | (b1 << 16) | (b2 << 8) | (b3)) :
                ((b3 << 24) | (b2 << 16) | (b1 << 8) | (b0));
    }

    /**
     * Makes the platform dependent long.
     */
    public static long makeLong(final int b7, final int b6, final int b5, final int b4,
                                final int b3, final int b2, final int b1, final int b0) {

        return (NATIVE_BYTE_ORDER == ByteOrder.LITTLE_ENDIAN) ?
                (((long) b0 << 56) | ((long) b1 << 48) | ((long) b2 << 40) | ((long) b3 << 32) | ((long) b4 << 24) | (b5 << 16) | (b6 << 8) | b7) :
                (((long) b7 << 56) | ((long) b6 << 48) | ((long) b5 << 40) | ((long) b4 << 32) | ((long) b3 << 24) | (b2 << 16) | (b1 << 8) | b0);
    }

    public static int codePointAt(final CharSequence sequence, final int index, final int end) {
        if (end - index < 2) {
            throw new IndexOutOfBoundsException("no space for surrogate pair: index=" + index + ", end=" + end);
        }

        final char c1 = sequence.charAt(index);
        final char c2 = sequence.charAt(index + 1);

        if (!Character.isSurrogatePair(c1, c2)) {
            throw new IllegalArgumentException("not a surrogate pair");
        }

        return Character.toCodePoint(c1, c2);
    }

    public static int findUtf8Bound(final CharSequence value, int start, final int end, int limit) {
        while (start < end) {
            final char c = value.charAt(start);
            boolean surrogate = false;

            if (c <= 0x007F) {
                limit -= 1;
            } else if (c <= 0x07FF) {
                limit -= 2;
            } else if (!Character.isSurrogate(c)) {
                limit -= 3;
            } else {
                final int next = start + 1;

                if (next == end) {
                    throw new IndexOutOfBoundsException("no space for low surrogate");
                }

                if (!Character.isSurrogatePair(c, value.charAt(next))) {
                    throw new IllegalArgumentException("not a surrogate pair");
                }

                surrogate = true;
                limit -= 4;
            }

            if (limit < 0) {
                break;
            }

            start += surrogate ? 2 : 1;
        }

        return start;
    }

    public static UnsafeBuffer fromUtf8String(final String string) {
        final byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        return new UnsafeBuffer(bytes);
    }

    public static UnsafeBuffer fromUtf8String(final String string, int limit) {
        final byte[] bytes = string.getBytes(StandardCharsets.UTF_8);

        if (bytes.length <= limit) {
            return new UnsafeBuffer(bytes);
        }

        byte b = bytes[limit];

        if ((b & 0b11000000) == 0b10000000) {
            while (limit > 0) {
                b = bytes[--limit];

                if ((b & 0b01000000) != 0) {
                    break;
                }
            }
        }

        return new UnsafeBuffer(bytes, 0, limit);
    }

    public static String toUtf8String(final Buffer buffer) {
        final byte[] bytes = new byte[buffer.capacity()];
        buffer.getBytes(0, bytes);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Allocate a new direct {@link ByteBuffer} that is aligned on a given alignment boundary.
     *
     * @param capacity  required for the buffer.
     * @param alignment boundary at which the buffer should begin.
     * @return a new {@link ByteBuffer} with the required alignment.
     * @throws IllegalArgumentException if the alignment is not a power of 2.
     */
    static ByteBuffer allocateDirectAligned(final int capacity, final int alignment) {
        if (!isPowerOfTwo(alignment)) {
            throw new IllegalArgumentException("Must be a power of 2: alignment=" + alignment);
        }

        final ByteBuffer buffer = ByteBuffer.allocateDirect(capacity + alignment);

        final long address = address(buffer);
        final int mask = alignment - 1;
        final int remainder = (int) (address & mask);
        final int offset = alignment - remainder;

        buffer.limit(capacity + offset);
        buffer.position(offset);

        return buffer.slice();
    }

    /**
     * Allocate a direct {@link ByteBuffer} that is padded at the end with at least alignment bytes.
     *
     * @param capacity  for the buffer.
     * @param alignment for the buffer.
     * @return the direct {@link ByteBuffer}.
     */
    static ByteBuffer allocateDirectAlignedAndPadded(final int capacity, final int alignment) {
        final ByteBuffer buffer = allocateDirectAligned(capacity + alignment, alignment);

        buffer.limit(buffer.limit() - alignment);

        return buffer.slice();
    }

    /**
     * Get the address at which the underlying buffer storage begins.
     *
     * @param buffer that wraps the underlying storage.
     * @return the memory address at which the buffer storage begins.
     */
    static long address(final ByteBuffer buffer) {
        return ((sun.nio.ch.DirectBuffer) buffer).address();
    }

    /**
     * Get the array from a read-only {@link ByteBuffer} similar to {@link ByteBuffer#array()}.
     *
     * @param buffer that wraps the underlying array.
     * @return the underlying array.
     */
    static byte[] array(final ByteBuffer buffer) {
        return (byte[]) UNSAFE.getObject(buffer, BYTE_BUFFER_HB_FIELD_OFFSET);
    }

    /**
     * Get the array offset from a read-only {@link ByteBuffer} similar to {@link ByteBuffer#arrayOffset()}.
     *
     * @param buffer that wraps the underlying array.
     * @return the underlying array offset at which this ByteBuffer starts.
     */
    static int arrayOffset(final ByteBuffer buffer) {
        return UNSAFE.getInt(buffer, BYTE_BUFFER_OFFSET_FIELD_OFFSET);
    }

}