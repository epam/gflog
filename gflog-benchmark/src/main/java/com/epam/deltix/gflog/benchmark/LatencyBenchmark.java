package com.epam.deltix.gflog.benchmark;

import com.epam.deltix.gflog.api.Log;
import com.epam.deltix.gflog.api.LogFactory;
import com.epam.deltix.gflog.benchmark.util.Benchmark;
import com.epam.deltix.gflog.benchmark.util.BenchmarkState;
import com.epam.deltix.gflog.core.LogConfigurator;

import java.util.*;

import static com.epam.deltix.gflog.benchmark.util.BenchmarkUtil.*;

/**
 * Shows safepoints: -XX:+UnlockDiagnosticVMOptions -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime
 * Shows details: -XX:+PrintSafepointStatistics -XX:PrintSafepointStatisticsCount=1
 * Adjusts interval: -XX:GuaranteedSafepointInterval=1200000
 */
public class LatencyBenchmark {

    // 100 bytes + (line separator 1-2)
    // 2020-10-01 14:25:58.310 INFO '123456789012345678901234567890' [12345678901234567890123] Hello world!

    private static final String[] CONFIGS = {
            "noop",
            /*"console-direct",
            "console-wrapper",*/
            "file",
            "rolling-file"
    };

    public static void main(final String[] args) throws Exception {
        final Map<String, Benchmark> all = benchmarks();
        final LatencyBenchmarkRunner runner = new LatencyBenchmarkRunner(all);

        runner.run(args);
    }

    private static void entry1Arg(final BenchmarkState state) {
        Holder.LOG.info().append(MESSAGE).commit();
    }

    private static void template1Arg(final BenchmarkState state) {
        Holder.LOG.info(MESSAGE);
    }

    public static void entry5Args(final BenchmarkState state) {
        Holder.LOG.info()
                .append("Some array: [")
                .append(state.arg1).append(',')
                .append(state.arg2).append(',')
                .append(state.arg3).append(',')
                .append(state.arg4).append(',')
                .append(state.arg5).append(']')
                .commit();
    }

    public static void template5Args(final BenchmarkState state) {
        Holder.LOG.info("Some array: [%s,%s,%s,%s,%s]")
                .with(state.arg1)
                .with(state.arg2)
                .with(state.arg3)
                .with(state.arg4)
                .with(state.arg5);
    }

    public static void entry10Args(final BenchmarkState state) {
        Holder.LOG.info()
                .append("Some array: [")
                .append(state.arg1).append(',')
                .append(state.arg2).append(',')
                .append(state.arg3).append(',')
                .append(state.arg4).append(',')
                .append(state.arg5).append(',')
                .append(state.arg6).append(',')
                .append(state.arg7).append(',')
                .append(state.arg8).append(',')
                .append(state.arg9).append(',')
                .append(state.arg10).append(']')
                .commit();
    }

    public static void template10Args(final BenchmarkState state) {
        Holder.LOG.info("Some array: [%s,%s,%s,%s,%s,%s,%s,%s,%s,%s]")
                .with(state.arg1)
                .with(state.arg2)
                .with(state.arg3)
                .with(state.arg4)
                .with(state.arg5)
                .with(state.arg6)
                .with(state.arg7)
                .with(state.arg8)
                .with(state.arg9)
                .with(state.arg10);
    }

    private static void entryException(final BenchmarkState state) {
        Holder.LOG.info().append("Exception: ").append(state.exception).commit();
    }

    private static void templateException(final BenchmarkState state) {
        Holder.LOG.info("Exception: %s").with(state.exception);
    }

    private static void prepare(final String config) {
        try {
            final Properties properties = new Properties();
            properties.setProperty("temp-file", generateTempFile("gflog-latency-benchmark"));
            properties.setProperty("encoding", ENCODING);

            final String configFile = "classpath:com/epam/deltix/gflog/benchmark/gflog-" + config + "-benchmark.xml";
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

    private static Map<String, Benchmark> benchmarks() {
        final List<Benchmark> benchmarks = new ArrayList<>();
        final Runnable noop = () -> {
        };

        benchmarks.add(new Benchmark("baseline", noop, noop, LatencyBenchmarkRunner::baseline));
        benchmarks.add(new Benchmark("timestamp", noop, noop, LatencyBenchmarkRunner::timestamp));

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
            final String name = benchmark.getName();
            map.put(name, benchmark);
        }

        return map;
    }

    private static final class Holder {

        private static final Log LOG = LogFactory.getLog(LOGGER);

    }

}
