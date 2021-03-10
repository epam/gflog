package com.epam.deltix.gflog.benchmark.log4j;

import com.epam.deltix.gflog.benchmark.util.BenchmarkState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.epam.deltix.gflog.benchmark.util.BenchmarkUtil.*;
import static org.apache.logging.log4j.util.Unbox.box;


final class Log4jBenchmarkUtil {

    public static void prepare(final String config) {
        //System.setProperty("log4j.debug", "true");
        System.setProperty("log4j.configurationFile", "com/epam/deltix/gflog/benchmark/log4j/log4j-" + config + "-benchmark.xml");
        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        System.setProperty("AsyncLogger.RingBufferSize", "262144");
        System.setProperty("AsyncLogger.WaitStrategy", "Yield");
        System.setProperty("log4j2.enable.threadlocals", "true");
        System.setProperty("log4j2.enable.direct.encoders", "true");
        System.setProperty("temp-file", generateTempFile("log4j-" + config + "-benchmark"));

        deleteTempDirectory();
        LogManager.exists(LOGGER); // forces lazy initialization
    }

    public static void cleanup() {
        try {
            LogManager.shutdown();
            deleteTempDirectory();
        } catch (final Exception e) {
            // ignore
        }
    }

    public static void log0Arg(final BenchmarkState state) {
        Holder.LOG.info("Some array: []");
    }

    public static void log1Arg(final BenchmarkState state) {
        Holder.LOG.info("Some array: [{}]", state.arg0);
    }

    public static void log5Args(final BenchmarkState state) {
        Holder.LOG.info("Some array: [{},{},{},{},{}]",
                state.arg0, box(state.arg1), box(state.arg2), box(state.arg3), state.arg4
        );
    }

    public static void log10Args(final BenchmarkState state) {
        Holder.LOG.info("Some array: [{},{},{},{},{},{},{},{},{},{}]",
                state.arg0, box(state.arg1), box(state.arg2), box(state.arg3), state.arg4,
                state.arg5, box(state.arg6), box(state.arg7), box(state.arg8), state.arg9
        );
    }

    public static void logException(final BenchmarkState state) {
        final Throwable exception = state.newException();
        Holder.LOG.info("Some exception: ", exception);
    }

    public static void logCachedException(final BenchmarkState state) {
        final Throwable exception = state.exception;
        Holder.LOG.info("Some exception: ", exception);
    }

    private static final class Holder {

        private static final Logger LOG = LogManager.getLogger(LOGGER);

    }

}
