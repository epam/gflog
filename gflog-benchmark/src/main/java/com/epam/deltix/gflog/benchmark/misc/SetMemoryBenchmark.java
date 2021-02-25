package com.epam.deltix.gflog.benchmark.misc;

import com.epam.deltix.gflog.core.util.Util;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

import static com.epam.deltix.gflog.core.util.Util.UNSAFE;


@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 5, time = 10)
@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class SetMemoryBenchmark {

    private static final int SIZE = 128 * 1024;
    private static final int WORDS = SIZE / 8;
    private static final int STEP = 64;

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

    @Benchmark
    public void setMemoryForwardLongWithStep() {
        for (int i = 0; i < SIZE; i += STEP) {
            UNSAFE.putLong(address + i, 0);
        }
    }

    @Benchmark
    public void setMemoryBackwardLongWithStep() {
        for (int i = SIZE - STEP; i >= 0; i -= STEP) {
            UNSAFE.putLong(address + i, 0);
        }
    }

    @Benchmark
    public void setMemoryForwardIntWithStep() {
        for (int i = 0; i < SIZE; i += STEP) {
            UNSAFE.putInt(address + i, 0);
        }
    }

    @Benchmark
    public void setMemoryBackwardIntWithStep() {
        for (int i = SIZE - STEP; i >= 0; i -= STEP) {
            UNSAFE.putInt(address + i, 0);
        }
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(SetMemoryBenchmark.class.getName())
                .build();

        new Runner(opt).run();
    }

}
