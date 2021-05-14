package com.epam.deltix.gflog.benchmark.gflog;

import com.epam.deltix.gflog.api.Log;
import com.epam.deltix.gflog.api.LogFactory;

/**
 * A simple benchmark to validate the code below is inlined on stack (OSR) using JIT Watch.
 * Required JVM opts: -XX:+UnlockDiagnosticVMOptions -XX:+TraceClassLoading -XX:+LogCompilation
 * Optional JVM opts: -Dgflog.entry.encoding=UTF-8
 */
public class GflogInliningBenchmark {

    private static final Log LOG = LogFactory.getLog(GflogInliningBenchmark.class);

    public static void main(final String[] args) {
        entry();
        template();
    }

    private static void entry() {
        for (int i = 0; i < 1000000; i++) { // it is
            LOG.info()
                    .append("String")
                    .append(1234)
                    .append(12345L)
                    .appendTimestamp(0)
                    .appendDecimal64(3539829308347777746L)
                    .appendAlphanumeric(8674652278174253056L)
                    .commit();
        }
    }

    private static void template() { // it is not
        for (int i = 0; i < 1000000; i++) {
            LOG.info("%s, %s, %s, %s, %s, %s")
                    .with("String")
                    .with(1234)
                    .with(12345L)
                    .withTimestamp(0)
                    .withDecimal64(3539829308347777746L)
                    .withAlphanumeric(8674652278174253056L);
        }
    }

}
