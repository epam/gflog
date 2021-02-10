package com.epam.deltix.gflog;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;


public class TestUtil {

    protected static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    protected static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS'Z'");
    protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd");
    protected static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private static final int[] INT_RANGE = {0, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000, Integer.MAX_VALUE};

    private static final long[] LONG_RANGE = {0, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000,
            10000000000L, 100000000000L, 1000000000000L, 10000000000000L, 100000000000000L, 1000000000000000L,
            10000000000000000L, 100000000000000000L, 1000000000000000000L, Long.MAX_VALUE};

    public static long randomDecimal64() {
        final long significand = randomLongWithLength(1, 16);
        final int exponent = randomInt(-351, 351);

        if (exponent == -351) {
            return Decimal64Util.NaN;
        }

        final String value = new BigDecimal(significand).movePointRight(exponent).toPlainString();
        return Decimal64Util.parse(value);
    }


    public static long randomTimestamp() {
        long timestamp = random().nextLong(-1000, 3153600000000L);
        return (timestamp < 0) ? Long.MIN_VALUE : timestamp;
    }

    public static long randomAlphanumeric(int beginLength, int endLength) {
        final int length = randomInt(beginLength, endLength);
        long value = Long.MIN_VALUE;

        if (length >= 0) {
            value = ((long) length << 60);

            for (int i = 0, shift = 54; i < length; i++, shift -= 6) {
                value |= (randomInt(0x20, 0x5F) - 0x20L) << shift;
            }
        }

        return value;
    }

    public static int randomULongWithLength(int length) {
        return randomUIntWithLength(length, length);
    }

    public static long randomLong() {
        return random().nextLong();
    }

    public static long randomLong(long min, long max) {
        return random().nextLong(min, max + 1);
    }

    public static long randomLongWithLength(int beginLength, int endLength) {
        final long value = randomULongWithLength(beginLength, endLength);
        return randomBoolean() ? -value : value;
    }

    public static long randomULongWithLength(int beginLength, int endLength) {
        final ThreadLocalRandom random = random();
        final int index = random.nextInt(beginLength, endLength + 1) - 1;
        final long low = LONG_RANGE[index];
        final long high = LONG_RANGE[index + 1];

        return random.nextLong(low, high);
    }

    public static double randomDouble() {
        return random().nextDouble(Double.MIN_VALUE, Double.MAX_VALUE);
    }

    public static double randomDouble(double lowInclusive, double highExclusive) {
        return random().nextDouble(lowInclusive, highExclusive);
    }

    public static int randomInt() {
        return random().nextInt();
    }

    public static int randomIntWithLength(int length) {
        final int value = randomUIntWithLength(length);
        return randomBoolean() ? -value : value;
    }

    public static int randomIntWithLength(int beginLength, int endLength) {
        final int value = randomUIntWithLength(beginLength, endLength);
        return randomBoolean() ? -value : value;
    }

    public static int randomUIntWithLength(int length) {
        return randomUIntWithLength(length, length);
    }

    public static int randomUIntWithLength(int beginLength, int endLength) {
        final ThreadLocalRandom random = random();
        final int index = random.nextInt(beginLength, endLength + 1) - 1;
        final int low = INT_RANGE[index];
        final int high = INT_RANGE[index + 1];

        return random.nextInt(low, high);
    }

    public static int randomInt(int min, int max) {
        return random().nextInt(min, max + 1);
    }

    public static float randomFloat() {
        return Float.intBitsToFloat(randomInt());
    }

    public static short randomShort() {
        return (short) randomInt();
    }

    public static byte randomByte() {
        return (byte) randomInt();
    }

    public static boolean randomBoolean() {
        return randomInt() < 0;
    }

    public static char randomChar() {
        return (char) randomInt();
    }

    public static char randomChar(final int min, final int max) {
        return (char) randomInt(min, max);
    }

    public static <E extends Enum<E>> E randomEnumValue(Class<E> clazz) {
        E[] constants = clazz.getEnumConstants();
        int index = random().nextInt(-1, constants.length);
        return index < 0 ? null : constants[index];
    }

    public static String randomAsciiString(final int fromLength, final int toLength) {
        final ThreadLocalRandom random = random();
        final int length = random.nextInt(fromLength, toLength + 1);

        if (length < 0) {
            return null;
        }

        final char[] chars = new char[length];

        for (int i = 0; i < length; i++) {
            chars[i] = (char) random.nextInt(0, 128);
        }

        return new String(chars);
    }

    public static String randomUtf8String(final int fromLength, final int toLength) {
        final ThreadLocalRandom random = random();
        final int length = random.nextInt(fromLength, toLength + 1);

        if (length < 0) {
            return null;
        }

        final char[] chars = new char[length];

        for (int i = 0; i < length; i++) {
            if (i + 1 < length && random.nextInt(0, 100) == 1) {
                chars[i] = (char) random.nextInt(Character.MIN_HIGH_SURROGATE, Character.MAX_HIGH_SURROGATE + 1);
                chars[++i] = (char) random.nextInt(Character.MIN_LOW_SURROGATE, Character.MAX_LOW_SURROGATE + 1);
                continue;
            }

            final char c = (char) random.nextInt(0, Character.MAX_VALUE + 1);
            chars[i] = Character.isSurrogate(c) ? 0 : c;
        }

        return new String(chars);
    }


    public static ThreadLocalRandom random() {
        return ThreadLocalRandom.current();
    }


    public static String formatInt(int value) {
        return Integer.toString(value);
    }

    public static String formatLong(long value) {
        return Long.toString(value);
    }


    public static String formatTimestamp(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(UTC_ZONE).format(TIMESTAMP_FORMATTER);
    }

    public static String formatDate(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(UTC_ZONE).format(DATE_FORMATTER);
    }

    public static String formatTime(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(UTC_ZONE).format(TIME_FORMATTER);
    }

    public static String formatAlphanumeric(long value) {
        if (value == Long.MIN_VALUE) {
            return "null";
        }

        int length = (int) (value >>> 60);
        byte[] array = new byte[length];

        for (int i = 0, shift = 54; i < length; i++, shift -= 6) {
            byte c = (byte) (((value >>> shift) & 0x3F) + 0x20);
            array[i] = c;
        }

        return new String(array);
    }

    public static long parseAlphanumeric(final String text) {
        if (text == null)
            return Long.MIN_VALUE;

        int length = text.length();
        if (length > 10)
            throw new IllegalArgumentException("Text \"" + text + "\" exceeds max length of alphanumeric text");

        long value = (long) length << 60L;

        for (int i = 0, shift = 54; i < length; i++, shift -= 6) {
            final char c = text.charAt(i);

            if (c < 0x20 | c > 0x5F) {
                throw new IllegalArgumentException("Text \"" + text + "\" contains non-alphanumeric char at " + i);
            }

            value |= (c - 0x20L) << shift;
        }

        return value;
    }
}
