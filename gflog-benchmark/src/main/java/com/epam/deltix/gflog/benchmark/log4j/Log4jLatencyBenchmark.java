package com.epam.deltix.gflog.benchmark.log4j;

import com.epam.deltix.gflog.benchmark.util.BenchmarkDescriptor;
import com.epam.deltix.gflog.benchmark.util.LatencyBenchmarkRunner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shows safepoints: -XX:+UnlockDiagnosticVMOptions -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime
 * Shows details: -XX:+PrintSafepointStatistics -XX:PrintSafepointStatisticsCount=1
 * Adjusts interval: -XX:GuaranteedSafepointInterval=1200000
 */
public class Log4jLatencyBenchmark {

    private static final String[] CONFIGS = {
            "noop",
            "file",
    };

    public static void main(final String[] args) throws Exception {
        final Map<String, BenchmarkDescriptor> all = benchmarks();
        final LatencyBenchmarkRunner runner = new LatencyBenchmarkRunner(all);

        runner.run(args);
    }

    private static Map<String, BenchmarkDescriptor> benchmarks() {
        final List<BenchmarkDescriptor> benchmarks = new ArrayList<>();
        final Runnable noop = () -> {
        };

        benchmarks.add(new BenchmarkDescriptor("baseline", noop, noop, LatencyBenchmarkRunner::baseline));
        benchmarks.add(new BenchmarkDescriptor("timestamp", noop, noop, LatencyBenchmarkRunner::timestamp));

        for (final String config : CONFIGS) {
            final Runnable prepare = () -> Log4jBenchmarkUtil.prepare(config);
            final Runnable cleanup = Log4jBenchmarkUtil::cleanup;

            benchmarks.add(new BenchmarkDescriptor("log0Arg-" + config, prepare, cleanup, Log4jBenchmarkUtil::log0Arg));
            benchmarks.add(new BenchmarkDescriptor("log1Arg-" + config, prepare, cleanup, Log4jBenchmarkUtil::log1Arg));
            benchmarks.add(new BenchmarkDescriptor("log5Args-" + config, prepare, cleanup, Log4jBenchmarkUtil::log5Args));
            benchmarks.add(new BenchmarkDescriptor("log10Args-" + config, prepare, cleanup, Log4jBenchmarkUtil::log10Args));
            benchmarks.add(new BenchmarkDescriptor("logException-" + config, prepare, cleanup, Log4jBenchmarkUtil::logException));
            benchmarks.add(new BenchmarkDescriptor("logCachedException-" + config, prepare, cleanup,
                    Log4jBenchmarkUtil::logCachedException));
        }

        final Map<String, BenchmarkDescriptor> map = new LinkedHashMap<>();

        for (final BenchmarkDescriptor benchmark : benchmarks) {
            final String name = benchmark.getName();
            map.put(name, benchmark);
        }

        return map;
    }

}
