package com.epam.deltix.gflog.core.util;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static com.epam.deltix.gflog.core.util.Util.*;
import static java.lang.Integer.toUnsignedLong;

/**
 * Uses unsafe code to format primitive types to text representation. Use with care. Otherwise JVM will crash.
 */
public final class Formatting {

    public static final int LENGTH_OF_BYTE = 1;
    public static final int LENGTH_OF_NAN = 3;
    public static final int LENGTH_OF_INFINITY = 8;
    public static final int LENGTH_OF_NULL = 4;
    public static final int LENGTH_OF_TRUE = 4;
    public static final int LENGTH_OF_FALSE = 5;
    public static final int LENGTH_OF_TIMESTAMP = 24;
    public static final int LENGTH_OF_DATE = 10;
    public static final int LENGTH_OF_TIME = 12;

    public static final int MAX_LENGTH_OF_BOOLEAN = 5;
    public static final int MAX_LENGTH_OF_ALPHANUMERIC = 10;
    public static final int MAX_LENGTH_OF_INT = 11;
    public static final int MAX_LENGTH_OF_LONG = 20;
    public static final int MAX_LENGTH_OF_DOUBLE = 30;

    public static final byte T = 'T';
    public static final byte Z = 'Z';
    public static final byte MINUS = '-';
    public static final byte DOT = '.';
    public static final byte COLON = ':';
    public static final byte SPACE = ' ';
    public static final byte ZERO = '0';

    public static final int MAX_PRECISION_OF_DOUBLE = 9;
    public static final long MIN_VALUE_OF_TIMESTAMP = 0L;
    public static final long MAX_VALUE_OF_TIMESTAMP = 4102444800000L;
    public static final long MIN_VALUE_OF_TIMESTAMP_NS = 0L;
    public static final long MAX_VALUE_OF_TIMESTAMP_NS = 4102444800000000000L;
    public static final double MAX_VALUE_OF_DOUBLE = 9e18;

    public static final long DECIMAL_64_SPECIAL_MASK = 0x60_00_00_00_00_00_00_00L;
    public static final long DECIMAL_64_INFINITY_MASK = 0x78_00_00_00_00_00_00_00L;
    public static final long DECIMAL_64_NAN_MASK = 0x7C_00_00_00_00_00_00_00L;
    public static final int DECIMAL_64_EXPONENT_MASK = 0x03_FF;
    public static final int DECIMAL_64_EXPONENT_SHIFT_SPECIAL = 51;
    public static final int DECIMAL_64_EXPONENT_SHIFT = 53;
    public static final int DECIMAL_64_EXPONENT_BIAS = 398;

    public static final long DECIMAL_64_SIGNIFICAND_MAX_VALUE = 9999_9999_9999_9999L;
    public static final int DECIMAL_64_SIGNIFICAND_MAX_DIGITS = 16;
    public static final long DECIMAL_64_SIGNIFICAND_MASK = 0x00_1F_FF_FF_FF_FF_FF_FFL;
    public static final long DECIMAL_64_SIGNIFICAND_MASK_SPECIAL = 0x00_07_FF_FF_FF_FF_FF_FFL;
    public static final long DECIMAL_64_SIGNIFICAND_BIT_SPECIAL = 0x00_20_00_00_00_00_00_00L;

    public static final int DOUBLE_MULTIPLIER = 1_000_000_000;
    public static final int DOUBLE_SCALE = 9;

    private static final int WORD_NULL = makeInt('n', 'u', 'l', 'l');
    private static final short WORD_NAN_1 = makeShort('N', 'a');
    private static final byte WORD_NAN_2 = 'N';
    private static final long WORD_INFINITY = makeLong('I', 'n', 'f', 'i', 'n', 'i', 't', 'y');
    private static final int WORD_TRUE = makeInt('t', 'r', 'u', 'e');
    private static final int WORD_FALSE_1 = makeInt('f', 'a', 'l', 's');
    private static final byte WORD_FALSE_2 = 'e';
    private static final long WORD_INT_MIN_1 = makeLong('-', '2', '1', '4', '7', '4', '8', '3');
    private static final short WORD_INT_MIN_2 = makeShort('6', '4');
    private static final byte WORD_INT_MIN_3 = '8';
    private static final long WORD_LONG_MIN_1 = makeLong('-', '9', '2', '2', '3', '3', '7', '2');
    private static final long WORD_LONG_MIN_2 = makeLong('0', '3', '6', '8', '5', '4', '7', '7');
    private static final int WORD_LONG_MIN_3 = makeInt('5', '8', '0', '8');
    private static final short WORD_ZERO_WITH_DOT = makeShort('0', '.');

    private static final long[] ULONG_MULTIPLIER_TABLE = {
            1,
            10,
            100,
            1000,
            10000,
            100000,
            1000000,
            10000000,
            100000000,
            1000000000,
            10000000000L,
            100000000000L,
            1000000000000L,
            10000000000000L,
            100000000000000L,
            1000000000000000L,
            10000000000000000L,
            100000000000000000L,
            1000000000000000000L
    };

    private static final int[] UINT_MULTIPLIER_TABLE = {
            1,
            10,
            100,
            1000,
            10000,
            100000,
            1000000,
            10000000,
            100000000,
            1000000000
    };

    private static final byte[] UINT_LENGTH_TABLE = {
            9, 9, 9, 9, 9,
            8, 8, 8,
            7, 7, 7, 7,
            6, 6, 6,
            5, 5, 5,
            4, 4, 4, 4,
            3, 3, 3,
            2, 2, 2,
            1, 1, 1, 1, 1
    };

    public static final int DAY_MS = 86400000;
    public static final long DAY_NS = 86400000000000L;

    public static final int DAYS_IN_YEAR = 365;
    public static final int DAYS_IN_LEAP_YEAR = 366;
    public static final int DAYS_IN_4_CYCLE = 1461;

    public static final int SECOND_MS = 1000;
    public static final long SECOND_NS = 1000000000;

    public static final int MINUTE_MS = SECOND_MS * 60;
    public static final long MINUTE_NS = SECOND_NS * 60;

    public static final int HOUR_MS = MINUTE_MS * 60;
    public static final long HOUR_NS = MINUTE_NS * 60;

    private static final long TIMESTAMP_BASE_MS = (DAYS_IN_LEAP_YEAR + DAYS_IN_YEAR) * (long) DAY_MS;
    private static final long TIMESTAMP_BASE_NS = TIMESTAMP_BASE_MS * 1_000_000;

    private static final int TIMESTAMP_BASE_YEAR = 1968;

    private static final byte[] DAYS_TO_MONTH_TABLE = { // +1 day in January cause for ordinary year we are looking up for day + 1
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
            3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
            4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
            5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
            9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
            10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
            11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
            12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12
    };

    private static final byte[] DAYS_TO_MONTH_LEAP_TABLE = {
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
            3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
            4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
            5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
            9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
            10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
            11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
            12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12
    };

    // day start from 1 but we are looking up for ordinary year
    private static final short[] MONTH_TO_DAYS_TABLE = {0, 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
    // -1 since day start from 1, saving one sub
    private static final short[] MONTH_TO_DAYS_LEAP_TABLE = {-1, -1, 30, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};

    private static final Buffer BUFFER_REFERENCE;

    private static final long ADDRESS_OF_ULONG_MULTIPLIER_TABLE;
    private static final long ADDRESS_OF_UINT_MULTIPLIER_TABLE;
    private static final long ADDRESS_OF_UINT_LENGTH_TABLE;
    private static final long ADDRESS_OF_DIGITS_TABLE;
    private static final long ADDRESS_OF_DAYS_TO_MONTH_TABLE;
    private static final long ADDRESS_OF_DAYS_TO_MONTH_LEAP_TABLE;
    private static final long ADDRESS_OF_MONTH_TO_DAYS_TABLE;
    private static final long ADDRESS_OF_MONTH_TO_DAYS_LEAP_TABLE;

    // region Tables

    static {
        final int prePadding = 2 * DOUBLE_CACHE_LINE_SIZE; // for alignment
        final int postPadding = DOUBLE_CACHE_LINE_SIZE;

        final int ulongMultiplierTableSpace = ULONG_MULTIPLIER_TABLE.length * SIZE_OF_LONG;
        final int uintMultiplierTableSpace = UINT_MULTIPLIER_TABLE.length * SIZE_OF_INT;
        final int uintLengthTableSpace = UINT_LENGTH_TABLE.length;
        final int digitsTableSpace = 100 * SIZE_OF_SHORT;

        final int monthToDaysTableSpace = MONTH_TO_DAYS_TABLE.length * SIZE_OF_SHORT;
        final int monthToDaysLeapTableSpace = MONTH_TO_DAYS_LEAP_TABLE.length * SIZE_OF_SHORT;

        final int daysToMonthTableSpace = DAYS_TO_MONTH_TABLE.length;
        final int daysToMonthLeapTableSpace = DAYS_TO_MONTH_LEAP_TABLE.length;

        final int memorySpace = prePadding +
                ulongMultiplierTableSpace + uintMultiplierTableSpace + align(uintLengthTableSpace, SIZE_OF_SHORT) +
                digitsTableSpace +
                monthToDaysTableSpace + monthToDaysLeapTableSpace +
                daysToMonthTableSpace + daysToMonthLeapTableSpace +
                postPadding;

        final Buffer buffer = UnsafeBuffer.allocateDirectedAlignedPadded(memorySpace, DOUBLE_CACHE_LINE_SIZE);
        final long memoryAddress = buffer.address();

        BUFFER_REFERENCE = buffer;

        ADDRESS_OF_ULONG_MULTIPLIER_TABLE = align(memoryAddress + DOUBLE_CACHE_LINE_SIZE, DOUBLE_CACHE_LINE_SIZE);
        ADDRESS_OF_UINT_MULTIPLIER_TABLE = ADDRESS_OF_ULONG_MULTIPLIER_TABLE + ulongMultiplierTableSpace;
        ADDRESS_OF_UINT_LENGTH_TABLE = ADDRESS_OF_UINT_MULTIPLIER_TABLE + uintMultiplierTableSpace;
        ADDRESS_OF_DIGITS_TABLE = align(ADDRESS_OF_UINT_LENGTH_TABLE + uintLengthTableSpace, SIZE_OF_SHORT);
        ADDRESS_OF_DAYS_TO_MONTH_TABLE = ADDRESS_OF_DIGITS_TABLE + digitsTableSpace;
        ADDRESS_OF_DAYS_TO_MONTH_LEAP_TABLE = ADDRESS_OF_DAYS_TO_MONTH_TABLE + daysToMonthTableSpace;
        ADDRESS_OF_MONTH_TO_DAYS_TABLE = ADDRESS_OF_DAYS_TO_MONTH_LEAP_TABLE + daysToMonthLeapTableSpace;
        ADDRESS_OF_MONTH_TO_DAYS_LEAP_TABLE = ADDRESS_OF_MONTH_TO_DAYS_TABLE + monthToDaysTableSpace;

        UNSAFE.copyMemory(ULONG_MULTIPLIER_TABLE, ARRAY_LONG_BASE_OFFSET, null, ADDRESS_OF_ULONG_MULTIPLIER_TABLE, ulongMultiplierTableSpace);
        UNSAFE.copyMemory(UINT_MULTIPLIER_TABLE, ARRAY_INT_BASE_OFFSET, null, ADDRESS_OF_UINT_MULTIPLIER_TABLE, uintMultiplierTableSpace);
        UNSAFE.copyMemory(UINT_LENGTH_TABLE, ARRAY_BYTE_BASE_OFFSET, null, ADDRESS_OF_UINT_LENGTH_TABLE, uintLengthTableSpace);

        UNSAFE.copyMemory(DAYS_TO_MONTH_TABLE, ARRAY_BYTE_BASE_OFFSET, null, ADDRESS_OF_DAYS_TO_MONTH_TABLE, daysToMonthTableSpace);
        UNSAFE.copyMemory(DAYS_TO_MONTH_LEAP_TABLE, ARRAY_BYTE_BASE_OFFSET, null, ADDRESS_OF_DAYS_TO_MONTH_LEAP_TABLE, daysToMonthLeapTableSpace);

        UNSAFE.copyMemory(MONTH_TO_DAYS_TABLE, ARRAY_SHORT_BASE_OFFSET, null, ADDRESS_OF_MONTH_TO_DAYS_TABLE, monthToDaysTableSpace);
        UNSAFE.copyMemory(MONTH_TO_DAYS_LEAP_TABLE, ARRAY_SHORT_BASE_OFFSET, null, ADDRESS_OF_MONTH_TO_DAYS_LEAP_TABLE, monthToDaysLeapTableSpace);

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                final int index = (10 * i + j) * SIZE_OF_SHORT;

                final int firstDigit = i + '0';
                final int secondDigit = j + '0';

                UNSAFE.putShort(ADDRESS_OF_DIGITS_TABLE + index, makeShort(firstDigit, secondDigit));
            }
        }
    }

    // endregion

    private Formatting() {
    }

    // region Misc

    public static long multiplierOfULong(final @Nonnegative int length) {
        // Preconditions:
        // assert length >= 0 && length <= 18;

        return UNSAFE.getLong(ADDRESS_OF_ULONG_MULTIPLIER_TABLE + (length << 3));
    }

    public static int multiplierOfUInt(final @Nonnegative int length) {
        // Preconditions:
        // assert length >= 0 && length <= 9;

        return UNSAFE.getInt(ADDRESS_OF_UINT_MULTIPLIER_TABLE + (length << 2));
    }

    public static int lengthOfUInt(final @Nonnegative int value) {
        // Preconditions:
        // assert value >= 0;

        final int index = Integer.numberOfLeadingZeros(value);
        final int length = UNSAFE.getByte(ADDRESS_OF_UINT_LENGTH_TABLE + index);
        final int multiplier = multiplierOfUInt(length);
        return length + ((value >= multiplier) ? 1 : 0); // cmov
    }

    public static int lengthOfAlphanumeric(final long value) {
        return (int) (value >>> 60);
    }

    public static int maxLengthOfDecimal64(final int exponent) {
        // assert exponent >= DECIMAL_64_EXPONENT_MIN_VALUE && exponent <= DECIMAL_64_EXPONENT_MAX_VALUE;

        return (exponent >= 0) ?
                (17 + exponent) :
                Math.max(19, -exponent + 3);
    }

    public static int formatZeros(final @Nonnegative int zeros,
                                  final @Nonnull byte[] array,
                                  @Nonnegative int offset) {
        // Preconditions:
        // assert zeros >= 0;
        // assert array != null;
        // assert offset >= 0;

        UNSAFE.setMemory(array, ARRAY_BYTE_BASE_OFFSET + offset, zeros, ZERO);
        return offset + zeros;
    }

    public static int formatZeros(final @Nonnegative int zeros,
                                  final @Nonnull MutableBuffer buffer,
                                  @Nonnegative int offset) {
        // Preconditions:
        // assert zeros >= 0;
        // assert array != null;
        // assert offset >= 0;

        buffer.setMemory(offset, zeros, ZERO);
        return offset + zeros;
    }

    public static int formatZeroWithDot(final @Nonnull byte[] array,
                                        final @Nonnegative int offset) {
        // assert array != null;
        // assert offset >= 0;

        UNSAFE.putShort(array, ARRAY_BYTE_BASE_OFFSET + offset, WORD_ZERO_WITH_DOT);
        return offset + SIZE_OF_SHORT;
    }

    public static int formatZeroWithDot(final @Nonnull MutableBuffer buffer,
                                        final @Nonnegative int offset) {
        // assert array != null;
        // assert offset >= 0;

        buffer.putShort(offset, WORD_ZERO_WITH_DOT);
        return offset + SIZE_OF_SHORT;
    }

    public static int formatNull(final @Nonnull byte[] array, final @Nonnegative int offset) {
        // Preconditions:
        // assert array != null;
        // assert offset >= 0;

        UNSAFE.putInt(array, ARRAY_BYTE_BASE_OFFSET + offset, WORD_NULL);
        return offset + LENGTH_OF_NULL;
    }

    public static int formatNaN(final @Nonnull byte[] array, final @Nonnegative int offset) {
        // Preconditions:
        // assert array != null;
        // assert offset >= 0;

        UNSAFE.putShort(array, ARRAY_BYTE_BASE_OFFSET + offset, WORD_NAN_1);
        UNSAFE.putByte(array, ARRAY_BYTE_BASE_OFFSET + SIZE_OF_SHORT + offset, WORD_NAN_2);

        return offset + LENGTH_OF_NAN;
    }

    public static int formatInfinity(final @Nonnull byte[] array, final @Nonnegative int offset) {
        // Preconditions:
        // assert array != null;
        // assert offset >= 0;

        UNSAFE.putLong(array, ARRAY_BYTE_BASE_OFFSET + offset, WORD_INFINITY);
        return offset + LENGTH_OF_INFINITY;
    }

    public static int formatBoolean(final boolean value, final @Nonnull byte[] array, final @Nonnegative int offset) {
        // Preconditions:
        // assert array != null;
        // assert offset >= 0;

        return value ? formatTrue(array, offset) : formatFalse(array, offset);
    }

    public static int formatTrue(final @Nonnull byte[] array, final @Nonnegative int offset) {
        // Preconditions:
        // assert array != null;
        // assert offset >= 0;

        UNSAFE.putInt(array, ARRAY_BYTE_BASE_OFFSET + offset, WORD_TRUE);
        return offset + LENGTH_OF_TRUE;
    }

    public static int formatFalse(final @Nonnull byte[] array, final @Nonnegative int offset) {
        // Preconditions:
        // assert array != null;
        // assert offset >= 0;

        UNSAFE.putInt(array, ARRAY_BYTE_BASE_OFFSET + offset, WORD_FALSE_1);
        UNSAFE.putByte(array, ARRAY_BYTE_BASE_OFFSET + SIZE_OF_INT + offset, WORD_FALSE_2);

        return offset + LENGTH_OF_FALSE;
    }

    public static int formatByte(final byte b, final @Nonnull byte[] array, final @Nonnegative int offset) {
        // Preconditions:
        // assert array != null;
        // assert offset >= 0;

        UNSAFE.putByte(array, ARRAY_BYTE_BASE_OFFSET + offset, b);
        return offset + LENGTH_OF_BYTE;
    }

    public static int formatByte(final byte b, final @Nonnull MutableBuffer buffer, final @Nonnegative int offset) {
        // Preconditions:
        // assert buffer != null;
        // assert offset >= 0;

        buffer.putByte(offset, b);
        return offset + LENGTH_OF_BYTE;
    }

    public static int formatBytes(final @Nonnull byte[] bytes,
                                  final @Nonnegative int bytesOffset,
                                  final @Nonnegative int bytesLength,
                                  final @Nonnull byte[] array,
                                  final @Nonnegative int offset) {
        // Preconditions:
        // assert bytes != null;
        // assert bytesOffset >= 0 && bytesLength >= 0 && bytesOffset + bytesLength <= bytes.length;
        // assert array != null;
        // assert offset >= 0;

        UNSAFE.copyMemory(
                bytes,
                ARRAY_BYTE_BASE_OFFSET + bytesOffset,
                array,
                ARRAY_BYTE_BASE_OFFSET + offset,
                bytesLength
        );

        return offset + bytesLength;
    }

    public static int formatBytes(final @Nonnull Buffer bytes,
                                  final @Nonnegative int bytesOffset,
                                  final @Nonnegative int bytesLength,
                                  final @Nonnull byte[] array,
                                  final @Nonnegative int offset) {
        // Preconditions:
        // assert bytes != null;
        // assert bytesOffset >= 0 && bytesLength >= 0 && bytesOffset + bytesLength <= bytes.capacity();
        // assert array != null;
        // assert offset >= 0;

        bytes.getBytes(bytesOffset, array, offset, bytesLength);
        return offset + bytesLength;
    }

    public static int formatBytes(final @Nonnull byte[] bytes,
                                  final @Nonnegative int bytesOffset,
                                  final @Nonnegative int bytesLength,
                                  final @Nonnull MutableBuffer buffer,
                                  final @Nonnegative int offset) {
        // Preconditions:
        // assert bytes != null;
        // assert bytesOffset >= 0 && bytesLength >= 0 && bytesOffset + bytesLength <= bytes.length;
        // assert buffer != null;
        // assert offset >= 0;

        buffer.putBytes(offset, bytes, bytesOffset, bytesLength);
        return offset + bytesLength;
    }

    public static int formatBytes(final @Nonnull Buffer bytes,
                                  final @Nonnull MutableBuffer buffer,
                                  final @Nonnegative int offset) {
        // Preconditions:
        // assert bytes != null;
        // assert bytesOffset >= 0 && bytesLength >= 0 && bytesOffset + bytesLength <= bytes.capacity();
        // assert array != null;
        // assert offset >= 0;

        buffer.putBytes(offset, bytes);
        return offset + bytes.capacity();
    }

    public static int formatBytes(final @Nonnull Buffer bytes,
                                  final @Nonnegative int bytesOffset,
                                  final @Nonnegative int bytesLength,
                                  final @Nonnull MutableBuffer buffer,
                                  final @Nonnegative int offset) {
        // Preconditions:
        // assert bytes != null;
        // assert bytesOffset >= 0 && bytesLength >= 0 && bytesOffset + bytesLength <= bytes.capacity();
        // assert array != null;
        // assert offset >= 0;

        buffer.putBytes(offset, bytes, bytesOffset, bytesLength);
        return offset + bytesLength;
    }

    public static int formatUtf8Char(final char c, final @Nonnull byte[] array, @Nonnegative int offset) {
        // Preconditions:
        // assert array != null;
        // assert offset >= 0;

        if (c <= 0x007F) {
            UNSAFE.putByte(array, ARRAY_BYTE_BASE_OFFSET + offset++, (byte) c);
        } else if (c <= 0x07FF) {
            UNSAFE.putByte(array, ARRAY_BYTE_BASE_OFFSET + offset++, (byte) (0b11000000 | (c >> 6)));
            UNSAFE.putByte(array, ARRAY_BYTE_BASE_OFFSET + offset++, (byte) (0b10000000 | (c & 0b111111)));
        } else if (!Character.isSurrogate(c)) {
            UNSAFE.putByte(array, ARRAY_BYTE_BASE_OFFSET + offset++, (byte) (0b11100000 | (c >> 12)));
            UNSAFE.putByte(array, ARRAY_BYTE_BASE_OFFSET + offset++, (byte) (0b10000000 | ((c >> 6) & 0b111111)));
            UNSAFE.putByte(array, ARRAY_BYTE_BASE_OFFSET + offset++, (byte) (0b10000000 | (c & 0b111111)));
        } else {
            throw new IllegalArgumentException("surrogate is not supported");
        }

        return offset;
    }


    public static int formatAsciiString(final @Nonnull String value,
                                        final @Nonnull byte[] array,
                                        final @Nonnegative int offset) {
        // Preconditions:
        // assert value != null;
        // assert array != null;
        // assert offset >= 0;

        final int length = value.length();

        for (int i = 0; i < length; i++) {
            UNSAFE.putByte(array, ARRAY_BYTE_BASE_OFFSET + i + offset, (byte) (value.charAt(i) & 0x7F));
        }

        return offset + length;
    }

    public static int formatAsciiString(final @Nonnull String value,
                                        @Nonnegative int start,
                                        final @Nonnegative int end,
                                        final @Nonnull byte[] array,
                                        @Nonnegative int offset) {
        // Preconditions:
        // assert value != null;
        // assert start <= end && start >=0 && end >= 0;
        // assert array != null;
        // assert offset >= 0;

        while (start < end) {
            UNSAFE.putByte(array, ARRAY_BYTE_BASE_OFFSET + offset, (byte) (value.charAt(start) & 0x7F));
            start++;
            offset++;
        }

        return offset;
    }

    public static int formatUtf8String(final @Nonnull String value,
                                       final @Nonnull byte[] array,
                                       @Nonnegative int offset) {
        // Preconditions:
        // assert value != null;
        // assert array != null;
        // assert offset >= 0;

        for (int i = 0, length = value.length(); i < length; i++) {
            final char c = value.charAt(i);

            if (c <= 0x007F) {
                array[offset++] = (byte) c;
            } else if (c <= 0x07FF) {
                array[offset++] = (byte) (0b11000000 | c >> 6);
                array[offset++] = (byte) (0b10000000 | c & 0b111111);
            } else if (!Character.isSurrogate(c)) {
                array[offset++] = (byte) (0b11100000 | c >> 12);
                array[offset++] = (byte) (0b10000000 | c >> 6 & 0b111111);
                array[offset++] = (byte) (0b10000000 | c & 0b111111);
            } else {
                final int code = codePointAt(value, i++, length);
                array[offset++] = (byte) (0b11110000 | code >> 18);
                array[offset++] = (byte) (0b10000000 | code >> 12 & 0b111111);
                array[offset++] = (byte) (0b10000000 | code >> 6 & 0b111111);
                array[offset++] = (byte) (0b10000000 | code & 0b111111);
            }
        }

        return offset;
    }

    public static int formatUtf8String(final @Nonnull String value,
                                       @Nonnegative int start,
                                       final @Nonnegative int end,
                                       final @Nonnull byte[] array,
                                       @Nonnegative int offset) {
        // Preconditions:
        // assert value != null;
        // assert start <= end && start >=0 && end >= 0;
        // assert array != null;
        // assert offset >= 0;

        for (; start < end; start++) {
            final char c = value.charAt(start);

            if (c <= 0x007F) {
                array[offset++] = (byte) c;
            } else if (c <= 0x07FF) {
                array[offset++] = (byte) (0b11000000 | c >> 6);
                array[offset++] = (byte) (0b10000000 | c & 0b111111);
            } else if (!Character.isSurrogate(c)) {
                array[offset++] = (byte) (0b11100000 | c >> 12);
                array[offset++] = (byte) (0b10000000 | c >> 6 & 0b111111);
                array[offset++] = (byte) (0b10000000 | c & 0b111111);
            } else {
                final int code = codePointAt(value, start++, end);
                array[offset++] = (byte) (0b11110000 | code >> 18);
                array[offset++] = (byte) (0b10000000 | code >> 12 & 0b111111);
                array[offset++] = (byte) (0b10000000 | code >> 6 & 0b111111);
                array[offset++] = (byte) (0b10000000 | code & 0b111111);
            }
        }

        return offset;
    }

    public static int formatAsciiCharSequence(final @Nonnull CharSequence value,
                                              final @Nonnull byte[] array,
                                              final @Nonnegative int offset) {
        // Preconditions:
        // assert value != null;
        // assert array != null;
        // assert offset >= 0;

        final int length = value.length();

        for (int i = 0; i < length; i++) {
            UNSAFE.putByte(array, ARRAY_BYTE_BASE_OFFSET + i + offset, (byte) (value.charAt(i) & 0x7F));
        }

        return offset + length;
    }

    public static int formatAsciiCharSequence(final @Nonnull CharSequence value,
                                              @Nonnegative int start,
                                              final @Nonnegative int end,
                                              final @Nonnull byte[] array,
                                              @Nonnegative int offset) {
        // Preconditions:
        // assert value != null;
        // assert start <= end && start >=0 && end >= 0;
        // assert array != null;
        // assert offset >= 0;

        while (start < end) {
            UNSAFE.putByte(array, ARRAY_BYTE_BASE_OFFSET + offset, (byte) (value.charAt(start) & 0x7F));
            start++;
            offset++;
        }

        return offset;
    }

    public static int formatUtf8CharSequence(final @Nonnull CharSequence value,
                                             final @Nonnull byte[] array,
                                             @Nonnegative int offset) {
        // Preconditions:
        // assert value != null;
        // assert array != null;
        // assert offset >= 0;

        for (int i = 0, length = value.length(); i < length; i++) {
            final char c = value.charAt(i);

            if (c <= 0x007F) {
                array[offset++] = (byte) c;
            } else if (c <= 0x07FF) {
                array[offset++] = (byte) (0b11000000 | c >> 6);
                array[offset++] = (byte) (0b10000000 | c & 0b111111);
            } else if (!Character.isSurrogate(c)) {
                array[offset++] = (byte) (0b11100000 | c >> 12);
                array[offset++] = (byte) (0b10000000 | c >> 6 & 0b111111);
                array[offset++] = (byte) (0b10000000 | c & 0b111111);
            } else {
                final int code = codePointAt(value, i++, length);
                array[offset++] = (byte) (0b11110000 | code >> 18);
                array[offset++] = (byte) (0b10000000 | code >> 12 & 0b111111);
                array[offset++] = (byte) (0b10000000 | code >> 6 & 0b111111);
                array[offset++] = (byte) (0b10000000 | code & 0b111111);
            }
        }

        return offset;
    }

    public static int formatUtf8CharSequence(final @Nonnull CharSequence value,
                                             @Nonnegative int start,
                                             final @Nonnegative int end,
                                             final @Nonnull byte[] array,
                                             @Nonnegative int offset) {
        // Preconditions:
        // assert value != null;
        // assert start <= end && start >=0 && end >= 0;
        // assert array != null;
        // assert offset >= 0;

        for (; start < end; start++) {
            final char c = value.charAt(start);

            if (c <= 0x007F) {
                array[offset++] = (byte) c;
            } else if (c <= 0x07FF) {
                array[offset++] = (byte) (0b11000000 | c >> 6);
                array[offset++] = (byte) (0b10000000 | c & 0b111111);
            } else if (!Character.isSurrogate(c)) {
                array[offset++] = (byte) (0b11100000 | c >> 12);
                array[offset++] = (byte) (0b10000000 | c >> 6 & 0b111111);
                array[offset++] = (byte) (0b10000000 | c & 0b111111);
            } else {
                final int code = codePointAt(value, start++, end);
                array[offset++] = (byte) (0b11110000 | code >> 18);
                array[offset++] = (byte) (0b10000000 | code >> 12 & 0b111111);
                array[offset++] = (byte) (0b10000000 | code >> 6 & 0b111111);
                array[offset++] = (byte) (0b10000000 | code & 0b111111);
            }
        }

        return offset;
    }

    public static int formatAsciiCharSequence(final @Nonnull CharSequence value,
                                              final @Nonnull MutableBuffer buffer,
                                              final @Nonnegative int offset) {
        // Preconditions:
        // assert value != null;
        // assert buffer != null;
        // assert offset >= 0;

        final int length = value.length();

        for (int i = 0; i < length; i++) {
            buffer.putByte(offset + i, (byte) (value.charAt(i) & 0x7F));
        }

        return offset + length;
    }

    // endregion

    // region Int

    public static int formatInt(int value, final @Nonnull byte[] array, @Nonnegative int offset) {
        // Preconditions:
        // assert array != null;
        // assert offset >= 0;

        if (value < 0) {
            if (value == Integer.MIN_VALUE) {
                return formatIntMinValue(array, offset);
            }

            value = -value;
            offset = formatByte(MINUS, array, offset);
        }

        return formatUInt(value, array, offset);
    }

    public static int formatInt(int value, final @Nonnull MutableBuffer buffer, @Nonnegative int offset) {
        // Preconditions:
        // assert buffer != null;
        // assert offset >= 0;

        if (value < 0) {
            if (value == Integer.MIN_VALUE) {
                return formatIntMinValue(buffer, offset);
            }

            value = -value;
            buffer.putByte(offset++, MINUS);
        }

        return formatUInt(value, buffer, offset);
    }

    public static int formatUInt(@Nonnegative int value, final @Nonnull byte[] array, @Nonnegative int offset) {
        // Preconditions:
        // assert value >= 0;
        // assert array != null;
        // assert offset >= 0;

        return formatUInt(value, lengthOfUInt(value), array, offset);
    }

    public static int formatUInt(@Nonnegative int value, final @Nonnull MutableBuffer buffer, @Nonnegative int offset) {
        // Preconditions:
        // assert value >= 0;
        // assert buffer != null;
        // assert offset >= 0;

        return formatUInt(value, lengthOfUInt(value), buffer, offset);
    }

    public static int formatUInt(@Nonnegative int value,
                                 final @Nonnegative int valueLength,
                                 final @Nonnull byte[] array,
                                 @Nonnegative int offset) {
        // Preconditions:
        // assert value >= 0;
        // assert valueLength >= 0;
        // assert array != null;
        // assert offset >= 0;

        final int end = offset + valueLength;
        final int limit = offset + 1;
        offset = end;

        while (offset > limit) {
            final int newValue = (int) (2748779070L * value >>> 38);
            final int remainder = value - newValue * 100;
            value = newValue;

            offset -= 2;
            final short digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (remainder << 1));
            UNSAFE.putShort(array, ARRAY_BYTE_BASE_OFFSET + offset, digits);
        }

        if (offset == limit) {
            UNSAFE.putByte(array, ARRAY_BYTE_BASE_OFFSET - 1 + offset, (byte) (value + ZERO));
        }

        return end;
    }

    public static int formatUInt(@Nonnegative int value,
                                 final @Nonnegative int valueLength,
                                 final @Nonnull MutableBuffer buffer,
                                 @Nonnegative int offset) {
        // Preconditions:
        // assert value >= 0;
        // assert valueLength >= 0;
        // assert buffer != null;
        // assert offset >= 0;

        final int end = offset + valueLength;
        final int limit = offset + 1;
        offset = end;

        while (offset > limit) {
            final int newValue = (int) (2748779070L * value >>> 38);
            final int remainder = value - newValue * 100;
            value = newValue;

            offset -= 2;
            final short digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (remainder << 1));
            buffer.putShort(offset, digits);
        }

        if (offset == limit) {
            buffer.putByte(offset - 1, (byte) (value + ZERO));
        }

        return end;
    }

    public static int formatUInt2Digits(final @Nonnegative int value,
                                        final @Nonnull byte[] array,
                                        final @Nonnegative int offset) {
        // Preconditions:
        // assert value >= 0 && value <= 99;
        // assert array != null;
        // assert offset >= 0;

        final short digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (value << 1));
        UNSAFE.putShort(array, ARRAY_BYTE_BASE_OFFSET + offset, digits);

        return offset + 2;
    }

    public static int formatUInt2Digits(final @Nonnegative int value,
                                        final @Nonnull MutableBuffer buffer,
                                        final @Nonnegative int offset) {
        // Preconditions:
        // assert value >= 0 && value <= 99;
        // assert buffer != null;
        // assert offset >= 0;

        final short digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (value << 1));
        buffer.putShort(offset, digits);
        return offset + 2;
    }

    public static int formatUInt3Digits(final @Nonnegative int value, final @Nonnull byte[] array, final @Nonnegative int offset) {
        // Preconditions:
        // assert value >= 0 && value <= 999
        // assert array != null;
        // assert offset >= 0;

        final int leftPart = (int) (2748779070L * (long) value >>> 38);
        final int rightPart = value - leftPart * 100;

        final byte leftDigit = (byte) (leftPart + ZERO);
        final short rightDigits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (rightPart << 1));

        UNSAFE.putByte(array, ARRAY_BYTE_BASE_OFFSET + offset, leftDigit);
        UNSAFE.putShort(array, ARRAY_BYTE_BASE_OFFSET + 1 + offset, rightDigits);

        return offset + 3;
    }

    public static int formatUInt3Digits(final @Nonnegative int value, final @Nonnull MutableBuffer buffer, final @Nonnegative int offset) {
        // Preconditions:
        // assert value >= 0 && value <= 999
        // assert buffer != null;
        // assert offset >= 0;

        final int leftPart = (int) (2748779070L * (long) value >>> 38);
        final int rightPart = value - leftPart * 100;

        final byte leftDigit = (byte) (leftPart + ZERO);
        final short rightDigits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (rightPart << 1));

        buffer.putByte(offset, leftDigit);
        buffer.putShort(offset + 1, rightDigits);

        return offset + 3;
    }

    public static int formatUInt4Digits(final @Nonnegative int value,
                                        final @Nonnull byte[] array,
                                        final @Nonnegative int offset) {
        // Preconditions:
        // assert value >= 0 && value <= 9999;
        // assert array != null;
        // assert offset >= 0;

        final int leftPart = (int) (2748779070L * (long) value >>> 38);
        final int rightPart = value - leftPart * 100;

        final short leftDigits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (leftPart << 1));
        final short rightDigits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (rightPart << 1));

        UNSAFE.putShort(array, ARRAY_BYTE_BASE_OFFSET + offset, leftDigits);
        UNSAFE.putShort(array, ARRAY_BYTE_BASE_OFFSET + 2 + offset, rightDigits);

        return offset + 4;
    }

    public static int formatUInt4Digits(final @Nonnegative int value,
                                        final @Nonnull MutableBuffer buffer,
                                        final @Nonnegative int offset) {
        // Preconditions:
        // assert value >= 0 && value <= 9999;
        // assert buffer != null;
        // assert offset >= 0;

        final int leftPart = (int) (2748779070L * (long) value >>> 38);
        final int rightPart = value - leftPart * 100;

        final short leftDigits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (leftPart << 1));
        final short rightDigits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (rightPart << 1));

        buffer.putShort(offset, leftDigits);
        buffer.putShort(offset + 2, rightDigits);

        return offset + 4;
    }

    public static int formatUInt8Digits(@Nonnegative int value,
                                        final @Nonnull byte[] array,
                                        final @Nonnegative int offset) {
        // Preconditions:
        // assert value >= 0 && value <= 99999999;
        // assert array != null;
        // assert offset >= 0;

        int newValue = (int) (2748779070L * (long) value >>> 38);
        int remainder = value - 100 * newValue;

        short digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (remainder << 1));
        UNSAFE.putShort(array, ARRAY_BYTE_BASE_OFFSET + 6 + offset, digits);

        value = (int) (2748779070L * (long) newValue >>> 38);
        remainder = newValue - 100 * value;

        digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (remainder << 1));
        UNSAFE.putShort(array, ARRAY_BYTE_BASE_OFFSET + 4 + offset, digits);

        newValue = (int) (2748779070L * (long) value >>> 38);
        remainder = value - 100 * newValue;

        digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (remainder << 1));
        UNSAFE.putShort(array, ARRAY_BYTE_BASE_OFFSET + 2 + offset, digits);

        digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (newValue << 1));
        UNSAFE.putShort(array, ARRAY_BYTE_BASE_OFFSET + offset, digits);

        return offset + 8;
    }

    public static int formatUInt8Digits(@Nonnegative int value,
                                        final @Nonnull MutableBuffer buffer,
                                        final @Nonnegative int offset) {
        // Preconditions:
        // assert value >= 0 && value <= 99999999;
        // assert buffer != null;
        // assert offset >= 0;

        int newValue = (int) (2748779070L * (long) value >>> 38);
        int remainder = value - 100 * newValue;

        short digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (remainder << 1));
        buffer.putShort(offset + 6, digits);

        value = (int) (2748779070L * (long) newValue >>> 38);
        remainder = newValue - 100 * value;

        digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (remainder << 1));
        buffer.putShort(offset + 4, digits);

        newValue = (int) (2748779070L * (long) value >>> 38);
        remainder = value - 100 * newValue;

        digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (remainder << 1));
        buffer.putShort(offset + 2, digits);

        digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (newValue << 1));
        buffer.putShort(offset, digits);

        return offset + 8;
    }

    public static int formatUInt9Digits(@Nonnegative int value,
                                        final @Nonnull byte[] array,
                                        final @Nonnegative int offset) {
        // Preconditions:
        // assert value >= 0 && value <= 999999999;
        // assert array != null;
        // assert offset >= 0;

        int newValue = (int) (2748779070L * (long) value >>> 38);
        int remainder = value - 100 * newValue;

        short digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (remainder << 1));
        UNSAFE.putShort(array, ARRAY_BYTE_BASE_OFFSET + 7 + offset, digits);

        value = (int) (2748779070L * (long) newValue >>> 38);
        remainder = newValue - 100 * value;

        digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (remainder << 1));
        UNSAFE.putShort(array, ARRAY_BYTE_BASE_OFFSET + 5 + offset, digits);

        newValue = (int) (2748779070L * (long) value >>> 38);
        remainder = value - 100 * newValue;

        digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (remainder << 1));
        UNSAFE.putShort(array, ARRAY_BYTE_BASE_OFFSET + 3 + offset, digits);

        value = (int) (2748779070L * (long) newValue >>> 38);
        remainder = newValue - 100 * value;

        digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (remainder << 1));
        UNSAFE.putShort(array, ARRAY_BYTE_BASE_OFFSET + 1 + offset, digits);
        UNSAFE.putByte(array, ARRAY_BYTE_BASE_OFFSET + offset, (byte) (value + ZERO));

        return offset + 9;
    }

    public static int formatUInt9Digits(@Nonnegative int value,
                                        final @Nonnull MutableBuffer buffer,
                                        final @Nonnegative int offset) {
        // Preconditions:
        // assert value >= 0 && value <= 999999999;
        // assert buffer != null;
        // assert offset >= 0;

        int newValue = (int) (2748779070L * (long) value >>> 38);
        int remainder = value - 100 * newValue;

        short digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (remainder << 1));
        buffer.putShort(offset + 7, digits);

        value = (int) (2748779070L * (long) newValue >>> 38);
        remainder = newValue - 100 * value;

        digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (remainder << 1));
        buffer.putShort(offset + 5, digits);

        newValue = (int) (2748779070L * (long) value >>> 38);
        remainder = value - 100 * newValue;

        digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (remainder << 1));
        buffer.putShort(offset + 3, digits);

        value = (int) (2748779070L * (long) newValue >>> 38);
        remainder = newValue - 100 * value;

        digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (remainder << 1));
        buffer.putShort(offset + 1, digits);
        buffer.putByte(offset, (byte) (value + ZERO));

        return offset + 9;
    }

    public static int formatIntMinValue(final @Nonnull byte[] array, final @Nonnegative int offset) {
        // Preconditions:
        // assert array != null;
        // assert offset >= 0;

        UNSAFE.putLong(array, ARRAY_BYTE_BASE_OFFSET + offset, WORD_INT_MIN_1);
        UNSAFE.putShort(array, ARRAY_BYTE_BASE_OFFSET + 8 + offset, WORD_INT_MIN_2);
        UNSAFE.putByte(array, ARRAY_BYTE_BASE_OFFSET + 10 + offset, WORD_INT_MIN_3);

        return offset + MAX_LENGTH_OF_INT;
    }

    public static int formatIntMinValue(final @Nonnull MutableBuffer buffer, final @Nonnegative int offset) {
        // Preconditions:
        // assert buffer != null;
        // assert offset >= 0;

        buffer.putLong(offset, WORD_INT_MIN_1);
        buffer.putShort(offset + 8, WORD_INT_MIN_2);
        buffer.putByte(offset + 10, WORD_INT_MIN_3);

        return offset + MAX_LENGTH_OF_INT;
    }

    // endregion

    // region Long

    public static int formatLong(long value, final @Nonnull byte[] array, @Nonnegative int offset) {
        // Preconditions:
        // assert array != null;
        // assert offset >= 0;

        if (value < 0) {
            if (value == Long.MIN_VALUE) {
                return formatLongMinValue(array, offset);
            }

            value = -value;
            offset = formatByte(MINUS, array, offset);
        }

        return formatULong(value, array, offset);
    }

    public static int formatLong(long value, final @Nonnull MutableBuffer buffer, @Nonnegative int offset) {
        // Preconditions:
        // assert buffer != null;
        // assert offset >= 0;

        if (value < 0) {
            if (value == Long.MIN_VALUE) {
                return formatLongMinValue(buffer, offset);
            }

            value = -value;
            buffer.putByte(offset++, MINUS);
        }

        return formatULong(value, buffer, offset);
    }

    public static int formatULong(@Nonnegative long value, final @Nonnull byte[] array, @Nonnegative int offset) {
        // Preconditions:
        // assert value >= 0;
        // assert array != null;
        // assert offset >= 0;

        if (value <= Integer.MAX_VALUE) {
            return formatUInt((int) value, array, offset);
        }

        final long leftPart = value / 10_000_000_000L;
        final long rightPart = value - 10_000_000_000L * leftPart;

        if (leftPart > 0) {
            offset = formatUInt((int) leftPart, array, offset);
        }

        return formatULong10Digits(rightPart, array, offset);
    }

    public static int formatULong(@Nonnegative long value,
                                  @Nonnegative int valueLength,
                                  final @Nonnull byte[] array,
                                  @Nonnegative int offset) {
        // Preconditions:
        // assert value >= 0;
        // assert array != null;
        // assert offset >= 0;
        // assert valueLength >= lengthOfULong(value);

        if (value <= Integer.MAX_VALUE) {
            return formatUInt((int) value, valueLength, array, offset);
        }

        final long leftPart = value / 10_000_000_000L;
        final long rightPart = value - 10_000_000_000L * leftPart;

        valueLength -= 10;

        offset = formatUInt((int) leftPart, valueLength, array, offset);
        offset = formatULong10Digits(rightPart, array, offset);

        return offset;
    }

    public static int formatULong(@Nonnegative long value,
                                  @Nonnegative int valueLength,
                                  final @Nonnull MutableBuffer buffer,
                                  @Nonnegative int offset) {
        // Preconditions:
        // assert value >= 0;
        // assert array != null;
        // assert offset >= 0;
        // assert valueLength >= lengthOfULong(value);

        if (value <= Integer.MAX_VALUE) {
            return formatUInt((int) value, valueLength, buffer, offset);
        }

        final long leftPart = value / 10_000_000_000L;
        final long rightPart = value - 10_000_000_000L * leftPart;

        valueLength -= 10;

        offset = formatUInt((int) leftPart, valueLength, buffer, offset);
        offset = formatULong10Digits(rightPart, buffer, offset);

        return offset;
    }

    public static int formatULong(@Nonnegative long value, final MutableBuffer buffer, @Nonnegative int offset) {
        // Preconditions:
        // assert value >= 0;
        // assert buffer != null;
        // assert offset >= 0;

        if (value <= Integer.MAX_VALUE) {
            return formatUInt((int) value, buffer, offset);
        }

        final long leftPart = value / 10_000_000_000L;
        final long rightPart = value - 10_000_000_000L * leftPart;

        if (leftPart > 0) {
            offset = formatUInt((int) leftPart, buffer, offset);
        }

        return formatULong10Digits(rightPart, buffer, offset);
    }

    public static int formatULong10Digits(@Nonnegative long value, final @Nonnull byte[] array, @Nonnegative int offset) {
        // Preconditions:
        // assert value >= 0 && value <= 9999999999;
        // assert array != null;
        // assert offset >= 0;

        final long left = value / 100_000_000;

        short digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (left << 1));
        UNSAFE.putShort(array, ARRAY_BYTE_BASE_OFFSET + offset, digits);

        long right = value - 100_000_000 * left;
        long rightNew = 2748779070L * right >>> 38;
        long rightRemainder = right - 100 * rightNew;

        digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (rightRemainder << 1));
        UNSAFE.putShort(array, ARRAY_BYTE_BASE_OFFSET + offset + 8, digits);

        right = 2748779070L * rightNew >>> 38;
        rightRemainder = rightNew - 100 * right;

        digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (rightRemainder << 1));
        UNSAFE.putShort(array, ARRAY_BYTE_BASE_OFFSET + offset + 6, digits);

        rightNew = (int) (2748779070L * right >>> 38);
        rightRemainder = right - 100 * rightNew;

        digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (rightRemainder << 1));
        UNSAFE.putShort(array, ARRAY_BYTE_BASE_OFFSET + offset + 4, digits);

        digits = UNSAFE.getShort(ADDRESS_OF_DIGITS_TABLE + (rightNew << 1));
        UNSAFE.putShort(array, ARRAY_BYTE_BASE_OFFSET + offset + 2, digits);

        return offset + 10;
    }

    public static int formatULong10Digits(@Nonnegative long value, final @Nonnull MutableBuffer buffer, @Nonnegative int offset) {
        // Preconditions:
        // assert value >= 0 && value <= 9999999999;
        // assert buffer != null;
        // assert offset >= 0;

        final long leftPart = value / 100_000_000;
        final long rightPart = value - 100_000_000 * leftPart;

        formatUInt2Digits((int) leftPart, buffer, offset);
        formatUInt8Digits((int) rightPart, buffer, offset + 2);

        return offset + 10;
    }

    public static int formatLongMinValue(final @Nonnull byte[] array, @Nonnegative final int offset) {
        // Preconditions:
        // assert array != null;
        // assert offset >= 0;

        UNSAFE.putLong(array, ARRAY_BYTE_BASE_OFFSET + offset, WORD_LONG_MIN_1);
        UNSAFE.putLong(array, ARRAY_BYTE_BASE_OFFSET + 8 + offset, WORD_LONG_MIN_2);
        UNSAFE.putInt(array, ARRAY_BYTE_BASE_OFFSET + 16 + offset, WORD_LONG_MIN_3);

        return offset + MAX_LENGTH_OF_LONG;
    }

    public static int formatLongMinValue(final @Nonnull MutableBuffer buffer, @Nonnegative final int offset) {
        // Preconditions:
        // assert buffer != null;
        // assert offset >= 0;

        buffer.putLong(offset, WORD_LONG_MIN_1);
        buffer.putLong(offset + 8, WORD_LONG_MIN_2);
        buffer.putInt(offset + 16, WORD_LONG_MIN_3);

        return offset + MAX_LENGTH_OF_LONG;
    }

    // endregion

    // region Double

    public static void verifyDouble(final double value) {
        if (Double.isNaN(value) || Math.abs(value) > MAX_VALUE_OF_DOUBLE) {
            throw new IllegalArgumentException("double is out of supported range " + value);
        }
    }

    public static void verifyDoublePrecision(final int precision) {
        if (toUnsignedLong(precision) > MAX_PRECISION_OF_DOUBLE) {
            throw new IllegalArgumentException("precision " + precision + " out of 0-9");
        }
    }

    public static int formatDouble(@Nonnegative double value, final @Nonnull byte[] array, @Nonnegative int offset) {
        // Preconditions:
        // assert !Double.isNaN(value) && Long.MIN_VALUE < value && value <= Long.MAX_VALUE;
        // assert array != 0;
        // assert offset >= 0;

        if (value < 0) {
            value = -value;
            offset = formatByte(MINUS, array, offset);
        }

        long integer = (long) value;
        int fractional = (int) Math.round((value - integer) * DOUBLE_MULTIPLIER);

        if (fractional >= DOUBLE_MULTIPLIER) {
            integer++;
            fractional -= DOUBLE_MULTIPLIER;
        }

        offset = formatULong(integer, array, offset);

        if (fractional > 0) {
            offset = formatFraction(fractional, array, offset);
        }

        return offset;
    }

    public static int formatDouble(@Nonnegative double value, final @Nonnull MutableBuffer buffer, @Nonnegative int offset) {
        // Preconditions:
        // assert !Double.isNaN(value) && Long.MIN_VALUE < value && value <= Long.MAX_VALUE;
        // assert buffer != 0;
        // assert offset >= 0;

        if (value < 0) {
            value = -value;
            buffer.putByte(offset++, MINUS);
        }

        long integer = (long) value;
        int fractional = (int) Math.round((value - integer) * DOUBLE_MULTIPLIER);

        if (fractional >= DOUBLE_MULTIPLIER) {
            integer++;
            fractional -= DOUBLE_MULTIPLIER;
        }

        offset = formatULong(integer, buffer, offset);

        if (fractional > 0) {
            offset = formatFraction(fractional, buffer, offset);
        }

        return offset;
    }

    public static int formatDouble(@Nonnegative double value,
                                   final @Nonnegative int precision,
                                   final @Nonnull byte[] array,
                                   @Nonnegative int offset) {
        // Preconditions:
        // assert !Double.isNaN(value) && Long.MIN_VALUE < value && value <= Long.MAX_VALUE;
        // assert precision >= 0 && precision <= 9
        // assert array != 0;
        // assert offset >= 0;

        if (value < 0) {
            value = -value;
            offset = formatByte(MINUS, array, offset);
        }

        final int multiplier = multiplierOfUInt(precision);
        long integer = (long) value;
        int fraction = (int) Math.round((value - integer) * multiplier);

        if (fraction >= multiplier) {
            integer++;
            fraction -= multiplier;
        }

        offset = formatULong(integer, array, offset);

        if (fraction > 0) {
            offset = formatFraction(fraction, precision, array, offset);
        }

        return offset;
    }

    public static int formatDouble(@Nonnegative double value,
                                   final @Nonnegative int precision,
                                   final @Nonnull MutableBuffer buffer,
                                   @Nonnegative int offset) {
        // Preconditions:
        // assert !Double.isNaN(value) && Long.MIN_VALUE < value && value <= Long.MAX_VALUE;
        // assert precision >= 0 && precision <= 9
        // assert buffer != 0;
        // assert offset >= 0;

        if (value < 0) {
            value = -value;
            buffer.putByte(offset++, MINUS);
        }

        final int multiplier = multiplierOfUInt(precision);
        long integer = (long) value;
        int fraction = (int) Math.round((value - integer) * multiplier);

        if (fraction >= multiplier) {
            integer++;
            fraction -= multiplier;
        }

        offset = formatULong(integer, buffer, offset);

        if (fraction > 0) {
            offset = formatFraction(fraction, precision, buffer, offset);
        }

        return offset;
    }

    // endregion

    // region Decimal

    public static int formatFraction(@Nonnegative int fraction, final @Nonnull byte[] array, @Nonnegative int offset) {
        // Preconditions:
        // assert fraction >= 0 && value < DECIMAL_MULTIPLIER;
        // assert array != null;
        // assert offset >= 0;

        int scale = DOUBLE_SCALE;

        while (fraction % 10 == 0) {
            fraction /= 10;
            scale--;
        }

        UNSAFE.putByte(array, ARRAY_BYTE_BASE_OFFSET + offset, DOT);
        return formatUInt(fraction, scale, array, ++offset);
    }

    public static int formatFraction(@Nonnegative int fraction, final @Nonnull MutableBuffer buffer, @Nonnegative int offset) {
        // Preconditions:
        // assert fraction >= 0 && value < DECIMAL_MULTIPLIER;
        // assert buffer != null;
        // assert offset >= 0;

        int scale = DOUBLE_SCALE;

        while (fraction % 10 == 0) {
            fraction /= 10;
            scale--;
        }

        buffer.putByte(offset++, DOT);
        return formatUInt(fraction, scale, buffer, offset);
    }

    public static int formatFraction(@Nonnegative int fraction,
                                     @Nonnegative int scale,
                                     final @Nonnull byte[] array,
                                     @Nonnegative int offset) {
        // Preconditions:
        // assert fraction >= 0 && value < DECIMAL_MULTIPLIER;
        // assert array != null;
        // assert offset >= 0;

        while (fraction % 10 == 0) {
            fraction /= 10;
            scale--;
        }

        UNSAFE.putByte(array, ARRAY_BYTE_BASE_OFFSET + offset, DOT);
        return formatUInt(fraction, scale, array, ++offset);
    }

    public static int formatFraction(@Nonnegative int fraction,
                                     @Nonnegative int scale,
                                     final @Nonnull MutableBuffer buffer,
                                     @Nonnegative int offset) {
        // Preconditions:
        // assert fraction >= 0 && value < DECIMAL_MULTIPLIER;
        // assert buffer != null;
        // assert offset >= 0;

        while (fraction % 10 == 0) {
            fraction /= 10;
            scale--;
        }

        buffer.putByte(offset++, DOT);
        return formatUInt(fraction, scale, buffer, offset);
    }

    // endregion

    // region Decimal64

    public static int formatDecimal64(final boolean sign,
                                      final int exponent,
                                      final @Nonnegative long significand,
                                      final @Nonnull byte[] array,
                                      @Nonnegative int offset) {
        // Preconditions:
        // assert exponent >= DECIMAL_64_EXPONENT_MIN_VALUE && exponent <= DECIMAL_64_EXPONENT_MAX_VALUE;
        // assert significand > 0 && significand <= DECIMAL_64_SIGNIFICAND_MAX_VALUE;
        // assert array != null;
        // assert offset >= 0;

        if (sign) {
            offset = formatByte(MINUS, array, offset);
        }

        if (exponent >= 0) {
            offset = formatULong(significand, array, offset);
            offset = formatZeros(exponent, array, offset);
        } else if (exponent > -DECIMAL_64_SIGNIFICAND_MAX_DIGITS) {
            final int fractionLength = -exponent;
            final long multiplier = multiplierOfULong(fractionLength);

            final long integer = significand / multiplier;
            final long fraction = significand - integer * multiplier;

            offset = formatULong(integer, array, offset);

            if (fraction > 0) {
                offset = formatByte(DOT, array, offset);
                offset = formatFraction(fraction, fractionLength, array, offset);
            }
        } else {
            offset = formatZeroWithDot(array, offset);
            offset = formatZeros(-exponent - DECIMAL_64_SIGNIFICAND_MAX_DIGITS, array, offset);
            offset = formatFraction(significand, DECIMAL_64_SIGNIFICAND_MAX_DIGITS, array, offset);
        }

        return offset;
    }

    public static int formatDecimal64(final boolean sign,
                                      final int exponent,
                                      final @Nonnegative long significand,
                                      final @Nonnull MutableBuffer buffer,
                                      @Nonnegative int offset) {
        // Preconditions:
        // assert exponent >= DECIMAL_64_EXPONENT_MIN_VALUE && exponent <= DECIMAL_64_EXPONENT_MAX_VALUE;
        // assert significand > 0 && significand <= DECIMAL_64_SIGNIFICAND_MAX_VALUE;
        // assert array != null;
        // assert offset >= 0;

        if (sign) {
            offset = formatByte(MINUS, buffer, offset);
        }

        if (exponent >= 0) {
            offset = formatULong(significand, buffer, offset);
            offset = formatZeros(exponent, buffer, offset);
        } else if (exponent > -DECIMAL_64_SIGNIFICAND_MAX_DIGITS) {
            final int fractionLength = -exponent;
            final long multiplier = multiplierOfULong(fractionLength);

            final long integer = significand / multiplier;
            final long fraction = significand - integer * multiplier;

            offset = formatULong(integer, buffer, offset);

            if (fraction > 0) {
                offset = formatByte(DOT, buffer, offset);
                offset = formatFraction(fraction, fractionLength, buffer, offset);
            }
        } else {
            offset = formatZeroWithDot(buffer, offset);
            offset = formatZeros(-exponent - DECIMAL_64_SIGNIFICAND_MAX_DIGITS, buffer, offset);
            offset = formatFraction(significand, DECIMAL_64_SIGNIFICAND_MAX_DIGITS, buffer, offset);
        }

        return offset;
    }

    public static int formatFraction(@Nonnegative long fraction,
                                     @Nonnegative int fractionLength,
                                     final @Nonnull byte[] array,
                                     @Nonnegative int offset) {
        // assert fraction > 0 && lengthOfULong(fraction) <= fractionLength
        // assert array != null;
        // assert offset >= 0;

        while (fraction % 10 == 0) {
            fraction /= 10;
            fractionLength--;
        }

        return formatULong(fraction, fractionLength, array, offset);
    }

    public static int formatFraction(@Nonnegative long fraction,
                                     @Nonnegative int fractionLength,
                                     final @Nonnull MutableBuffer buffer,
                                     @Nonnegative int offset) {
        // assert fraction > 0 && lengthOfULong(fraction) <= fractionLength
        // assert array != null;
        // assert offset >= 0;

        while (fraction % 10 == 0) {
            fraction /= 10;
            fractionLength--;
        }

        return formatULong(fraction, fractionLength, buffer, offset);
    }

    // region Alphanumeric

    public static int formatAlphanumeric(long value,
                                         final @Nonnull byte[] array,
                                         final @Nonnegative int offset) {
        // Preconditions:
        // assert array != null;
        // assert offset >= 0;

        final int length = lengthOfAlphanumeric(value);
        return formatAlphanumeric(value, length, array, offset);
    }

    public static int formatAlphanumeric(final long value,
                                         final @Nonnull MutableBuffer buffer,
                                         final @Nonnegative int offset) {
        // Preconditions:
        // assert buffer != null;
        // assert offset >= 0;

        final int length = lengthOfAlphanumeric(value);
        return formatAlphanumeric(value, length, buffer, offset);
    }

    public static int formatAlphanumeric(long value,
                                         final @Nonnegative int length,
                                         final @Nonnull byte[] array,
                                         @Nonnegative int offset) {
        // Preconditions:
        // assert length == lengthOfAlphanumeric(value)
        // assert array != null;
        // assert offset >= 0;

        value = Long.rotateLeft(value, 10);

        for (int i = 0; i < length; i++) {
            UNSAFE.putByte(array, ARRAY_BYTE_BASE_OFFSET + i + offset, (byte) ((value & 0x3F) + 0x20));
            value = Long.rotateLeft(value, 6);
        }

        return offset + length;
    }

    public static int formatAlphanumeric(long value,
                                         final @Nonnegative int length,
                                         final @Nonnull MutableBuffer buffer,
                                         @Nonnegative int offset) {
        // Preconditions:
        // assert length == lengthOfAlphanumeric(value)
        // assert buffer != null;
        // assert offset >= 0;

        value = Long.rotateLeft(value, 10);

        for (int i = 0; i < length; i++) {
            buffer.putByte(offset + i, (byte) ((value & 0x3F) + 0x20));
            value = Long.rotateLeft(value, 6);
        }

        return offset + length;
    }

    // endregion

    // region Timestamp, Date, Time (ms)

    public static void verifyTimestamp(final long timestamp) {
        if (timestamp < MIN_VALUE_OF_TIMESTAMP || timestamp > MAX_VALUE_OF_TIMESTAMP) {
            throw new IllegalArgumentException("timestamp " + timestamp + " is out of 1970-2100 years");
        }
    }

    public static int formatTimestamp(final long timestamp, final @Nonnull byte[] array, @Nonnegative int offset) {
        // Preconditions:
        // assert MIN_VALUE_OF_TIMESTAMP <= timestamp && timestamp <= MAX_VALUE_OF_TIMESTAMP;
        // assert array != null;
        // assert offset >= 0;

        offset = formatDate(timestamp, array, offset);
        offset = formatByte(T, array, offset);
        offset = formatTime(timestamp, array, offset);
        offset = formatByte(Z, array, offset);

        return offset;
    }

    public static int formatTimestamp(final long timestamp, final @Nonnull MutableBuffer buffer, @Nonnegative int offset) {
        // Preconditions:
        // assert MIN_VALUE_OF_TIMESTAMP <= timestamp && timestamp <= MAX_VALUE_OF_TIMESTAMP;
        // assert buffer != null;
        // assert offset >= 0;

        offset = formatDate(timestamp, buffer, offset);
        buffer.putByte(offset++, T);
        offset = formatTime(timestamp, buffer, offset);
        buffer.putByte(offset++, Z);

        return offset;
    }

    public static int formatDate(long timestamp, final @Nonnull byte[] array, @Nonnegative int offset) {
        // Preconditions:
        // assert MIN_VALUE_OF_TIMESTAMP <= timestamp && timestamp <= MAX_VALUE_OF_TIMESTAMP;
        // assert array != null;
        // assert offset >= 0;

        timestamp += TIMESTAMP_BASE_MS;

        int days = (int) (timestamp / DAY_MS);
        final int cycles4 = days / DAYS_IN_4_CYCLE;
        days -= cycles4 * DAYS_IN_4_CYCLE;

        final boolean leapYear = days < DAYS_IN_LEAP_YEAR;
        final long daysToMonthAddress = leapYear ? ADDRESS_OF_DAYS_TO_MONTH_LEAP_TABLE : ADDRESS_OF_DAYS_TO_MONTH_TABLE;
        final long monthToDaysAddress = leapYear ? ADDRESS_OF_MONTH_TO_DAYS_LEAP_TABLE : ADDRESS_OF_MONTH_TO_DAYS_TABLE;

        int year = TIMESTAMP_BASE_YEAR + (cycles4 << 2);
        final int yearsInCycle = (days - 1) / DAYS_IN_YEAR;

        year += yearsInCycle;
        days -= yearsInCycle * DAYS_IN_YEAR; // for ordinary year more than actual days on 1

        final int month = UNSAFE.getByte(daysToMonthAddress + days);
        final int day = days - UNSAFE.getShort(monthToDaysAddress + (month << 1));

        offset = formatUInt4Digits(year, array, offset);
        offset = formatByte(MINUS, array, offset);
        offset = formatUInt2Digits(month, array, offset);
        offset = formatByte(MINUS, array, offset);
        offset = formatUInt2Digits(day, array, offset);

        return offset;
    }

    public static int formatDate(long timestamp, final @Nonnull MutableBuffer buffer, @Nonnegative int offset) {
        // Preconditions:
        // assert MIN_VALUE_OF_TIMESTAMP <= timestamp && timestamp <= MAX_VALUE_OF_TIMESTAMP;
        // assert buffer != null;
        // assert offset >= 0;

        timestamp += TIMESTAMP_BASE_MS;

        int days = (int) (timestamp / DAY_MS);
        final int cycles4 = days / DAYS_IN_4_CYCLE;
        days -= cycles4 * DAYS_IN_4_CYCLE;

        final boolean leapYear = days < DAYS_IN_LEAP_YEAR;
        final long daysToMonthAddress = leapYear ? ADDRESS_OF_DAYS_TO_MONTH_LEAP_TABLE : ADDRESS_OF_DAYS_TO_MONTH_TABLE;
        final long monthToDaysAddress = leapYear ? ADDRESS_OF_MONTH_TO_DAYS_LEAP_TABLE : ADDRESS_OF_MONTH_TO_DAYS_TABLE;

        int year = TIMESTAMP_BASE_YEAR + (cycles4 << 2);
        final int yearsInCycle = (days - 1) / DAYS_IN_YEAR;

        year += yearsInCycle;
        days -= yearsInCycle * DAYS_IN_YEAR; // for ordinary year more than actual days on 1

        final int month = UNSAFE.getByte(daysToMonthAddress + days);
        final int day = days - UNSAFE.getShort(monthToDaysAddress + (month << 1));

        offset = formatUInt4Digits(year, buffer, offset);
        buffer.putByte(offset++, MINUS);
        offset = formatUInt2Digits(month, buffer, offset);
        buffer.putByte(offset++, MINUS);
        offset = formatUInt2Digits(day, buffer, offset);

        return offset;
    }

    public static int formatTime(final long timestamp, final @Nonnull byte[] array, @Nonnegative int offset) {
        // Preconditions:
        // assert MIN_VALUE_OF_TIMESTAMP <= timestamp && timestamp <= MAX_VALUE_OF_TIMESTAMP;
        // assert array != null;
        // assert offset >= 0;

        int ms = (int) (timestamp % DAY_MS);

        final int hour = ms / HOUR_MS;
        ms -= hour * HOUR_MS;

        final int minute = ms / MINUTE_MS;
        ms -= minute * MINUTE_MS;

        final int second = ms / SECOND_MS;
        ms -= second * SECOND_MS;

        offset = formatUInt2Digits(hour, array, offset);
        offset = formatByte(COLON, array, offset);

        offset = formatUInt2Digits(minute, array, offset);
        offset = formatByte(COLON, array, offset);

        offset = formatUInt2Digits(second, array, offset);
        offset = formatByte(DOT, array, offset);

        offset = formatUInt3Digits(ms, array, offset);
        return offset;
    }

    public static int formatTime(long timestamp, final @Nonnull MutableBuffer buffer, @Nonnegative int offset) {
        // Preconditions:
        // assert MIN_VALUE_OF_TIMESTAMP <= timestamp && timestamp <= MAX_VALUE_OF_TIMESTAMP;
        // assert buffer != null;
        // assert offset >= 0;

        int ms = (int) (timestamp % DAY_MS);

        final int hour = ms / HOUR_MS;
        ms -= hour * HOUR_MS;

        final int minute = ms / MINUTE_MS;
        ms -= minute * MINUTE_MS;

        final int second = ms / SECOND_MS;
        ms -= second * SECOND_MS;

        offset = formatUInt2Digits(hour, buffer, offset);
        buffer.putByte(offset++, COLON);

        offset = formatUInt2Digits(minute, buffer, offset);
        buffer.putByte(offset++, COLON);

        offset = formatUInt2Digits(second, buffer, offset);
        buffer.putByte(offset++, DOT);

        offset = formatUInt3Digits(ms, buffer, offset);
        return offset;
    }

    // endregion

    // region Timestamp, Date, Time (ns)

    public static void verifyTimestampNs(final long timestamp) {
        if (timestamp < MIN_VALUE_OF_TIMESTAMP_NS || timestamp > MAX_VALUE_OF_TIMESTAMP_NS) {
            throw new IllegalArgumentException("timestamp " + timestamp + " is out of 1970-2100 years");
        }
    }

    public static int formatTimestampNs(final long timestamp, final @Nonnull byte[] array, @Nonnegative int offset) {
        // Preconditions:
        // assert MIN_VALUE_OF_TIMESTAMP <= timestamp && timestamp <= MAX_VALUE_OF_TIMESTAMP;
        // assert array != null;
        // assert offset >= 0;

        offset = formatDateNs(timestamp, array, offset);
        offset = formatByte(T, array, offset);
        offset = formatTimeNs(timestamp, array, offset);
        offset = formatByte(Z, array, offset);

        return offset;
    }

    public static int formatDateNs(long timestamp, final @Nonnull byte[] array, @Nonnegative int offset) {
        // Preconditions:
        // assert MIN_VALUE_OF_TIMESTAMP <= timestamp && timestamp <= MAX_VALUE_OF_TIMESTAMP;
        // assert array != null;
        // assert offset >= 0;

        timestamp += TIMESTAMP_BASE_NS;

        int days = (int) (timestamp / DAY_NS);
        final int cycles4 = days / DAYS_IN_4_CYCLE;
        days -= cycles4 * DAYS_IN_4_CYCLE;

        final boolean leapYear = days < DAYS_IN_LEAP_YEAR;
        final long daysToMonthAddress = leapYear ? ADDRESS_OF_DAYS_TO_MONTH_LEAP_TABLE : ADDRESS_OF_DAYS_TO_MONTH_TABLE;
        final long monthToDaysAddress = leapYear ? ADDRESS_OF_MONTH_TO_DAYS_LEAP_TABLE : ADDRESS_OF_MONTH_TO_DAYS_TABLE;

        int year = TIMESTAMP_BASE_YEAR + (cycles4 << 2);
        final int yearsInCycle = (days - 1) / DAYS_IN_YEAR;

        year += yearsInCycle;
        days -= yearsInCycle * DAYS_IN_YEAR; // for ordinary year more than actual days on 1

        final int month = UNSAFE.getByte(daysToMonthAddress + days);
        final int day = days - UNSAFE.getShort(monthToDaysAddress + (month << 1));

        offset = formatUInt4Digits(year, array, offset);
        offset = formatByte(MINUS, array, offset);
        offset = formatUInt2Digits(month, array, offset);
        offset = formatByte(MINUS, array, offset);
        offset = formatUInt2Digits(day, array, offset);

        return offset;
    }

    public static int formatTimeNs(final long timestamp, final @Nonnull byte[] array, @Nonnegative int offset) {
        // Preconditions:
        // assert MIN_VALUE_OF_TIMESTAMP <= timestamp && timestamp <= MAX_VALUE_OF_TIMESTAMP;
        // assert array != null;
        // assert offset >= 0;

        long ns = timestamp % DAY_NS;

        final long hour = ns / HOUR_NS;
        ns -= hour * HOUR_NS;

        final long minute = ns / MINUTE_NS;
        ns -= minute * MINUTE_NS;

        final long second = ns / SECOND_NS;
        ns -= second * SECOND_NS;

        offset = formatUInt2Digits((int) hour, array, offset);
        offset = formatByte(COLON, array, offset);

        offset = formatUInt2Digits((int) minute, array, offset);
        offset = formatByte(COLON, array, offset);

        offset = formatUInt2Digits((int) second, array, offset);
        offset = formatByte(DOT, array, offset);

        offset = formatUInt9Digits((int) ns, array, offset);
        return offset;
    }

    // endregion

}
