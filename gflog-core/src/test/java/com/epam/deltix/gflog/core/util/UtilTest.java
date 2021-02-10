package com.epam.deltix.gflog.core.util;

import org.junit.Assert;
import org.junit.Test;


public class UtilTest {

    @Test
    public void test() {
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

    private void verifyUtf8Conversion(final String value, final int limit, final String expected) {
        final UnsafeBuffer buffer = Util.fromUtf8String(value, limit);
        Assert.assertEquals(expected, Util.toUtf8String(buffer));
    }

}
