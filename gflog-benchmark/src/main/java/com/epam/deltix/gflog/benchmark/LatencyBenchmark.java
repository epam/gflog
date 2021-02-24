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
import java.util.*;
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
            "file",
            "rolling-file"
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
    private static final int AFFINITY_BASE = Integer.getInteger("benchmark.affinity.base", -1);
    private static final int AFFINITY_STEP = Integer.getInteger("benchmark.affinity.step", 1);

    // causes a burst on a context switch if enabled, otherwise the guaranteed interval is preserved between operations
    private static final boolean CATCHUP = Boolean.getBoolean("benchmark.catchup");

    private static final Map<String, Benchmark> BENCHMARKS = benchmarks();

    public static void main(final String[] args) throws Exception {
        if (args.length == 0) {
            printHelp();
            System.exit(1);
        }

        for (final String name : args) {
            run(name);
        }
    }

    private static void run(final String name) throws Exception {
        final Benchmark benchmark = BENCHMARKS.get(name);
        Objects.requireNonNull(benchmark, "Benchmark is not found: " + name);

        final Runnable prepare = benchmark.prepare;
        final Runnable command = benchmark.command;
        final Runnable cleanup = benchmark.cleanup;

        prepare.run();

        try {
            run(name, command);
        } finally {
            cleanup.run();
        }
    }

    private static void run(final String name, final Runnable command) throws Exception {
        System.out.printf("Benchmark: %s. Warmup: %s s. Duration: %s s. Interval: %s ns. Threads: %s. Batch: %s. Catchup: %s. Affinity (base,step): %s,%s.%n",
                name, WARMUP_S, DURATION_S, INTERVAL_NS, THREADS, BATCH, CATCHUP, AFFINITY_BASE, AFFINITY_STEP);

        final AtomicBoolean active = new AtomicBoolean(true);
        final AtomicBoolean measure = new AtomicBoolean(false);

        final Runner[] runners = new Runner[THREADS];

        for (int i = 0; i < THREADS; i++) {
            final int affinity = (AFFINITY_BASE < 0 || AFFINITY_STEP < 0) ? -1 : (AFFINITY_BASE + i * AFFINITY_STEP);
            final Runner runner = new Runner(command, active, measure, INTERVAL_NS, BATCH, affinity);
            runner.start();

            runners[i] = runner;
        }

        System.gc();
        Thread.sleep(TimeUnit.SECONDS.toMillis(WARMUP_S));

        System.out.printf("Benchmark: %s. Measuring.%n", name);

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
                name, took, 1000.0 * histogram.getTotalCount() * BATCH / took);

        histogram.outputPercentileDistribution(System.out, (double) BATCH);
        System.out.println();
    }

    private static Map<String, Benchmark> benchmarks() {
        final List<Benchmark> benchmarks = new ArrayList<>();

        benchmarks.add(new Benchmark("baseline", LatencyBenchmark::baseline));
        benchmarks.add(new Benchmark("timestamp", LatencyBenchmark::timestamp));

        for (final String config : CONFIGS) {
            final Runnable prepare = () -> prepare(config);
            final Runnable cleanup = LatencyBenchmark::cleanup;

            benchmarks.add(new Benchmark("entry1Arg-" + config, prepare, cleanup, LatencyBenchmark::entry1Arg));
            benchmarks.add(new Benchmark("entry5Args-" + config, prepare, cleanup, LatencyBenchmark::entry5Args));
            benchmarks.add(new Benchmark("entry10Args-" + config, prepare, cleanup, LatencyBenchmark::entry10Args));
            benchmarks.add(new Benchmark("entryException-" + config, prepare, cleanup, LatencyBenchmark::entryException));

            benchmarks.add(new Benchmark("template1Arg-" + config, prepare, cleanup, LatencyBenchmark::template1Arg));
            benchmarks.add(new Benchmark("template5Args-" + config, prepare, cleanup, LatencyBenchmark::template5Args));
            benchmarks.add(new Benchmark("template10Args-" + config, prepare, cleanup, LatencyBenchmark::template10Args));
            benchmarks.add(new Benchmark("templateException-" + config, prepare, cleanup, LatencyBenchmark::templateException));
        }

        final Map<String, Benchmark> map = new LinkedHashMap<>();

        for (final Benchmark benchmark : benchmarks) {
            map.put(benchmark.name, benchmark);
        }

        return map;
    }

    private static void printHelp() {
        System.out.printf("No benchmarks specified to run. Sample: bench1 bench2 bench3.%n");
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

        System.out.printf("%nBenchmarks (cmd-args):%n");
        for (final String benchmark : BENCHMARKS.keySet()) {
            System.out.printf("\t%s%n", benchmark);
        }
    }

    private static void prepare(final String config) {
        try {
            final Properties properties = new Properties();
            properties.setProperty("temp-file", TEMP_FILE);
            properties.setProperty("encoding", ENCODING);

            final String configFile = "classpath:com/epam/deltix/gflog/benchmark/throughput-" + config + "-benchmark.xml";
            LogConfigurator.configure(configFile, properties);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void cleanup() {
        try {
            LogConfigurator.unconfigure();
            deleteTempDirectory();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
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

    private static void entry1Arg() {
        Holder.LOG.info().append(MESSAGE).commit();
    }

    private static void template1Arg() {
        Holder.LOG.info(MESSAGE);
    }

    private static void entry5Args() {
        Holder.LOG.info()
                .append("Some array: [")
                .append("string").append(',')
                .append('c').append(',')
                .append(1234567).append(',')
                .append(12345678901234L).append(',')
                .append("string").append(']')
                .commit();
    }

    private static void template5Args() {
        Holder.LOG.info("Some array: [%s, %s, %s, %s, %s]")
                .with("string")
                .with('c')
                .with(1234567)
                .with(12345678901234L)
                .with("string");
    }

    private static void entry10Args() {
        Holder.LOG.info()
                .append("Some array: [")
                .append("string").append(',')
                .append('c').append(',')
                .append(1234567).append(',')
                .append(12345678901234L).append(',')
                .append("string").append(',')
                .append("string").append(',')
                .append('c').append(',')
                .append(1234567).append(',')
                .append(12345678901234L).append(',')
                .append("string").append(']')
                .commit();
    }

    private static void template10Args() {
        Holder.LOG.info("Some array: [%s, %s, %s, %s, %s, %s, %s, %s, %s, %s]")
                .with("string")
                .with('c')
                .with(1234567)
                .with(12345678901234L)
                .with("string")
                .with("string")
                .with('c')
                .with(1234567)
                .with(12345678901234L)
                .with("string");
    }

    private static void entryException() {
        Holder.LOG.info().append("Exception: ").append(EXCEPTION).commit();
    }

    private static void templateException() {
        Holder.LOG.info("Exception: %s").with(EXCEPTION);
    }

    private static final class Benchmark {

        private final String name;
        private final Runnable prepare;
        private final Runnable cleanup;
        private final Runnable command;

        public Benchmark(final String name, final Runnable command) {
            this(name, LatencyBenchmark::baseline, LatencyBenchmark::baseline, command);
        }

        public Benchmark(final String name, final Runnable prepare, final Runnable cleanup, final Runnable command) {
            this.name = name;
            this.prepare = prepare;
            this.cleanup = cleanup;
            this.command = command;
        }

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
