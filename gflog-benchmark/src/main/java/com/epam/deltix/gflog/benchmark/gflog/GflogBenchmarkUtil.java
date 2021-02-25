package com.epam.deltix.gflog.benchmark.gflog;

import com.epam.deltix.gflog.api.Log;
import com.epam.deltix.gflog.api.LogFactory;
import com.epam.deltix.gflog.benchmark.util.BenchmarkState;
import com.epam.deltix.gflog.benchmark.util.BenchmarkUtil;
import com.epam.deltix.gflog.core.LogConfigurator;

import java.util.Properties;

import static com.epam.deltix.gflog.benchmark.util.BenchmarkUtil.LOGGER;
import static com.epam.deltix.gflog.benchmark.util.BenchmarkUtil.MESSAGE;


final class GflogBenchmarkUtil {

    public static void prepare(final String config, final String encoding) {
        try {
            final Properties properties = new Properties();
            properties.setProperty("temp-file", "gflog-" + config + "-benchmark.log");
            properties.setProperty("encoding", encoding);

            final String configFile = "classpath:com/epam/deltix/gflog/benchmark/gflog/gflog-" + config + "-benchmark.xml";
            LogConfigurator.configure(configFile, properties);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void cleanup() {
        try {
            LogConfigurator.unconfigure();
            BenchmarkUtil.deleteTempDirectory();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void entry1Arg(final BenchmarkState state) {
        Holder.LOG.info().append(MESSAGE).commit();
    }

    public static void template1Arg(final BenchmarkState state) {
        Holder.LOG.info(MESSAGE);
    }

    public static void entry5Args(final BenchmarkState state) {
        Holder.LOG.info()
                .append("Some array: [")
                .append(state.arg1).append(',')
                .append(state.arg2).append(',')
                .append(state.arg3).append(',')
                .append(state.arg4).append(',')
                .append(state.arg5).append(']')
                .commit();
    }

    public static void template5Args(final BenchmarkState state) {
        Holder.LOG.info("Some array: [%s,%s,%s,%s,%s]")
                .with(state.arg1)
                .with(state.arg2)
                .with(state.arg3)
                .with(state.arg4)
                .with(state.arg5);
    }

    public static void entry10Args(final BenchmarkState state) {
        Holder.LOG.info()
                .append("Some array: [")
                .append(state.arg1).append(',')
                .append(state.arg2).append(',')
                .append(state.arg3).append(',')
                .append(state.arg4).append(',')
                .append(state.arg5).append(',')
                .append(state.arg6).append(',')
                .append(state.arg7).append(',')
                .append(state.arg8).append(',')
                .append(state.arg9).append(',')
                .append(state.arg10).append(']')
                .commit();
    }

    public static void template10Args(final BenchmarkState state) {
        Holder.LOG.info("Some array: [%s,%s,%s,%s,%s,%s,%s,%s,%s,%s]")
                .with(state.arg1)
                .with(state.arg2)
                .with(state.arg3)
                .with(state.arg4)
                .with(state.arg5)
                .with(state.arg6)
                .with(state.arg7)
                .with(state.arg8)
                .with(state.arg9)
                .with(state.arg10);
    }

    public static void entryException(final BenchmarkState state) {
        Holder.LOG.info().append("Exception: ").append(state.exception).commit();
    }

    public static void templateException(final BenchmarkState state) {
        Holder.LOG.info("Exception: %s").with(state.exception);
    }

    private static final class Holder {

        private static final Log LOG = LogFactory.getLog(LOGGER);

    }

}
