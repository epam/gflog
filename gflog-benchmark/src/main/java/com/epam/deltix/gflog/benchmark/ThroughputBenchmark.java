package com.epam.deltix.gflog.benchmark;

import com.epam.deltix.gflog.api.Log;
import com.epam.deltix.gflog.api.LogFactory;
import com.epam.deltix.gflog.core.LogConfigurator;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 3, time = 15)
@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Threads(1)
public class ThroughputBenchmark {

    private static final String TEMP_DIRECTORY = "tmp";
    private static final String TEMP_FILE = TEMP_DIRECTORY + "/throughput-benchmark-" + UUID.randomUUID() + ".log";

    // 100 bytes + (line separator 1-2)
    // 2020-10-01 14:25:58.310 INFO '123456789012345678901234567890' [12345678901234567890123] Hello world!

    private static final String MESSAGE = "Hello world!";
    private static final String LOGGER = "123456789012345678901234567890";
    private static final String THREAD = "12345678901234567890123";

    private static final Log LOG = LogFactory.getLog(LOGGER);

    @Param({
            "noop",
            /*"console-direct",
            "console-wrapper",*/
            "file",
            "rolling-file"
    })
    public String config;

    @Param({"ASCII", "UTF-8"})
    public String encoding;

    @Setup
    public void setup() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty("temp-file", TEMP_FILE);
        properties.setProperty("encoding", encoding);

        final String configFile = "classpath:com/epam/deltix/gflog/benchmark/throughput-" + config + "-benchmark.xml";
        LogConfigurator.configure(configFile, properties);
    }

    @TearDown
    public void destroy() throws Exception {
        LogConfigurator.unconfigure();
        deleteTmpDirectory();
    }

    @Benchmark
    public void entry(final ThreadState state) {
        LOG.info().append(MESSAGE).commit();
    }

    @Benchmark
    public void template(final ThreadState state) {
        LOG.info(MESSAGE);
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(ThroughputBenchmark.class.getName())
                // .addProfiler(GCProfiler.class)
                .build();

        new Runner(opt).run();
    }

    private static void deleteTmpDirectory() throws Exception {
        final Path directory = Paths.get(TEMP_DIRECTORY);

        if (Files.exists(directory)) {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    @State(Scope.Thread)
    public static class ThreadState {

        @Setup
        public void setup() {
            Thread.currentThread().setName(THREAD);
        }

    }

}
