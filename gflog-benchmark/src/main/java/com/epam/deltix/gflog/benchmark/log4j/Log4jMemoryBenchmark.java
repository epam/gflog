package com.epam.deltix.gflog.benchmark.log4j;

import org.apache.logging.log4j.Logger;
import org.openjdk.jol.vm.VM;

import java.util.concurrent.ThreadLocalRandom;

import static com.epam.deltix.gflog.benchmark.log4j.Log4jBenchmarkUtil.cleanup;
import static com.epam.deltix.gflog.benchmark.log4j.Log4jBenchmarkUtil.prepare;
import static com.epam.deltix.gflog.benchmark.util.BenchmarkUtil.memoryFootprint;


public class Log4jMemoryBenchmark {

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

        System.out.println("Before: " + memoryFootprint(log));
        System.out.println();

        final StringBuilder message = new StringBuilder();

        for (int i = 0; i < 1000000; i++) {
            generate(message);
            log.info("Hello there: {}", message);
        }

        System.out.println("After: " + memoryFootprint(log));
    }

    private static void generate(final StringBuilder message) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final int max = maxSize(random);

        final int size = random.nextInt(0, max);
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
