package com.epam.deltix.gflog.benchmark;

import com.epam.deltix.gflog.api.Log;
import com.epam.deltix.gflog.api.LogFactory;
import com.epam.deltix.gflog.core.LogConfigurator;
import net.openhft.affinity.Affinity;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.ThreadParams;
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

    private static final int AFFINITY_BASE = Integer.getInteger("benchmark.affinity.base", -1);
    private static final int AFFINITY_STEP = Integer.getInteger("benchmark.affinity.step", 1);

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
        deleteTempDirectory();
    }

    @Benchmark
    public void baseline(final ThreadState state) {
    }

    @Benchmark
    public void timestamp(final ThreadState state) {
        System.currentTimeMillis();
    }

    @Benchmark
    public void entryWith1Arg(final ThreadState state) {
        LOG.info().append(MESSAGE).commit();
    }

    @Benchmark
    public void templateWith1Arg(final ThreadState state) {
        LOG.info(MESSAGE);
    }

    @Benchmark
    public void entryWith5Args() {
        LOG.info()
                .append("Some array: [")
                .append("string").append(',')
                .append('c').append(',')
                .append(1234567).append(',')
                .append(12345678901234L).append(',')
                .append("string").append(']')
                .commit();
    }

    @Benchmark
    public void templateWith5Args() {
        LOG.info("Some array: [%s, %s, %s, %s, %s]")
                .with("string")
                .with('c')
                .with(1234567)
                .with(12345678901234L)
                .with("string");
    }

    @Benchmark
    public void entryWith10Args() {
        LOG.info()
                .append("Some array: [")
                .append("string").append(',')
                .append('c').append(',')
                .append(1234567).append(',')
                .append(12345678901234L).append(',')
                .append("string").append(',')
                .append("string").append(',')
                .append('c').append(',')
                .append(1234567).append(',')
                .append(12345678901234L).append(',')
                .append("string").append(']')
                .commit();
    }

    @Benchmark
    public void templateWith10Args() {
        LOG.info("Some array: [%s, %s, %s, %s, %s, %s, %s, %s, %s, %s]")
                .with("string")
                .with('c')
                .with(1234567)
                .with(12345678901234L)
                .with("string")
                .with("string")
                .with('c')
                .with(1234567)
                .with(12345678901234L)
                .with("string");
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(ThroughputBenchmark.class.getName())
                // .addProfiler(GCProfiler.class)
                .build();

        new Runner(opt).run();
    }

    private static void deleteTempDirectory() throws Exception {
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
