package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.core.util.Util;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;


@RunWith(Parameterized.class)
public class LogBufferTest {

    private final LogBuffer buffer;
    private final Producer[] producers;

    @Parameterized.Parameters(name = "capacity={0}, producers={1}")
    public static Collection<?> parameters() {
        final int[] capacities = {64 * 1024, 1024 * 1024, 8 * 1024 * 1024};
        final int[] producers = {1, 2, 3, 4};

        final ArrayList<Object[]> parameters = new ArrayList<>();

        for (final int capacity : capacities) {
            for (final int producer : producers) {
                final Object[] oneCase = {capacity, producer};
                parameters.add(oneCase);
            }
        }

        return parameters;
    }

    public LogBufferTest(final int capacity, final int producers) {
        this.buffer = new LogBuffer(capacity);
        this.producers = IntStream.range(0, producers)
                .mapToObj(Producer::new)
                .toArray(Producer[]::new);
    }

    @Before
    public void initialize() {
        for (final Producer producer : producers) {
            producer.start();
        }
    }

    @After
    public void destroy() throws Exception {
        for (final Producer producer : producers) {
            producer.close();
        }
    }

    @Test
    public void producersShouldNotBlock() throws Exception {
        final LogBuffer.RecordHandler handler = (buffer, offset, length) -> {
            Assert.assertTrue(offset >= 0);
            Assert.assertTrue(length >= 0);
            Assert.assertTrue(offset + length >= 0);
        };

        final long deadline = System.currentTimeMillis() + 1000;
        while (System.currentTimeMillis() < deadline) {
            buffer.read(handler);
        }

        Thread.sleep(1000);
        buffer.unblock();
        Thread.sleep(1000);
    }

    private final class Producer extends Thread implements AutoCloseable {

        private final int number;

        private volatile boolean active = true;

        Producer(final int number) {
            super("producer-" + number);

            this.number = number;
        }

        @Override
        public void run() {
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            final LogBuffer.BackpressureCallback callback = () -> {
            };

            while (active) {
                final boolean claim = random.nextBoolean();
                final boolean commit = random.nextBoolean();

                final int length = random.nextInt(4, 4096);
                final int offset = claim ?
                        buffer.claim(length, callback) :
                        buffer.tryClaim(length);

                if (offset < 0) {
                    continue;
                }

                final long address = buffer.dataAddress();
                Util.UNSAFE.putInt(address + offset, 0);

                for (int index = 4; index < length; index++) {
                    Util.UNSAFE.putByte(address + offset + index, (byte) number);
                }

                if (commit) {
                    buffer.commit(offset, length);
                } else {
                    buffer.abort(offset, length);
                }
            }
        }

        @Override
        public void close() throws Exception {
            active = false;
            join(5000);
        }

    }

}
