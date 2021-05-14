package com.epam.deltix.gflog.benchmark.gflog;

import com.epam.deltix.gflog.api.Log;
import com.epam.deltix.gflog.api.LogFactory;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;


@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 3, time = 5)
@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class GflogStubBenchmark {

    private static final Log LOG = LogFactory.getLog(GflogStubBenchmark.class);

    @Benchmark
    public void baseline() {
    }


    @Benchmark
    public void entryWith1Arg() {
        LOG.debug().append("1").commit();
    }

    @Benchmark
    public void entryWith10Args() {
        LOG.debug()
                .append("1")
                .append(2)
                .append('3')
                .append(4L)
                .append(5.0)
                .append("6")
                .append(7)
                .append('8')
                .append(9L)
                .append(10.0)
                .commit();
    }


    @Benchmark
    public void entryIfWith1Arg() {
        if (LOG.isDebugEnabled()) {
            LOG.debug().append("1").commit();
        }
    }

    @Benchmark
    public void entryIfWith10Args() {
        if (LOG.isDebugEnabled()) {
            LOG.debug()
                    .append("1")
                    .append(2)
                    .append('3')
                    .append(4L)
                    .append(5.0)
                    .append("6")
                    .append(7)
                    .append('8')
                    .append(9L)
                    .append(10.0)
                    .commit();
        }
    }


    @Benchmark
    public void templateWith1Arg() {
        LOG.debug("1");
    }

    @Benchmark
    public void templateWith10Args() {
        LOG.debug("%s%s%s%s%s%s%s%s%s%s")
                .with("1")
                .with(2)
                .with('3')
                .with(4L)
                .with(5.0)
                .with("6")
                .with(7)
                .with('8')
                .with(9L)
                .with(10.0);
    }


    @Benchmark
    public void templateIfWith1Arg() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("1");
        }
    }

    @Benchmark
    public void templateIfWith10Arg() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("%s%s%s%s%s%s%s%s%s%s")
                    .with("1")
                    .with(2)
                    .with('3')
                    .with(4L)
                    .with(5.0)
                    .with("6")
                    .with(7)
                    .with('8')
                    .with(9L)
                    .with(10.0);
        }
    }

    public static void main(final String[] args) throws Exception {
        final Options opt = new OptionsBuilder()
                .include(GflogStubBenchmark.class.getName())
                .build();

        new Runner(opt).run();
    }

}
