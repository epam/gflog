package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.TestUtil;
import com.epam.deltix.gflog.api.AppendableEntry;
import com.epam.deltix.gflog.api.Loggable;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;


public class LogUtf8EntryTruncationTest {

    @Test
    public void testAppendWithoutTruncation() {
        final LogUtf8Entry builder = new LogUtf8Entry(">>", 16, 256);

        builder.append(true)
                .append('1')
                .append(2)
                .append(3L)
                .append(4.0)
                .append(5.3, 0)
                .append("6")
                .append("00700", 2, 3)
                .append((LoggableObject) null)
                .append(new LoggableObject())
                .append((Object) null)
                .append((Object) "text-object")
                .append("Привет")
                .append((Throwable) null);

        Assert.assertEquals("true1234567nullLOGGABLEnulltext-objectПриветnull", builder.substring());
    }

    @Test
    public void testAppendWithTruncationAtZeroIndex() {
        final LogUtf8Entry builder = new LogUtf8Entry(">>", 16, 0);

        builder.clear();
        builder.append(true);
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.append('1');
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.append(2);
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.append(3L);
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.append(4.0);
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.append(5.3, 0);
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.append("6");
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.append((CharSequence) "6");
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.append("00700", 2, 3);
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.append("Привет");
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.append("在");
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.append("\uD801\uDC37");
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.append((LoggableObject) null);
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.append(new LoggableObject());
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.append((Object) null);
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.append((Object) "text-object");
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.append((Throwable) null);
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.appendAlphanumeric(TestUtil.randomAlphanumeric(1, 10));
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.appendTimestamp(ThreadLocalRandom.current().nextLong(0, System.currentTimeMillis()));
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.appendDate(ThreadLocalRandom.current().nextLong(0, System.currentTimeMillis()));
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.appendTime(ThreadLocalRandom.current().nextLong(0, System.currentTimeMillis()));
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.appendDecimal64(ThreadLocalRandom.current().nextLong());
        Assert.assertEquals(">>", builder.substring());
    }

    @Test
    public void testAppendWithTruncationAtFirstIndex() {
        final LogUtf8Entry builder = new LogUtf8Entry(">>", 16, 1);

        builder.clear();
        builder.append(true);
        Assert.assertEquals("t>>", builder.substring());

        builder.clear();
        builder.append(22);
        Assert.assertEquals("2>>", builder.substring());

        builder.clear();
        builder.append(33L);
        Assert.assertEquals("3>>", builder.substring());

        builder.clear();
        builder.append(44.0);
        Assert.assertEquals("4>>", builder.substring());

        builder.clear();
        builder.append(55.3, 0);
        Assert.assertEquals("5>>", builder.substring());

        builder.clear();
        builder.append("66");
        Assert.assertEquals("6>>", builder.substring());

        builder.clear();
        builder.append("Привет");
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.append("在");
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.append("\uD801\uDC37");
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.append("00700", 2, 4);
        Assert.assertEquals("7>>", builder.substring());

        builder.clear();
        builder.append((LoggableObject) null);
        Assert.assertEquals("n>>", builder.substring());

        builder.clear();
        builder.append(new LoggableObject());
        Assert.assertEquals("L>>", builder.substring());

        builder.clear();
        builder.append((Object) null);
        Assert.assertEquals("n>>", builder.substring());

        builder.clear();
        builder.append((Object) "text-object");
        Assert.assertEquals("t>>", builder.substring());

        builder.clear();
        builder.append((Throwable) null);
        Assert.assertEquals("n>>", builder.substring());

        builder.clear();
        builder.appendAlphanumeric(Long.MAX_VALUE);
        Assert.assertEquals("_>>", builder.substring());

        builder.clear();
        builder.appendTimestamp(0);
        Assert.assertEquals("1>>", builder.substring());

        builder.clear();
        builder.appendDate(1);
        Assert.assertEquals("1>>", builder.substring());

        builder.clear();
        builder.appendTime(2);
        Assert.assertEquals("0>>", builder.substring());

        builder.clear();
        builder.appendDecimal64(75920758972809457L);
        Assert.assertEquals("0>>", builder.substring());
    }

    @Test
    public void testAppendStringWithTruncation2() {
        final LogUtf8Entry builder = new LogUtf8Entry(">>", 16, 2);

        builder.clear();
        builder.append("12345");
        Assert.assertEquals("12>>", builder.substring());

        builder.clear();
        builder.append("123456", 1, 4);
        Assert.assertEquals("23>>", builder.substring());

        builder.clear();
        builder.append((CharSequence) "12345");
        Assert.assertEquals("12>>", builder.substring());


        builder.clear();
        builder.append((Object) "12345");
        Assert.assertEquals("12>>", builder.substring());

        builder.clear();
        builder.append("Привет");
        Assert.assertEquals("П>>", builder.substring());

        builder.clear();
        builder.append("在");
        Assert.assertEquals(">>", builder.substring());

        builder.clear();
        builder.append("\uD801\uDC37");
        Assert.assertEquals(">>", builder.substring());
    }

    @Test
    public void testAppendStringWithTruncation3() {
        final LogUtf8Entry builder = new LogUtf8Entry(">>", 16, 3);

        builder.clear();
        builder.append("Привет");
        Assert.assertEquals("П>>", builder.substring());

        builder.clear();
        builder.append("在");
        Assert.assertEquals("在", builder.substring());

        builder.clear();
        builder.append("\uD801\uDC37");
        Assert.assertEquals(">>", builder.substring());
    }

    @Test
    public void testAppendStringWithTruncation4() {
        final LogUtf8Entry builder = new LogUtf8Entry(">>", 16, 4);

        builder.clear();
        builder.append("在");
        Assert.assertEquals("在", builder.substring());

        builder.clear();
        builder.append("\uD801\uDC37");
        Assert.assertEquals("\uD801\uDC37", builder.substring());
    }

    private static final class LoggableObject implements Loggable {

        @Override
        public void appendTo(final AppendableEntry entry) {
            entry.append("LOGGABLE");
        }

    }
}
