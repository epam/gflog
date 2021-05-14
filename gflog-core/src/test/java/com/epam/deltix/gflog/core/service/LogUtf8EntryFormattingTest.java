package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.Decimal64Util;
import com.epam.deltix.gflog.TestUtil;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class LogUtf8EntryFormattingTest {

    private final StringBuilder expected = new StringBuilder();
    private final LogUtf8Entry entry = new LogUtf8Entry(">>", 64, 4096);

    private void verify() {
        Assert.assertEquals(expected.toString(), entry.substring());

        expected.setLength(0);
        entry.clear();
    }

    @Test
    public void testBoundaryValues() {
        final long timestamp = System.currentTimeMillis();

        expected.append(false).append('\n')
                .append(true).append('\n')
                .append(Integer.MIN_VALUE).append('\n')
                .append(Integer.MAX_VALUE).append('\n')
                .append(Long.MIN_VALUE).append('\n')
                .append(Long.MAX_VALUE).append('\n')
                .append(Double.NaN)
                .append(Double.POSITIVE_INFINITY)
                .append(Double.NEGATIVE_INFINITY)
                .append(TestUtil.formatTimestamp(timestamp)).append('\n')
                .append("1970-01-01T00:00:00.000Z").append('\n')
                .append("1988-05-03T21:32:55.393Z").append('\n')
                .append("CME").append("\n")
                .append("1").append("\n")
                .append("some three words", 5, 10).append('\n')
                .append((CharSequence) null).append('\n')
                .append(formatDouble(-3700874152965236736.0)).append('\n')
                .append("-0.000007567").append('\n')
                .append("0.000002465").append('\n')
                .append("0.0000000000000000000000000000000000000034305").append('\n');

        entry.append(false).append('\n')
                .append(true).append('\n')
                .append(Integer.MIN_VALUE).append('\n')
                .append(Integer.MAX_VALUE).append('\n')
                .append(Long.MIN_VALUE).append('\n')
                .append(Long.MAX_VALUE).append('\n')
                .append(Double.NaN)
                .append(Double.POSITIVE_INFINITY)
                .append(Double.NEGATIVE_INFINITY)
                .appendTimestamp(timestamp).append('\n')
                .appendTimestamp(0).append("\n")
                .appendTimestamp(578698375393L).append('\n')
                .appendAlphanumeric(TestUtil.parseAlphanumeric("CME")).append('\n')
                .append(0.9999999999).append("\n")
                .append("some three words", 5, 10).append('\n')
                .append((CharSequence) null).append('\n')
                .append(-3700874152965236736.0).append('\n')
                .append(-0.000007567).append('\n')
                .append(2.464622543623581E-6).append('\n')
                .appendDecimal64(Decimal64Util.parse("0.0000000000000000000000000000000000000034305")).append('\n');

        verify();
    }

    @Test
    public void testRandomValues() {
        for (int i = 0; i < 1_000_000; i++) {
            final String text = TestUtil.randomUtf8String(-1, 16);
            final String text2 = "        " + text + "        ";

            final int start = TestUtil.randomInt(0, 7);
            final int end = TestUtil.randomInt(text2.length() - 8, text2.length());

            final int intValue = TestUtil.randomIntWithLength(1, 10);
            final long longValue = TestUtil.randomLongWithLength(1, 19);

            final long timestampValue = TestUtil.randomTimestamp();
            final long alphanumericValue = TestUtil.randomAlphanumeric(-1, 10);

            final int precision = TestUtil.randomInt(0, 9);
            final long decimal64 = TestUtil.randomDecimal64();

            expected.append(text)
                    .append(text2, start, end)
                    .append((CharSequence) text)
                    .append(text2, start, end)
                    .append((Object) text)
                    .append(", int=").append(intValue)
                    .append(", long=").append(longValue)
                    .append(", timestamp=").append(TestUtil.formatTimestamp(timestampValue))
                    .append(", date=").append(TestUtil.formatDate(timestampValue))
                    .append(", time=").append(TestUtil.formatTime(timestampValue))
                    .append(", alphanumeric=").append(TestUtil.formatAlphanumeric(alphanumericValue))
                    .append(", double=").append(i)
                    .append(", doubleWithPrecision=").append(formatDouble(i, precision))
                    .append(", decimal64=");

            Decimal64Util.appendTo(decimal64, expected);

            entry.append(text)
                    .append(text2, start, end)
                    .append((CharSequence) text)
                    .append((CharSequence) text2, start, end)
                    .append((Object) text)
                    .append(", int=").append(intValue)
                    .append(", long=").append(longValue)
                    .append(", timestamp=").appendTimestamp(timestampValue)
                    .append(", date=").appendDate(timestampValue)
                    .append(", time=").appendTime(timestampValue)
                    .append(", alphanumeric=").appendAlphanumeric(alphanumericValue)
                    .append(", double=").append(i)
                    .append(", doubleWithPrecision=").append(i, precision)
                    .append(", decimal64=").appendDecimal64(decimal64);

            verify();
        }
    }

    @Test
    public void testDecimal64() {
        verifyDecimal64(Decimal64Util.NAN);
        verifyDecimal64(Decimal64Util.NEGATIVE_INFINITY);
        verifyDecimal64(Decimal64Util.POSITIVE_INFINITY);

        for (int i = 0; i < 1000000; i++) {
            final long decimal64 = TestUtil.randomDecimal64();
            verifyDecimal64(decimal64);
        }
    }

    @Test
    public void testCyclicException() {
        final RuntimeException one = new RuntimeException();
        final RuntimeException two = new RuntimeException(one);

        one.initCause(two);
        entry.append(one);
    }

    private void verifyDecimal64(long decimal64) {
        Decimal64Util.appendTo(decimal64, expected);
        entry.appendDecimal64(decimal64);

        verify();
    }

    private static String formatDouble(double value) {
        return formatDouble(value, 9);
    }

    private static String formatDouble(double value, int precision) {
        if (Double.isNaN(value)) {
            return "NaN";
        }

        String sign = "";
        if (value < 0) {
            sign = "-";
            value = -value;
        }

        if (value == Double.POSITIVE_INFINITY) {
            return sign + "Infinity";
        }

        return sign + new BigDecimal(value)
                .setScale(precision, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

}
