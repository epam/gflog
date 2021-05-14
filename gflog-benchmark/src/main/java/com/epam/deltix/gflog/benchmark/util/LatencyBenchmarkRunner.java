package com.epam.deltix.gflog.benchmark.util;

import com.epam.deltix.gflog.core.idle.BusySpinIdleStrategy;
import com.epam.deltix.gflog.core.idle.IdleStrategy;
import net.openhft.affinity.Affinity;
import org.HdrHistogram.Histogram;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.epam.deltix.gflog.benchmark.util.BenchmarkUtil.*;

/**
 * Shows safepoints: -XX:+UnlockDiagnosticVMOptions -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime
 * Shows details: -XX:+PrintSafepointStatistics -XX:PrintSafepointStatisticsCount=1
 * Adjusts interval: -XX:GuaranteedSafepointInterval=1200000
 */
public final class LatencyBenchmarkRunner {

    private final Map<String, BenchmarkDescriptor> all;

    public LatencyBenchmarkRunner(final Map<String, BenchmarkDescriptor> all) {
        this.all = all;
    }

    public void run(final String[] args) throws Exception {
        if (args.length == 0) {
            printHelp(all);
            System.exit(1);
        }

        for (final String name : args) {
            run(name);
        }
    }

    private void run(final String name) throws Exception {
        final BenchmarkDescriptor benchmark = all.get(name);
        Objects.requireNonNull(benchmark, "Benchmark is not found: " + name);

        final Runnable prepare = benchmark.getPrepare();
        final Runnable cleanup = benchmark.getCleanup();
        final Consumer<BenchmarkState> command = benchmark.getCommand();

        prepare.run();

        try {
            run(name, command);
        } finally {
            cleanup.run();
        }
    }

    private static void run(final String name, final Consumer<BenchmarkState> command) throws Exception {
        System.out.printf("Benchmark: %s. Warmup: %s s. Duration: %s s. Interval: %s ns. " +
                        "Threads: %s. Batch: %s. Catchup: %s. Affinity (base,step): %s,%s.%n",
                name, WARMUP_S, DURATION_S, INTERVAL_NS, THREADS, BATCH, CATCHUP, AFFINITY_BASE, AFFINITY_STEP);

        final AtomicBoolean active = new AtomicBoolean(true);
        final AtomicBoolean measure = new AtomicBoolean(false);

        final ThreadRunner[] runners = new ThreadRunner[THREADS];

        for (int i = 0; i < THREADS; i++) {
            final int affinity = (AFFINITY_BASE < 0 || AFFINITY_STEP < 0) ? -1 : (AFFINITY_BASE + i * AFFINITY_STEP);
            final ThreadRunner runner = new ThreadRunner(command, active, measure, INTERVAL_NS, BATCH, affinity);
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

        for (final ThreadRunner runner : runners) {
            runner.join();
            histogram.add(runner.histogram);
        }

        System.out.printf("Benchmark: %s. Took: %s ms. Throughput: %.0f msg/s. Latency: %n",
                name, took, 1000.0 * histogram.getTotalCount() * BATCH / took);

        histogram.outputPercentileDistribution(System.out, (double) BATCH);
        System.out.println();
    }

    public static void baseline(final BenchmarkState state) {
    }

    public static void timestamp(final BenchmarkState state) {
        System.currentTimeMillis();
    }

    private static final class ThreadRunner extends Thread {

        private final IdleStrategy idle = new BusySpinIdleStrategy();
        private final Histogram histogram = newHistogram();
        private final Consumer<BenchmarkState> command;
        private final AtomicBoolean active;
        private final AtomicBoolean measure;
        private final long interval;
        private final int batch;
        private final int affinity;

        ThreadRunner(final Consumer<BenchmarkState> command,
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

            final BenchmarkState state = new BenchmarkState();

            long next = System.nanoTime();
            boolean warmup = true;

            while (active.get()) {
                final long start = System.nanoTime();

                if (start < next) {
                    idle.idle(0);
                    continue;
                }

                for (int i = 0; i < batch; i++) {
                    command.accept(state);
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
