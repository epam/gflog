package com.epam.deltix.gflog.benchmark.util;

import com.epam.deltix.gflog.core.util.Util;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

import static com.epam.deltix.gflog.core.util.Util.UNSAFE;


@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class SetMemoryBenchmark {

    private static final int SIZE = 128 * 1024;
    private static final int WORDS = SIZE / 8;

    private final long address = (Util.UNSAFE.allocateMemory(SIZE + 7) + 7) & (~7);

    {
        for (int i = 0; i < SIZE; i++) {
            UNSAFE.putByte(address + i, (byte) 1);
        }
    }

    @Benchmark
    public void setMemory1() {
        UNSAFE.setMemory(address, SIZE, (byte) 0);
    }

    @Benchmark
    public void setMemory2() { // works faster on linux, but may work slower on windows
        UNSAFE.putByte(address, (byte) 0);
        UNSAFE.setMemory(address + 1, SIZE - 1, (byte) 0);
    }

    @Benchmark
    public void setMemory3() {
        for (int i = 0; i < WORDS; i++) {
            UNSAFE.putLong(address + (i << 3), 0);
        }
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(SetMemoryBenchmark.class.getName())
                .build();

        new Runner(opt).run();
    }

}
