package com.epam.deltix.gflog.benchmark.misc;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;


@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 3, time = 5)
@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ExceptionBenchmark {

    private boolean printed;

    @Benchmark
    public Object baseline() {
        return new Object();
    }

    @Benchmark
    public Object exceptionWithStack() {
        return new Throwable("my-exception");
    }

    @Benchmark
    public Object exceptionWithoutStack() {
        return new Throwable("my-exception") {

            @Override
            public Throwable fillInStackTrace() {
                return this;
            }

        };
    }

    @Benchmark
    public Object exceptionGetStack() {
        if (!printed) {
            printed = true;
            print();
        }

        return new Throwable("my-exception").getStackTrace();
    }

    private static void print() {
        final int length = new Throwable("my-exception").getStackTrace().length - 1;
        System.err.println("Stack frames: " + length);
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(ExceptionBenchmark.class.getName())
                .build();

        new Runner(opt).run();
    }

}
