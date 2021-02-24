package com.epam.deltix.gflog.benchmark;

import com.epam.deltix.gflog.api.Log;
import com.epam.deltix.gflog.api.LogFactory;
import com.epam.deltix.gflog.benchmark.util.BenchmarkState;
import com.epam.deltix.gflog.core.LogConfigurator;
import net.openhft.affinity.Affinity;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.ThreadParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.epam.deltix.gflog.benchmark.util.BenchmarkUtil.*;

@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 3, time = 15)
@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Threads(1)
public class ThroughputBenchmark {

    // 100 bytes + (line separator 1-2)
    // 2020-10-01 14:25:58.310 INFO '123456789012345678901234567890' [12345678901234567890123] Hello world!

    @Param({
            "noop",
            /*"console-direct",
            "console-wrapper",*/
            "file",
            "rolling-file"
    })
    public String config;

    @Param({"ASCII", "UTF-8"})
    public String encoding;

    @Setup
    public void setup() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty("temp-file", generateTempFile("gflog-throughput-benchmark"));
        properties.setProperty("encoding", encoding);

        final String configFile = "classpath:com/epam/deltix/gflog/benchmark/throughput-" + config + "-benchmark.xml";
        LogConfigurator.configure(configFile, properties);
    }

    @TearDown
    public void destroy() throws Exception {
        LogConfigurator.unconfigure();
        deleteTempDirectory();
    }

    @Benchmark
    public void baseline(final ThreadState state) {
    }

    @Benchmark
    public void timestamp(final ThreadState state) {
        System.currentTimeMillis();
    }

    @Benchmark
    public void entry1Arg(final ThreadState state) {
        Holder.LOG.info().append(MESSAGE).commit();
    }

    @Benchmark
    public void template1Arg(final ThreadState state) {
        Holder.LOG.info(MESSAGE);
    }

    @Benchmark
    public void entry5Args(final ThreadState state) {
        Holder.LOG.info()
                .append("Some array: [")
                .append(state.arg1).append(',')
                .append(state.arg2).append(',')
                .append(state.arg3).append(',')
                .append(state.arg4).append(',')
                .append(state.arg5).append(']')
                .commit();
    }

    @Benchmark
    public void template5Args(final ThreadState state) {
        Holder.LOG.info("Some array: [%s,%s,%s,%s,%s]")
                .with(state.arg1)
                .with(state.arg2)
                .with(state.arg3)
                .with(state.arg4)
                .with(state.arg5);
    }

    @Benchmark
    public void entry10Args(final ThreadState state) {
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

    @Benchmark
    public void template10Args(final ThreadState state) {
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

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(ThroughputBenchmark.class.getName())
                // .addProfiler(GCProfiler.class)
                .build();

        new Runner(opt).run();
    }

    @State(Scope.Thread)
    public static class ThreadState extends BenchmarkState {

        @Setup
        public void setup(final ThreadParams params) {
            Thread.currentThread().setName(THREAD);

            if (AFFINITY_BASE >= 0 && AFFINITY_STEP >= 0) {
                final int index = params.getThreadIndex();
                final int affinity = AFFINITY_BASE + index * AFFINITY_STEP;

                Affinity.setAffinity(affinity);
            }
        }

    }

    private static final class Holder {

        private static final Log LOG = LogFactory.getLog(LOGGER);

    }

}
