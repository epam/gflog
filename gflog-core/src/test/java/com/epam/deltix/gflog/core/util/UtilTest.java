package com.epam.deltix.gflog.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;


public class UtilTest {

    @Test
    public void testUtf8Conversion() {
        verifyUtf8Conversion("1234567890", 11, "1234567890");
        verifyUtf8Conversion("1234567890", 10, "1234567890");
        verifyUtf8Conversion("1234567890", 9, "123456789");
        verifyUtf8Conversion("1234567890", 1, "1");
        verifyUtf8Conversion("1234567890", 0, "");

        verifyUtf8Conversion("123456789И", 12, "123456789И");
        verifyUtf8Conversion("123456789И", 11, "123456789И");
        verifyUtf8Conversion("123456789И", 10, "123456789");
        verifyUtf8Conversion("123456789И", 9, "123456789");

        verifyUtf8Conversion("ИИИИИИ", 11, "ИИИИИ");
        verifyUtf8Conversion("ИИИИИИ", 10, "ИИИИИ");
        verifyUtf8Conversion("ИИИИИИ", 9, "ИИИИ");
        verifyUtf8Conversion("ИИИИИИ", 1, "");
        verifyUtf8Conversion("ИИИИИИ", 0, "");

        verifyUtf8Conversion("在在在在", 11, "在在在");
        verifyUtf8Conversion("在在在在", 10, "在在在");
        verifyUtf8Conversion("在在在在", 9, "在在在");
        verifyUtf8Conversion("在在在在", 9, "在在在");
        verifyUtf8Conversion("在在在在", 2, "");
        verifyUtf8Conversion("在在在在", 1, "");
        verifyUtf8Conversion("在在在在", 0, "");
    }


    @Test
    public void testLimitUtf8Length() {
        byte[] bytes = "123".getBytes(StandardCharsets.UTF_8);

        verifyLimitUtf8Length("123", bytes, 0, 3, 3);
        verifyLimitUtf8Length("12", bytes, 0, 4, 2);
        verifyLimitUtf8Length("2", bytes, 1, 5, 1);
        verifyLimitUtf8Length("", bytes, 1, 1, 0);

        bytes = "ПРИВЕТ".getBytes(StandardCharsets.UTF_8);

        verifyLimitUtf8Length("ПРИВЕТ", bytes, 0, 12, 12);
        verifyLimitUtf8Length("ПРИВЕ", bytes, 0, 12, 11);
        verifyLimitUtf8Length("ПРИВЕ", bytes, 0, 12, 10);
        verifyLimitUtf8Length("РИВ", bytes, 2, Integer.MAX_VALUE, 7);
        verifyLimitUtf8Length("ИВ", bytes, 4, Integer.MAX_VALUE, 4);
        verifyLimitUtf8Length("И", bytes, 4, Integer.MAX_VALUE, 3);
        verifyLimitUtf8Length("И", bytes, 4, Integer.MAX_VALUE, 2);
        verifyLimitUtf8Length("", bytes, 4, Integer.MAX_VALUE, 1);
        verifyLimitUtf8Length("", bytes, 4, Integer.MAX_VALUE, 0);

        bytes = "在在".getBytes(StandardCharsets.UTF_8);
        verifyLimitUtf8Length("在在", bytes, 0, 6, 6);
        verifyLimitUtf8Length("在", bytes, 0, 6, 5);
        verifyLimitUtf8Length("在", bytes, 0, 6, 4);
        verifyLimitUtf8Length("在", bytes, 0, 6, 3);

        bytes = "\uD801\uDC37".getBytes(StandardCharsets.UTF_8);
        verifyLimitUtf8Length("\uD801\uDC37", bytes, 0, 4, 4);
        verifyLimitUtf8Length("", bytes, 0, 4, 3);
        verifyLimitUtf8Length("", bytes, 0, 4, 2);
        verifyLimitUtf8Length("", bytes, 0, 4, 1);
        verifyLimitUtf8Length("", bytes, 0, 4, 0);
    }

    private static void verifyUtf8Conversion(final String value, final int limit, final String expected) {
        final UnsafeBuffer buffer = Util.fromUtf8String(value, limit);
        Assert.assertEquals(expected, Util.toUtf8String(buffer));
    }

    private static void verifyLimitUtf8Length(final String expected,
                                              final byte[] bytes,
                                              final int offset,
                                              final int length,
                                              final int limit) {
        final MutableBuffer buffer = new UnsafeBuffer(bytes);

        final int newLength = Util.limitUtf8Length(buffer, offset, length, limit);
        final String actual = new String(bytes, offset, newLength, StandardCharsets.UTF_8);

        Assert.assertEquals(expected, actual);
    }

}
