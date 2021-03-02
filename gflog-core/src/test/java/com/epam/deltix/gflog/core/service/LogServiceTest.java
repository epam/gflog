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

    private static final int MESSAGES = 500 * 1000;
    private static final int LOGS = 1000;

    private final LogService service;
    private final Log[] logs;

    private final Producer[] producers;
    private final int[] sequence;

    public LogServiceTest(final int producerCount, final LogServiceFactory factory) {
        final VerifyingAppender appender = new VerifyingAppender();
        final Logger logger = new Logger(LogLevel.DEBUG, appender);

        service = factory.create(Collections.singletonList(logger), Collections.singletonList(appender));

        logs = new Log[LOGS];
        for (int log = 0; log < LOGS; log++) {
            final String logName = "log" + TestUtil.randomLongWithLength(1, 18);
            final LogInfo logInfo = service.register(logName, log);
            logs[log] = new Log(logName, logInfo);
        }

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

        final String logName = logs[logIndex].name;
        final long appenderMask = logs[logIndex].appenderMask[logLevel];

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
                    .commit();
        } else {
            service.claim(logIndex, logLevel, appenderMask, "Producer: %s. Logger: %s. Sequence: %s")
                    .with(producer)
                    .with(logName)
                    .with(sequence);
        }
    }

    private void verify(final LogRecord record) {
        final String message = Util.toUtf8String(record.getMessage());
        final String[] parts = split(message);

        if (parts.length != 3) {
            Assert.fail(message);
        }

        final int producer = Integer.parseUnsignedInt(parts[0].substring("Producer: ".length()));
        final String logger = parts[1].substring("Logger: ".length());
        final int number = Integer.parseUnsignedInt(parts[2].substring("Sequence: ".length()));

        Assert.assertEquals(logger, Util.toUtf8String(record.getLogName()));
        Assert.assertEquals(LogLevel.INFO, record.getLogLevel());
        Assert.assertEquals(sequence[producer]++, number);
        Assert.assertEquals(producers[producer].getName(), Util.toUtf8String(record.getThreadName()));
        Assert.assertTrue(record.getTimestamp() > 0);
    }

    private String[] split(final String message) {
        final ArrayList<String> parts = new ArrayList<>(3);

        for (int i = 0; ; ) {
            final int j = message.indexOf(". ", i);

            if (j < 0) {
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
            join(10000);
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
