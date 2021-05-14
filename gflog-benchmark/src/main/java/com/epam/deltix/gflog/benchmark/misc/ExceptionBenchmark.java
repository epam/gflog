package com.epam.deltix.gflog.benchmark.misc;

import com.epam.deltix.gflog.benchmark.util.BenchmarkState;
import net.openhft.affinity.Affinity;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.ThreadParams;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

import static com.epam.deltix.gflog.benchmark.util.BenchmarkUtil.*;


@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 3, time = 10)
@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ExceptionBenchmark {

    @Benchmark
    public Object baseline(final ThreadState state) {
        return null;
    }

    @Benchmark
    public Object newObject(final ThreadState state) {
        return new Object();
    }

    @Benchmark
    public Object newObjectRecursively(final ThreadState state) {
        return state.newObject();
    }

    @Benchmark
    public Object newExceptionWithStackRecursively(final ThreadState state) {
        return state.newException();
    }

    @Benchmark
    public Object newExceptionWithoutStackRecursively(final ThreadState state) {
        return state.newExceptionWithoutStack();
    }

    @Benchmark
    public Object newExceptionWithStackAndGetStackRecursively(final ThreadState state) {
        return state.newException().getStackTrace();
    }

    @Benchmark
    public Object newExceptionWithoutStackAndGetStackRecursively(final ThreadState state) {
        return state.newExceptionWithoutStack().getStackTrace();
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(ExceptionBenchmark.class.getName())
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

}
