package com.epam.deltix.gflog.core.util;

import com.epam.deltix.gflog.TestUtil;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;


public class FormattingTest {

    private static final BigDecimal[] MAX_ERROR = {
            BigDecimal.valueOf(0, 0),
            BigDecimal.valueOf(1, 1),
            BigDecimal.valueOf(1, 2),
            BigDecimal.valueOf(1, 3),
            BigDecimal.valueOf(1, 4),
            BigDecimal.valueOf(1, 5),
            BigDecimal.valueOf(1, 6),
            BigDecimal.valueOf(1, 7),
            BigDecimal.valueOf(1, 8),
            BigDecimal.valueOf(1, 9)
    };

    private final byte[] array = new byte[1024];
    private final MutableBuffer buffer = new UnsafeBuffer(new byte[1024]);

    @Test
    public void formatStrings() {
        verifyString("\uD801\uDC37");
        verifyString("\uD852\uDF62");

        for (int i = 0; i <= 1000000; i++) {
            char c1 = TestUtil.randomChar(0, Character.MAX_VALUE);
            char c2 = TestUtil.randomChar(0, Character.MAX_VALUE);

            if (Character.isSurrogate(c1) || Character.isSurrogate(c2)) {
                c1 = TestUtil.randomChar(Character.MIN_HIGH_SURROGATE, Character.MAX_HIGH_SURROGATE);
                c2 = TestUtil.randomChar(Character.MIN_LOW_SURROGATE, Character.MAX_LOW_SURROGATE);
            }


            final String value = c1 + "" + c2;
            verifyString(value);
        }
    }

    @Test
    public void formatInt() {
        int[] specialValues = {Integer.MIN_VALUE, Integer.MAX_VALUE};
        for (int value : specialValues) {
            verifyInt(value);
        }

        for (int i = 0; i < 1000000; i++) {
            final int random = TestUtil.randomUIntWithLength(7, 10);

            verifyInt(i);
            verifyInt(-i);

            verifyInt(random);
            verifyInt(-random);
        }
    }

    @Test
    public void formatLong() {
        long[] specialValues = {Long.MIN_VALUE, Long.MAX_VALUE};
        for (long value : specialValues) {
            verifyLong(value);
        }

        for (int i = 0; i < 1000000; i++) {
            final long random = TestUtil.randomULongWithLength(7, 19);

            verifyLong(i);
            verifyLong(-i);

            verifyLong(random);
            verifyLong(-random);
        }
    }

    @Test
    public void formatDouble() {
        double[] specialValues = {
                0.0, -0.0, 0.999999999999999
        };
        for (double value : specialValues) {
            verifyDouble(value);
        }

        for (int i = 0; i < 100000; i++) {
            final long integer = TestUtil.randomULongWithLength(1, 18);
            final long fraction = TestUtil.randomULongWithLength(1, 12);
            final double bigValue = integer + fraction / 1e13;
            final double lowValue = fraction / 1e13;

            verifyDouble(bigValue);
            verifyDouble(-bigValue);

            verifyDouble(lowValue);
            verifyDouble(-lowValue);
        }
    }

    @Test
    public void formatAlphanumeric() {
        for (int i = 0; i < 1000000; i++) {
            final long value = TestUtil.randomAlphanumeric(0, 10);
            assertFormatAlphanumeric(value);
        }
    }

    @Test
    public void formatTimestamp() {
        long[] specialValues = {Formatting.MIN_VALUE_OF_TIMESTAMP, Formatting.MAX_VALUE_OF_TIMESTAMP};
        for (long value : specialValues) {
            verifyTimestamp(value);
        }

        for (int i = 0; i < 1000000; i++) {
            final long timestamp = TestUtil.randomLong(Formatting.MIN_VALUE_OF_TIMESTAMP, Formatting.MAX_VALUE_OF_TIMESTAMP);
            verifyTimestamp(timestamp);
        }
    }

    @Test
    public void formatDate() {
        long[] specialValues = {Formatting.MIN_VALUE_OF_TIMESTAMP, Formatting.MAX_VALUE_OF_TIMESTAMP};
        for (long value : specialValues) {
            verifyDate(value);
        }

        for (int i = 0; i < 1000000; i++) {
            final long timestamp = TestUtil.randomLong(Formatting.MIN_VALUE_OF_TIMESTAMP, Formatting.MAX_VALUE_OF_TIMESTAMP);
            verifyDate(timestamp);
        }
    }

    @Test
    public void formatTime() {
        for (int i = 0; i < 1000000; i++) {
            final long timestamp = TestUtil.randomLong(Formatting.MIN_VALUE_OF_TIMESTAMP, Formatting.MAX_VALUE_OF_TIMESTAMP);
            verifyTime(timestamp);
        }
    }

    private void verifyString(final String value) {
        final int offset = TestUtil.randomInt(0, 64);

        int end = Formatting.formatUtf8String(value, array, offset);
        Assert.assertEquals("formatUtf8String", value, new String(array, offset, end - offset, StandardCharsets.UTF_8));

        end = Formatting.formatUtf8String(value, 0, value.length(), array, offset);
        Assert.assertEquals("formatUtf8String", value, new String(array, offset, end - offset, StandardCharsets.UTF_8));

        end = Formatting.formatUtf8CharSequence(value, array, offset);
        Assert.assertEquals("formatUtf8CharSequence", value, new String(array, offset, end - offset, StandardCharsets.UTF_8));

        end = Formatting.formatUtf8CharSequence(value, 0, value.length(), array, offset);
        Assert.assertEquals("formatUtf8CharSequence", value, new String(array, offset, end - offset, StandardCharsets.UTF_8));
    }

    private void verifyInt(final int value) {
        String expected = TestUtil.formatInt(value);

        final int offset = TestUtil.randomInt(0, 64);
        int end = Formatting.formatInt(value, array, offset);
        Assert.assertEquals("formatInt", expected, new String(array, offset, end - offset));

        end = Formatting.formatInt(value, buffer, offset);
        Assert.assertEquals("formatInt", expected, new String(buffer.byteArray(), offset, end - offset));
    }

    private void verifyLong(final long value) {
        final String expected = TestUtil.formatLong(value);

        final int offset = TestUtil.randomInt(0, 64);
        int end = Formatting.formatLong(value, array, offset);
        Assert.assertEquals("formatLong", expected, new String(array, offset, end - offset));

        end = Formatting.formatLong(value, buffer, offset);
        Assert.assertEquals("formatLong", expected, new String(buffer.byteArray(), offset, end - offset));
    }

    private void verifyDouble(final double value) {
        verifyDoubleWithDefaultPrecision(value);
        verifyDoubleWithPrecision(value, TestUtil.randomInt(0, Formatting.MAX_PRECISION_OF_DOUBLE));
    }

    private void verifyDoubleWithDefaultPrecision(double value) {
        Formatting.verifyDouble(value);

        final BigDecimal expected = new BigDecimal(value)
                .setScale(Formatting.MAX_PRECISION_OF_DOUBLE, RoundingMode.HALF_UP);

        final int offset = TestUtil.randomInt(0, 64);
        int end = Formatting.formatDouble(value, array, offset);
        final String actualString = new String(array, offset, end - offset);

        end = Formatting.formatDouble(value, buffer, offset);
        final String actualString2 = new String(buffer.byteArray(), offset, end - offset);
        Assert.assertEquals(actualString, actualString2);

        final BigDecimal actual = new BigDecimal(actualString)
                .setScale(Formatting.MAX_PRECISION_OF_DOUBLE, RoundingMode.HALF_UP);

        if (!expected.equals(actual) && expected.subtract(actual).abs().compareTo(MAX_ERROR[Formatting.MAX_PRECISION_OF_DOUBLE]) > 0) {
            Assert.assertEquals(expected.toPlainString(), actualString);
        }
    }

    private void verifyDoubleWithPrecision(double value, int precision) {
        Formatting.verifyDouble(value);

        final BigDecimal expected = new BigDecimal(value)
                .setScale(precision, RoundingMode.HALF_UP);

        final int offset = TestUtil.randomInt(0, 64);
        int end = Formatting.formatDouble(value, precision, array, offset);
        final String actualString = new String(array, offset, end - offset);

        end = Formatting.formatDouble(value, precision, buffer, offset);
        final String actualString2 = new String(buffer.byteArray(), offset, end - offset);
        Assert.assertEquals(actualString, actualString2);

        final BigDecimal actual = new BigDecimal(actualString)
                .setScale(precision, RoundingMode.HALF_UP);

        if (!expected.equals(actual) && expected.subtract(actual).abs().compareTo(MAX_ERROR[precision]) > 0) {
            Assert.assertEquals(expected.toPlainString(), actualString);
        }
    }

    private void assertFormatAlphanumeric(long value) {
        String expected = TestUtil.formatAlphanumeric(value);

        final int offset = TestUtil.randomInt(0, 64);
        int end = Formatting.formatAlphanumeric(value, array, offset);
        Assert.assertEquals("formatAlphanumeric", expected, new String(array, offset, end - offset));

        end = Formatting.formatAlphanumeric(value, buffer, offset);
        Assert.assertEquals("formatAlphanumeric", expected, new String(buffer.byteArray(), offset, end - offset));
    }

    private void verifyTimestamp(long timestamp) {
        final String expected = TestUtil.formatTimestamp(timestamp);

        final int offset = TestUtil.randomInt(0, 64);
        int end = Formatting.formatTimestamp(timestamp, array, offset);
        Assert.assertEquals("formatTimestamp", expected, new String(array, offset, end - offset));

        end = Formatting.formatTimestamp(timestamp, buffer, offset);
        Assert.assertEquals("formatTimestamp", expected, new String(buffer.byteArray(), offset, end - offset));

        end = Formatting.formatTimestampNs(timestamp * 1000_000, array, offset);
        Assert.assertEquals("formatTimestampNs", expected, new String(array, offset, end - offset).substring(0, 23) + "Z");
    }

    private void verifyDate(long timestamp) {
        final String expected = TestUtil.formatDate(timestamp);

        final int offset = TestUtil.randomInt(0, 64);
        int end = Formatting.formatDate(timestamp, array, offset);
        Assert.assertEquals("formatDate", expected, new String(array, offset, end - offset));

        end = Formatting.formatDate(timestamp, buffer, offset);
        Assert.assertEquals("formatDate", expected, new String(buffer.byteArray(), offset, end - offset));
    }

    private void verifyTime(long timestamp) {
        final String expected = TestUtil.formatTime(timestamp);

        final int offset = TestUtil.randomInt(0, 64);
        int end = Formatting.formatTime(timestamp, array, offset);
        Assert.assertEquals("formatTime", expected, new String(array, offset, end - offset));

        end = Formatting.formatTime(timestamp, buffer, offset);
        Assert.assertEquals("formatTime", expected, new String(buffer.byteArray(), offset, end - offset));
    }

}
