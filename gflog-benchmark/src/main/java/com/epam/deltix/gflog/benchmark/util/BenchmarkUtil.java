package com.epam.deltix.gflog.benchmark.util;

import org.HdrHistogram.Histogram;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public final class BenchmarkUtil {

    public static final String TEMP_DIRECTORY = "tmp";

    public static final String ENCODING = System.getProperty("benchmark.encoding", "UTF-8"); // ASCII
    public static final String MESSAGE = System.getProperty("benchmark.message", "Hello world!");
    public static final String LOGGER = System.getProperty("benchmark.logger", "123456789012345678901234567890");
    public static final String THREAD = System.getProperty("benchmark.thread", "12345678901234567890123");

    public static final long WARMUP_S = Long.getLong("benchmark.warmup", 30);
    public static final long DURATION_S = Long.getLong("benchmark.duration", 60);
    public static final long INTERVAL_NS = Long.getLong("benchmark.interval", 10_000);

    public static final int THREADS = Integer.getInteger("benchmark.threads", 1);
    public static final int BATCH = Integer.getInteger("benchmark.batch", 1);

    // causes a burst on a context switch if enabled, otherwise the guaranteed interval is preserved between operations
    public static final boolean CATCHUP = Boolean.getBoolean("benchmark.catchup");
    public static final int AFFINITY_BASE = Integer.getInteger("benchmark.affinity.base", -1);
    public static final int AFFINITY_STEP = Integer.getInteger("benchmark.affinity.step", 1);

    public static final Exception EXCEPTION = newException(40);

    public static String generateTempFile(final String prefix) {
        return TEMP_DIRECTORY + "/" + prefix + "-" + UUID.randomUUID() + ".log";
    }

    public static void deleteTempDirectory() throws Exception {
        final Path directory = Paths.get(TEMP_DIRECTORY);

        if (Files.exists(directory)) {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    public static Histogram newHistogram() {
        return new Histogram(TimeUnit.SECONDS.toNanos(1), 3);
    }

    public static Exception newException(final int depth) {
        final Exception exception = new Exception("Fake exception");
        final StackTraceElement[] stack = new StackTraceElement[depth];

        for (int i = 0; i < depth; i++) {
            stack[i] = new StackTraceElement("com.some.fake.exception", "fakeMethod", null, -1);
        }

        exception.setStackTrace(stack);
        return exception;
    }

    public static void printHelp(final Map<String, Benchmark> benchmarks) {
        System.out.printf("No benchmarks specified to run. Sample: bench1 bench2 bench3.%n");
        printOptions();
        printBenchmarks(benchmarks);
    }

    public static void printOptions() {
        System.out.printf("%nProperties (jvm-opts):%n");
        System.out.printf("\tbenchmark.logger          %-35s (Logger name to log under)%n", LOGGER);
        System.out.printf("\tbenchmark.thread          %-35s (Thread name to log under)%n", THREAD);
        System.out.printf("\tbenchmark.warmup          %-35s (Warmup in seconds)%n", WARMUP_S);
        System.out.printf("\tbenchmark.duration        %-35s (Duration in seconds)%n", DURATION_S);
        System.out.printf("\tbenchmark.interval        %-35s (Interval between operations in nanoseconds)%n", INTERVAL_NS);
        System.out.printf("\tbenchmark.threads         %-35s (Logging threads)%n", THREADS);
        System.out.printf("\tbenchmark.catchup         %-35s (Instant catchup if lagging behind)%n", CATCHUP);
        System.out.printf("\tbenchmark.affinity.base   %-35s (Logging threads affinity base)%n", AFFINITY_BASE);
        System.out.printf("\tbenchmark.affinity.step   %-35s (Logging threads affinity step)%n", AFFINITY_STEP);
    }

    public static void printBenchmarks(final Map<String, Benchmark> benchmarks) {
        System.out.printf("%nBenchmarks (cmd-args):%n");
        for (final String benchmark : benchmarks.keySet()) {
            System.out.printf("\t%s%n", benchmark);
        }
    }

}
