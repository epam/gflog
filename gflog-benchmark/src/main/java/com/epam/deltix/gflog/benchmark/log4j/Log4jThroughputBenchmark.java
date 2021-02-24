package com.epam.deltix.gflog.benchmark.log4j;

import com.epam.deltix.gflog.benchmark.util.BenchmarkState;
import net.openhft.affinity.Affinity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.ThreadParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

import static com.epam.deltix.gflog.benchmark.util.BenchmarkUtil.*;
import static org.apache.logging.log4j.util.Unbox.box;

@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 3, time = 15)
@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Threads(1)
public class Log4jThroughputBenchmark {

    // 100 bytes + (line separator 1-2)
    // 2020-10-01 14:25:58.310 INFO '123456789012345678901234567890' [12345678901234567890123] Hello world!

    @Param({
            "noop",
            "file"
    })
    public String config;

    @Setup
    public void setup() throws Exception {
        //System.setProperty("log4j.debug", "true");
        System.setProperty("log4j.configurationFile", "com/epam/deltix/gflog/benchmark/log4j/throughput-" + config + "-benchmark.xml");
        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        System.setProperty("AsyncLogger.RingBufferSize", "262144");
        System.setProperty("AsyncLogger.WaitStrategy", "Yield");
        System.setProperty("log4j2.enable.threadlocals", "true");
        System.setProperty("log4j2.enable.direct.encoders", "true");
        System.setProperty("temp-file", generateTempFile("log4j-throughput-benchmark"));
    }

    @TearDown
    public void destroy() throws Exception {
        LogManager.shutdown();
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
    public void log1Arg(final ThreadState state) {
        Holder.LOG.info(MESSAGE);
    }

    @Benchmark
    public void log5Args(final ThreadState state) {
        Holder.LOG.info("Some array: [{},{},{},{},{}]",
                state.arg1, box(state.arg2), box(state.arg3), box(state.arg4), state.arg5
        );
    }

    @Benchmark
    public void log10Args(final ThreadState state) {
        Holder.LOG.info("Some array: [{},{},{},{},{},{},{},{},{},{}]",
                state.arg1, box(state.arg2), box(state.arg3), box(state.arg4), state.arg5,
                state.arg6, box(state.arg7), box(state.arg8), box(state.arg9), state.arg10
        );
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(Log4jThroughputBenchmark.class.getName())
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

    public static final class Holder {

        private static final Logger LOG = LogManager.getLogger(LOGGER);

    }

}
