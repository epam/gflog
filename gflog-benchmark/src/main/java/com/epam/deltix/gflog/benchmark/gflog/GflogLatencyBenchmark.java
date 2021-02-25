package com.epam.deltix.gflog.benchmark.gflog;

import com.epam.deltix.gflog.benchmark.util.BenchmarkDescriptor;
import com.epam.deltix.gflog.benchmark.util.LatencyBenchmarkRunner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.epam.deltix.gflog.benchmark.util.BenchmarkUtil.ENCODING;

/**
 * Shows safepoints: -XX:+UnlockDiagnosticVMOptions -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime
 * Shows details: -XX:+PrintSafepointStatistics -XX:PrintSafepointStatisticsCount=1
 * Adjusts interval: -XX:GuaranteedSafepointInterval=1200000
 */
public class GflogLatencyBenchmark {

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
            final Runnable prepare = () -> GflogBenchmarkUtil.prepare(config, ENCODING);
            final Runnable cleanup = GflogBenchmarkUtil::cleanup;

            benchmarks.add(new BenchmarkDescriptor("entry1Arg-" + config, prepare, cleanup, GflogBenchmarkUtil::entry1Arg));
            benchmarks.add(new BenchmarkDescriptor("entry5Args-" + config, prepare, cleanup, GflogBenchmarkUtil::entry5Args));
            benchmarks.add(new BenchmarkDescriptor("entry10Args-" + config, prepare, cleanup, GflogBenchmarkUtil::entry10Args));
            benchmarks.add(new BenchmarkDescriptor("entryException-" + config, prepare, cleanup, GflogBenchmarkUtil::entryException));

            benchmarks.add(new BenchmarkDescriptor("template1Arg-" + config, prepare, cleanup, GflogBenchmarkUtil::template1Arg));
            benchmarks.add(new BenchmarkDescriptor("template5Args-" + config, prepare, cleanup, GflogBenchmarkUtil::template5Args));
            benchmarks.add(new BenchmarkDescriptor("template10Args-" + config, prepare, cleanup, GflogBenchmarkUtil::template10Args));
            benchmarks.add(new BenchmarkDescriptor("templateException-" + config, prepare, cleanup, GflogBenchmarkUtil::templateException));
        }

        final Map<String, BenchmarkDescriptor> map = new LinkedHashMap<>();

        for (final BenchmarkDescriptor benchmark : benchmarks) {
            final String name = benchmark.getName();
            map.put(name, benchmark);
        }

        return map;
    }

}
