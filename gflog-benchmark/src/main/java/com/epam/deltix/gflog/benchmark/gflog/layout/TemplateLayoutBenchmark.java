package com.epam.deltix.gflog.benchmark.gflog.layout;

import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.LogRecordBean;
import com.epam.deltix.gflog.core.layout.Layout;
import com.epam.deltix.gflog.core.layout.TemplateLayoutFactory;
import com.epam.deltix.gflog.core.util.Buffer;
import com.epam.deltix.gflog.core.util.MutableBuffer;
import com.epam.deltix.gflog.core.util.UnsafeBuffer;
import com.epam.deltix.gflog.core.util.Util;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class TemplateLayoutBenchmark {

    private final MutableBuffer buffer = UnsafeBuffer.allocateHeap(128);
    private final LogRecordBean record = new LogRecordBean();

    private Layout layout;

    @Param({
            "%d %p [%t] %m%n",                 // optimized with level/thread/message
            "%d{yyyy-MM-dd HH:mm:ss.SSS}",     // optimized
            "%d{yyyy-MMdd- HH:mm:ss.SSS}",     // custom
    })
    public String template;

    @Param({"true", "false"})
    public boolean changeTimestamp;

    private long timestamp = System.currentTimeMillis() * 1_000_000;

    @Setup
    public void setup() {
        final TemplateLayoutFactory factory = new TemplateLayoutFactory();
        factory.setTemplate(template);

        layout = factory.create();

        record.setLogLevel(LogLevel.INFO);
        record.setLogName(Util.fromUtf8String("12345678901234567890"));
        record.setMessage(UnsafeBuffer.allocateDirect(64));
        record.setTimestamp(timestamp);
        record.setThreadName(Util.fromUtf8String("12345678901234567890"));
    }

    @Benchmark
    public Buffer benchmark() {
        if (changeTimestamp) {
            timestamp += 1_000_000;
            record.setTimestamp(timestamp);
        }

        layout.format(record, buffer, 0);
        return buffer;
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(TemplateLayoutBenchmark.class.getName())
                .build();

        new Runner(opt).run();
    }

}
