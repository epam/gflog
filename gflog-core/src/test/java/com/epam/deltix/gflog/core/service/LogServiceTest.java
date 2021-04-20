package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.TestUtil;
import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.LogInfo;
import com.epam.deltix.gflog.core.LogRecord;
import com.epam.deltix.gflog.core.Logger;
import com.epam.deltix.gflog.core.appender.Appender;
import com.epam.deltix.gflog.core.util.Util;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;


public abstract class LogServiceTest {

    private static final int MESSAGES = 1000 * 1000;
    private static final int LOGS = 100 * 1000;

    private final LogService service;
    private final Log[] logs;

    private final Producer[] producers;
    private final int[] sequence;

    public LogServiceTest(final int producerCount, final String entryEncoding, final LogServiceFactory factory) {
        final VerifyingAppender appender = new VerifyingAppender();
        final Logger logger = new Logger(LogLevel.DEBUG, appender);

        factory.setEntryEncoding(entryEncoding);
        service = factory.create(Collections.singletonList(logger), Collections.singletonList(appender));

        logs = new Log[LOGS];

        producers = new Producer[producerCount];
        for (int number = 0; number < producerCount; number++) {
            producers[number] = new Producer(number);
        }

        sequence = new int[producerCount];
    }

    @Before
    public void initialize() {
        service.open();

        for (final Producer producer : producers) {
            producer.start();
        }
    }

    @After
    public void destroy() throws Exception {
        for (final Producer producer : producers) {
            producer.close();
        }

        service.close();

        for (final int sequence : sequence) {
            Assert.assertEquals(MESSAGES, sequence);
        }
    }

    @Test
    public void test() {
    }

    private void log(final int producer, final int sequence) {
        final int logIndex = TestUtil.randomInt(0, LOGS - 1);
        final int logLevel = LogLevel.INFO.ordinal();

        final Log log = getOrCreateLog(logIndex);

        final String logName = log.name;
        final long appenderMask = log.appenderMask[logLevel];

        final int exceptionDepth = TestUtil.randomInt(0, 1000);
        final Throwable exception = (TestUtil.randomInt(0, 1000) == 0) ?
                newException(sequence, exceptionDepth) : null;

        if (TestUtil.randomInt(0, 256) == 7) {
            service.claim(logIndex, logLevel, appenderMask)
                    .append("Junk #")
                    .append(TestUtil.randomLong())
                    .abort();
        }

        if (TestUtil.randomBoolean()) {
            service.claim(logIndex, logLevel, appenderMask)
                    .append("Producer: ")
                    .append(producer)
                    .append(". ")
                    .append("Logger: ")
                    .append(logName)
                    .append(". ")
                    .append("Sequence: ")
                    .append(sequence)
                    .append(". ")
                    .append("Exception: ")
                    .append(exception)
                    .commit();
        } else {
            service.claim(logIndex, logLevel, appenderMask, "Producer: %s. Logger: %s. Sequence: %s. Exception: %s.")
                    .with(producer)
                    .with(logName)
                    .with(sequence)
                    .with(exception);
        }
    }

    private synchronized Log getOrCreateLog(final int logIndex) {
        Log log = logs[logIndex];

        if (log == null) {
            final String logName = "log" + TestUtil.randomLongWithLength(1, 18);
            final LogInfo logInfo = service.register(logName, logIndex);

            log = new Log(logName, logInfo);
            logs[logIndex] = log;
        }

        return log;
    }

    private Exception newException(final int sequence, final int depth) {
        if (depth <= 0) {
            return new Exception("my-exception: #" + sequence);
        }

        return newException(sequence, depth - 1);
    }

    private void verify(final LogRecord record) {
        final String message = Util.toUtf8String(record.getMessage());
        final String[] parts = split(message);

        if (parts.length != 4) {
            Assert.fail(message);
        }

        final int producer = Integer.parseUnsignedInt(parts[0].substring("Producer: ".length()));
        final String logger = parts[1].substring("Logger: ".length());
        final int number = Integer.parseUnsignedInt(parts[2].substring("Sequence: ".length()));
        final String exception = parts[3].substring("Exception: ".length());

        Assert.assertEquals(logger, Util.toUtf8String(record.getLogName()));
        Assert.assertEquals(LogLevel.INFO, record.getLogLevel());
        Assert.assertEquals(sequence[producer]++, number);
        Assert.assertEquals(producers[producer].getName(), Util.toUtf8String(record.getThreadName()));
        Assert.assertTrue(record.getTimestamp() > 0);

        if (!exception.startsWith("null")) {
            Assert.assertTrue(parts[3], parts[3].contains("my-exception: #" + number));
        }
    }

    private String[] split(final String message) {
        final ArrayList<String> parts = new ArrayList<>(4);

        for (int i = 0, c = 1; ; c++) {
            final int j = message.indexOf(". ", i);

            if (j < 0 || c == 4) {
                parts.add(message.substring(i));
                break;
            }

            parts.add(message.substring(i, j));
            i = j + 2;
        }

        return parts.toArray(new String[0]);
    }

    private static final class Log {

        private final String name;
        private final long[] appenderMask;

        Log(final String name, final LogInfo info) {
            this.name = name;
            this.appenderMask = info.getAppenderMask();
        }
    }

    private class Producer extends Thread implements AutoCloseable {

        private final int producer;
        private int messages;
        private volatile boolean active = true;

        Producer(final int producer) {
            super("Producer #" + producer);

            this.producer = producer;
        }

        @Override
        public void run() {
            while (active && messages < MESSAGES) {
                log(producer, messages++);
            }
        }

        @Override
        public void close() throws Exception {
            join(60000);

            if (isAlive()) {
                Assert.fail("Thread is still alive: " + this);
            }

            active = false;
        }

    }

    private class VerifyingAppender extends Appender {

        VerifyingAppender() {
            super("verifier", LogLevel.INFO);
        }

        @Override
        public void open() {
        }

        @Override
        public void close() {
        }

        @Override
        public int append(final LogRecord record) {
            verify(record);
            return 1;
        }

        @Override
        public int flush() {
            return 0;
        }

    }

}
