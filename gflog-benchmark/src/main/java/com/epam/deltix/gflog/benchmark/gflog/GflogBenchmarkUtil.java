package com.epam.deltix.gflog.benchmark.gflog;

import com.epam.deltix.gflog.api.Log;
import com.epam.deltix.gflog.api.LogFactory;
import com.epam.deltix.gflog.benchmark.util.BenchmarkState;
import com.epam.deltix.gflog.core.LogConfigurator;

import java.util.Properties;

import static com.epam.deltix.gflog.benchmark.util.BenchmarkUtil.*;


final class GflogBenchmarkUtil {

    public static void prepare(final String config, final String encoding) {
        try {
            final String configFile = "classpath:com/epam/deltix/gflog/benchmark/gflog/gflog-" + config + "-benchmark.xml";
            final String tempFile = generateTempFile("gflog-" + config + "-benchmark");

            final Properties properties = new Properties();
            properties.setProperty("temp-file", tempFile);
            properties.setProperty("encoding", encoding);

            deleteTempDirectory();
            LogConfigurator.configure(configFile, properties);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void cleanup() {
        LogConfigurator.unconfigure();
        deleteTempDirectory();
    }

    public static void entry0Arg(final BenchmarkState state) {
        Holder.LOG.info().append("Some array: []").commit();
    }

    public static void template0Arg(final BenchmarkState state) {
        Holder.LOG.info("Some array: []");
    }

    public static void entry1Arg(final BenchmarkState state) {
        Holder.LOG.info()
                .append("Some array: [")
                .append(state.arg0).append(']')
                .commit();
    }

    public static void template1Arg(final BenchmarkState state) {
        Holder.LOG.info("Some array: [%s]")
                .with(state.arg0);
    }

    public static void entry5Args(final BenchmarkState state) {
        Holder.LOG.info()
                .append("Some array: [")
                .append(state.arg0).append(',')
                .append(state.arg1).append(',')
                .append(state.arg2).append(',')
                .append(state.arg3).append(',')
                .append(state.arg4).append(']')
                .commit();
    }

    public static void template5Args(final BenchmarkState state) {
        Holder.LOG.info("Some array: [%s,%s,%s,%s,%s]")
                .with(state.arg0)
                .with(state.arg1)
                .with(state.arg2)
                .with(state.arg3)
                .with(state.arg4);
    }

    public static void entry10Args(final BenchmarkState state) {
        Holder.LOG.info()
                .append("Some array: [")
                .append(state.arg0).append(',')
                .append(state.arg1).append(',')
                .append(state.arg2).append(',')
                .append(state.arg3).append(',')
                .append(state.arg4).append(',')
                .append(state.arg5).append(',')
                .append(state.arg6).append(',')
                .append(state.arg7).append(',')
                .append(state.arg8).append(',')
                .append(state.arg9).append(']')
                .commit();
    }

    public static void template10Args(final BenchmarkState state) {
        Holder.LOG.info("Some array: [%s,%s,%s,%s,%s,%s,%s,%s,%s,%s]")
                .with(state.arg0)
                .with(state.arg1)
                .with(state.arg2)
                .with(state.arg3)
                .with(state.arg4)
                .with(state.arg5)
                .with(state.arg6)
                .with(state.arg7)
                .with(state.arg8)
                .with(state.arg9);
    }

    public static void entryException(final BenchmarkState state) {
        final Throwable exception = state.newException();
        Holder.LOG.info().append("Some exception: ").append(exception).commit();
    }

    public static void templateException(final BenchmarkState state) {
        final Throwable exception = state.newException();
        Holder.LOG.info("Some exception: %s").with(exception);
    }

    private static final class Holder {

        private static final Log LOG = LogFactory.getLog(LOGGER);

    }

}
