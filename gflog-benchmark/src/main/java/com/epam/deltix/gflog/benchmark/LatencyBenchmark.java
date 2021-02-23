package com.epam.deltix.gflog.benchmark;

import com.epam.deltix.gflog.api.Log;
import com.epam.deltix.gflog.api.LogFactory;
import com.epam.deltix.gflog.core.LogConfigurator;
import com.epam.deltix.gflog.core.idle.BusySpinIdleStrategy;
import com.epam.deltix.gflog.core.idle.IdleStrategy;
import net.openhft.affinity.Affinity;
import org.HdrHistogram.Histogram;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Shows safepoints: -XX:+UnlockDiagnosticVMOptions -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime
 * Shows details: -XX:+PrintSafepointStatistics -XX:PrintSafepointStatisticsCount=1
 * Adjusts interval: -XX:GuaranteedSafepointInterval=1200000
 */
public class LatencyBenchmark {

    private static final String TEMP_DIRECTORY = "tmp";
    private static final String TEMP_FILE = TEMP_DIRECTORY + "/latency-benchmark-" + UUID.randomUUID() + ".log";

    private static final String[] CONFIGS = {
            "noop",
            //"console-direct",
            //"console-wrapper",
            //"file",
            //"rolling-file"
    };

    // 100 bytes + (line separator 1-2)
    // 2020-10-01 14:25:58.310 INFO '123456789012345678901234567890' [12345678901234567890123] Hello world!

    private static final Exception EXCEPTION = newException(40);

    private static final String ENCODING = System.getProperty("benchmark.encoding", "UTF-8"); // ASCII
    private static final String MESSAGE = System.getProperty("benchmark.message", "Hello world!");
    private static final String LOGGER = System.getProperty("benchmark.logger", "123456789012345678901234567890");
    private static final String THREAD = System.getProperty("benchmark.thread", "12345678901234567890123");

    private static final long WARMUP_S = Long.getLong("benchmark.warmup", 30);
    private static final long DURATION_S = Long.getLong("benchmark.duration", 60);
    private static final long INTERVAL_NS = Long.getLong("benchmark.interval", 10_000);

    private static final int THREADS = Integer.getInteger("benchmark.threads", 1);
    private static final int BATCH = Integer.getInteger("benchmark.batch", 1);
    private static final int AFFINITY = Integer.getInteger("benchmark.affinity", Integer.MIN_VALUE);

    // causes a burst on a context switch if enabled, otherwise the guaranteed interval is preserved between operations
    private static final boolean CATCHUP = Boolean.getBoolean("benchmark.catchup");

    public static void main(final String[] args) throws Exception {
        run("baseline", LatencyBenchmark::baseline);
        run("timestamp", LatencyBenchmark::timestamp);

        runWithConfigs("entryWith1Arg", LatencyBenchmark::entryWith1Arg);
        runWithConfigs("entryWith5Args", LatencyBenchmark::entryWith5Args);
        //runWithConfigs("entryWithException", LatencyBenchmark::entryWithException);

        runWithConfigs("templateWith1Arg", LatencyBenchmark::templateWith1Arg);
        runWithConfigs("templateWith5Args", LatencyBenchmark::templateWith5Args);
        //runWithConfigs("templateWithException", LatencyBenchmark::templateWithException);
    }

    private static void runWithConfigs(final String prefix, final Runnable command) throws Exception {
        for (final String config : CONFIGS) {
            final String title = prefix + "-" + config;

            prepare(config);
            run(title, command);
            cleanup();
        }
    }

    private static void run(final String title, final Runnable command) throws Exception {
        final String affinity = AFFINITY < 0 ? "no" : (AFFINITY + "-" + (AFFINITY + THREADS - 1));
        System.out.printf("Benchmark: %s. Duration: %s s. Interval: %s ns. Threads: %s. Batch: %s. Affinity: %s.%n",
                title, DURATION_S, INTERVAL_NS, THREADS, BATCH, affinity);

        final AtomicBoolean active = new AtomicBoolean(true);
        final AtomicBoolean measure = new AtomicBoolean(false);

        final Runner[] runners = new Runner[THREADS];

        for (int i = 0; i < THREADS; i++) {
            final Runner runner = new Runner(command, active, measure, INTERVAL_NS, BATCH, AFFINITY + i);
            runner.start();

            runners[i] = runner;
        }

        System.gc();
        Thread.sleep(TimeUnit.SECONDS.toMillis(WARMUP_S));

        System.out.printf("Benchmark: %s. Measuring.%n", title);

        final long start = System.currentTimeMillis();
        measure.set(true);

        Thread.sleep(TimeUnit.SECONDS.toMillis(DURATION_S));

        active.set(false);
        final long end = System.currentTimeMillis();
        final long took = end - start;

        final Histogram histogram = newHistogram();

        for (final Runner runner : runners) {
            runner.join();
            histogram.add(runner.histogram);
        }

        System.out.printf("Benchmark: %s. Took: %s ms. Throughput: %.0f msg/s. Latency: %n",
                title, took, 1000.0 * histogram.getTotalCount() * BATCH / took);

        histogram.outputPercentileDistribution(System.out, (double) BATCH);
        System.out.println();
    }

    private static void prepare(final String config) throws Exception {
        final Properties properties = new Properties();
        properties.setProperty("temp-file", TEMP_FILE);
        properties.setProperty("encoding", ENCODING);

        final String configFile = "classpath:com/epam/deltix/gflog/benchmark/throughput-" + config + "-benchmark.xml";
        LogConfigurator.configure(configFile, properties);
    }

    private static void cleanup() throws Exception {
        LogConfigurator.unconfigure();
        deleteTempDirectory();
    }

    private static Histogram newHistogram() {
        return new Histogram(TimeUnit.SECONDS.toNanos(1), 3);
    }

    private static void deleteTempDirectory() throws Exception {
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

    private static Exception newException(final int depth) {
        if (depth <= 2) {
            return new Exception("Exception");
        }

        return newException(depth - 1);
    }

    private static void baseline() {
    }

    private static void timestamp() {
        System.currentTimeMillis();
    }

    private static void entryWith1Arg() {
        Holder.LOG.info().append(MESSAGE).commit();
    }

    private static void templateWith1Arg() {
        Holder.LOG.info(MESSAGE);
    }

    private static void entryWith5Args() {
        Holder.LOG.info()
                .append("Some array: [")
                .append("string").append(',')
                .append('c').append(',')
                .append(1234567).append(',')
                .append(12345678901234L).append(',')
                .append("string").append(']')
                .commit();
    }

    private static void templateWith5Args() {
        Holder.LOG.info("Some array: [%s, %s, %s, %s, %s]")
                .with("string")
                .with('c')
                .with(1234567)
                .with(12345678901234L)
                .with("string");
    }

    private static void entryWithException() {
        Holder.LOG.info().append("Exception: ").append(EXCEPTION).commit();
    }

    private static void templateWithException() {
        Holder.LOG.info("Exception: %s").with(EXCEPTION);
    }

    private static final class Holder {

        private static final Log LOG = LogFactory.getLog(LOGGER);

    }

    private static final class Runner extends Thread {

        private final IdleStrategy idle = new BusySpinIdleStrategy();
        private final Histogram histogram = newHistogram();
        private final Runnable command;
        private final AtomicBoolean active;
        private final AtomicBoolean measure;
        private final long interval;
        private final int batch;
        private final int affinity;

        public Runner(final Runnable command,
                      final AtomicBoolean active,
                      final AtomicBoolean measure,
                      final long interval,
                      final int batch,
                      final int affinity) {
            super(THREAD);

            this.command = command;
            this.active = active;
            this.measure = measure;
            this.interval = interval;
            this.batch = batch;
            this.affinity = affinity;
        }

        @Override
        public void run() {
            if (affinity >= 0) {
                Affinity.setAffinity(affinity);
            }

            long next = System.nanoTime();
            boolean warmup = true;

            while (active.get()) {
                final long start = System.nanoTime();

                if (start < next) {
                    idle.idle(0);
                    continue;
                }

                for (int i = 0; i < batch; i++) {
                    command.run();
                }

                final long end = System.nanoTime();
                histogram.recordValue(end - start);
                next = (CATCHUP ? next : end) + interval;

                if (warmup && measure.get()) {
                    histogram.reset();
                    warmup = false;
                    next = System.nanoTime() + interval;
                }
            }
        }

    }

}
