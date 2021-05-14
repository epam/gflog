package com.epam.deltix.gflog.benchmark.util;

import java.util.concurrent.ThreadLocalRandom;


public final class Generator {

    private final StringBuilder reusable = new StringBuilder(4096);

    public StringBuilder nextMessage() {
        final StringBuilder instance = reusable;
        nextMessage(instance);
        return instance;
    }

    private static void nextMessage(final StringBuilder message) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final int max = maxSize(random);

        final int size = random.nextInt(0, max + 1);
        message.setLength(size);

        for (int i = 0; i < size; i++) {
            message.setCharAt(i, '0');
        }
    }

    private static int maxSize(final ThreadLocalRandom random) {
        final int percentile = random.nextInt(0, 100);

        if (percentile < 50) {
            return 100;
        }

        if (percentile < 70) {
            return 200;
        }

        if (percentile < 80) {
            return 300;
        }

        if (percentile < 90) {
            return 400;
        }

        if (percentile < 99) {
            return 500;
        }

        return 1000;
    }

}
