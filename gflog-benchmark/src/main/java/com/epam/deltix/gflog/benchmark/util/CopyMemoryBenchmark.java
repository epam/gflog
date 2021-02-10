package com.epam.deltix.gflog.benchmark.util;

import com.epam.deltix.gflog.core.util.Util;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

import static com.epam.deltix.gflog.core.util.Util.UNSAFE;


@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 3, time = 5)
@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CopyMemoryBenchmark {

    private static final int SIZE = 64;

    private final long address1 = Util.UNSAFE.allocateMemory(SIZE + 15) + 7 & (~7);
    private final long address2 = Util.UNSAFE.allocateMemory(SIZE + 15) + 7 & (~7);

    {
        for (int i = 0; i < SIZE + 8; i++) {
            UNSAFE.putByte(address1 + i, (byte) 1);
            UNSAFE.putByte(address2 + i, (byte) 1);
        }
    }

    @Benchmark
    public void copyMemory0() {
        UNSAFE.copyMemory(address1, address2, SIZE);
    }

    @Benchmark
    public void copyMemory1() {
        UNSAFE.copyMemory(address1, address2, SIZE - 1);
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(CopyMemoryBenchmark.class.getName())
                .build();

        new Runner(opt).run();
    }

}
