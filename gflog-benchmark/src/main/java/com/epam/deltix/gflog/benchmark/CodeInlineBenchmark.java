package com.epam.deltix.gflog.benchmark;

import com.epam.deltix.gflog.api.Log;
import com.epam.deltix.gflog.api.LogFactory;

/**
 * A simple benchmark to validate the code below is inlined on stack (OSR) using JIT Watch.
 * Required JVM opts: -XX:+UnlockDiagnosticVMOptions -XX:+TraceClassLoading -XX:+LogCompilation
 * Optional JVM opts: -Dgflog.entry.encoding=UTF-8
 */
public class CodeInlineBenchmark {

    private static final Log LOG = LogFactory.getLog(CodeInlineBenchmark.class);

    public static void main(final String[] args) {

        for (int i = 0; i < 10000000; i++) {
            LOG.info()
                    .append("String")
                    .append(1234)
                    .append(12345L)
                    .appendTimestamp(0)
                    .appendDecimal64(412344324321L)
                    .appendAlphanumeric(432142342344343L)
                    .commit();
        }
    }

}
