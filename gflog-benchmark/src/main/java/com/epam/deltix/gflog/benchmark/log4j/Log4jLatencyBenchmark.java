package com.epam.deltix.gflog.benchmark.log4j;

import com.epam.deltix.gflog.benchmark.LatencyBenchmarkRunner;
import com.epam.deltix.gflog.benchmark.util.Benchmark;
import com.epam.deltix.gflog.benchmark.util.BenchmarkState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.epam.deltix.gflog.benchmark.util.BenchmarkUtil.*;
import static org.apache.logging.log4j.util.Unbox.box;

/**
 * Shows safepoints: -XX:+UnlockDiagnosticVMOptions -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime
 * Shows details: -XX:+PrintSafepointStatistics -XX:PrintSafepointStatisticsCount=1
 * Adjusts interval: -XX:GuaranteedSafepointInterval=1200000
 */
public class Log4jLatencyBenchmark {

    // 100 bytes + (line separator 1-2)
    // 2020-10-01 14:25:58.310 INFO '123456789012345678901234567890' [12345678901234567890123] Hello world!

    private static final String[] CONFIGS = {
            "noop",
            "file",
    };

    public static void main(final String[] args) throws Exception {
        final Map<String, Benchmark> all = benchmarks();
        final LatencyBenchmarkRunner runner = new LatencyBenchmarkRunner(all);

        runner.run(args);
    }

    private static void log1Arg(final BenchmarkState state) {
        Holder.LOG.info(MESSAGE);
    }

    private static void log5Args(final BenchmarkState state) {
        Holder.LOG.info("Some array: [{},{},{},{},{}]",
                state.arg1, box(state.arg2), box(state.arg3), box(state.arg4), state.arg5
        );
    }

    private static void log10Args(final BenchmarkState state) {
        Holder.LOG.info("Some array: [{},{},{},{},{},{},{},{},{},{}]",
                state.arg1, box(state.arg2), box(state.arg3), box(state.arg4), state.arg5,
                state.arg6, box(state.arg7), box(state.arg8), box(state.arg9), state.arg10
        );
    }


    private static void prepare(final String config) {
        //System.setProperty("log4j.debug", "true");
        System.setProperty("log4j.configurationFile", "com/epam/deltix/gflog/benchmark/log4j/log4j-" + config + "-benchmark.xml");
        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        System.setProperty("AsyncLogger.RingBufferSize", "262144");
        System.setProperty("AsyncLogger.WaitStrategy", "Yield");
        System.setProperty("log4j2.enable.threadlocals", "true");
        System.setProperty("log4j2.enable.direct.encoders", "true");
        System.setProperty("temp-file", generateTempFile("log4j-latency-benchmark"));
    }

    private static void cleanup() {
        try {
            LogManager.shutdown();
            deleteTempDirectory();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, Benchmark> benchmarks() {
        final List<Benchmark> benchmarks = new ArrayList<>();
        final Runnable noop = () -> {
        };

        benchmarks.add(new Benchmark("baseline", noop, noop, LatencyBenchmarkRunner::baseline));
        benchmarks.add(new Benchmark("timestamp", noop, noop, LatencyBenchmarkRunner::timestamp));

        for (final String config : CONFIGS) {
            final Runnable prepare = () -> prepare(config);
            final Runnable cleanup = Log4jLatencyBenchmark::cleanup;

            benchmarks.add(new Benchmark("log1Arg-" + config, prepare, cleanup, Log4jLatencyBenchmark::log1Arg));
            benchmarks.add(new Benchmark("log5Args-" + config, prepare, cleanup, Log4jLatencyBenchmark::log5Args));
            benchmarks.add(new Benchmark("log10Args-" + config, prepare, cleanup, Log4jLatencyBenchmark::log10Args));
        }

        final Map<String, Benchmark> map = new LinkedHashMap<>();

        for (final Benchmark benchmark : benchmarks) {
            final String name = benchmark.getName();
            map.put(name, benchmark);
        }

        return map;
    }

    private static final class Holder {

        private static final Logger LOG = LogManager.getLogger(LOGGER);

    }

}
