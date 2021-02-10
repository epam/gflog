package com.epam.deltix.gflog.core.service;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;


@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 3, time = 10)
@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class LogEntryBenchmark {

    private final Exception exception = new Exception();
    private final LogAsciiEntry asciiEntry = new LogAsciiEntry(">>", 4096, 64 * 1024);
    private final LogUtf8Entry utf8Entry = new LogUtf8Entry(">>", 4096, 64 * 1024);


    @Benchmark
    public LogAsciiEntry ascii() {
        asciiEntry.clear();
        asciiEntry.append("1234567890");
        asciiEntry.append(2312312);
        asciiEntry.append("1234567890");
        asciiEntry.append(432423423L);
        asciiEntry.append("1234567890");
        asciiEntry.append(true);
        asciiEntry.append("1234567890");
        asciiEntry.append('a');
        asciiEntry.append("1234567890");
        return asciiEntry;
    }

    @Benchmark
    public LogUtf8Entry utf8() {
        utf8Entry.clear();
        utf8Entry.append("1234567890");
        utf8Entry.append(2312312);
        utf8Entry.append("1234567890");
        utf8Entry.append(432423423L);
        utf8Entry.append("1234567890");
        utf8Entry.append(true);
        utf8Entry.append("1234567890");
        utf8Entry.append('a');
        utf8Entry.append("1234567890");
        return utf8Entry;
    }


    @Benchmark
    public LogAsciiEntry asciiException() {
        asciiEntry.clear();
        asciiEntry.append(exception);
        return asciiEntry;
    }

    @Benchmark
    public LogUtf8Entry utf8Exception() {
        utf8Entry.clear();
        utf8Entry.append(exception);
        return utf8Entry;
    }


    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(LogEntryBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

}
