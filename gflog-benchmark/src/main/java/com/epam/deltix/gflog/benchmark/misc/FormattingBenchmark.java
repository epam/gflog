package com.epam.deltix.gflog.benchmark.misc;

import com.epam.deltix.gflog.core.util.Formatting;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;


@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 5, time = 10)
@Fork(2)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class FormattingBenchmark {

    private static final byte[] BYTE_ARRAY = new byte[1024];

    private static final String LINE_50 = "12345678901234567890123456789012345678901234567890";
    private static final String STRING = LINE_50 + LINE_50 + LINE_50;
    public static int offsett = 42;

    @Benchmark
    public byte[] format() {
        Formatting.formatAsciiString(STRING, BYTE_ARRAY, offsett);
        return BYTE_ARRAY;
    }

    @Benchmark
    public byte[] format2() {
        Formatting.formatUtf8String(STRING, BYTE_ARRAY, offsett);
        return BYTE_ARRAY;
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(FormattingBenchmark.class.getName())
                .build();

        new Runner(opt).run();
    }

}
