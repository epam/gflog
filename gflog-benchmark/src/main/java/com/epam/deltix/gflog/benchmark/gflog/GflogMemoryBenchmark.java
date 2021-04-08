package com.epam.deltix.gflog.benchmark.gflog;

import com.epam.deltix.gflog.api.Log;
import com.epam.deltix.gflog.benchmark.util.BenchmarkUtil;
import org.openjdk.jol.vm.VM;

import java.util.concurrent.ThreadLocalRandom;

import static com.epam.deltix.gflog.benchmark.gflog.GflogBenchmarkUtil.cleanup;
import static com.epam.deltix.gflog.benchmark.gflog.GflogBenchmarkUtil.prepare;


public class GflogMemoryBenchmark {

    private static final int[] MESSAGES_LIMITS = {1_000_000, 10_000_000, 100_000_000};

    public static void main(final String[] args) throws Exception {
        prepare("noop", "UTF-8");

        try {
            run();
        } finally {
            cleanup();
        }
    }

    private static void run() {
        System.out.println(VM.current().details());

        final Log log = GflogBenchmarkUtil.getLog();
        log.info("Hello there!");

        System.out.println("Messages: 0. " + BenchmarkUtil.memoryFootprint(log));

        final StringBuilder message = new StringBuilder();
        int messages = 0;

        for (final int messagesLimit : MESSAGES_LIMITS) {
            for (; messages < messagesLimit; messages++) {
                generate(message);

                log.info("Hello there: %s, %s, %s, %s")
                        .with(message)
                        .with(generateInt())
                        .with(generateLong())
                        .with(generateChar());
            }

            System.out.println();
            System.out.println("Messages: " + messages + ". " + BenchmarkUtil.memoryFootprint(log));
        }
    }

    private static void generate(final StringBuilder message) {
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

    private static char generateChar() {
        return (char) ThreadLocalRandom.current().nextInt(' ', '~');
    }

    private static int generateInt() {
        return ThreadLocalRandom.current().nextInt();
    }

    private static long generateLong() {
        return ThreadLocalRandom.current().nextLong();
    }

}
