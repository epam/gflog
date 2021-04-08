package com.epam.deltix.gflog.benchmark.log4j;

import com.epam.deltix.gflog.benchmark.util.BenchmarkUtil;
import org.apache.logging.log4j.Logger;
import org.openjdk.jol.vm.VM;

import java.util.concurrent.ThreadLocalRandom;

import static com.epam.deltix.gflog.benchmark.log4j.Log4jBenchmarkUtil.cleanup;
import static com.epam.deltix.gflog.benchmark.log4j.Log4jBenchmarkUtil.prepare;


public class Log4jMemoryBenchmark {

    private static final int[] MESSAGES_LIMITS = {1_000_000, 10_000_000, 100_000_000};

    public static void main(final String[] args) {
        prepare("noop");

        try {
            run();
        } finally {
            cleanup();
        }
    }

    private static void run() {
        System.out.println(VM.current().details());

        final Logger log = Log4jBenchmarkUtil.getLogger();
        log.info("Hello there!");

        System.out.println("Messages: 0. " + BenchmarkUtil.memoryFootprint(log));

        final StringBuilder message = new StringBuilder();
        int messages = 0;

        for (int messagesLimit : MESSAGES_LIMITS) {
            for (; messages < messagesLimit; messages++) {
                generate(message);
                log.info("Hello there: {}", message);
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

}
