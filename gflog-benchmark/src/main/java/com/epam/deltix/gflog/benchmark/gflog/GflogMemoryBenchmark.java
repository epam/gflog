package com.epam.deltix.gflog.benchmark.gflog;

import com.epam.deltix.gflog.api.Log;
import com.epam.deltix.gflog.benchmark.util.BenchmarkUtil;
import com.epam.deltix.gflog.benchmark.util.Generator;
import org.openjdk.jol.vm.VM;

import static com.epam.deltix.gflog.benchmark.gflog.GflogBenchmarkUtil.cleanup;
import static com.epam.deltix.gflog.benchmark.gflog.GflogBenchmarkUtil.prepare;
import static com.epam.deltix.gflog.benchmark.util.BenchmarkUtil.gc;


public class GflogMemoryBenchmark {

    private static final int[] MESSAGES_LIMIT = {
            1,
            10,
            100,
            1000,
            10000,
            100000,
            1000000,
            10000000,
            100000000,
            1000000000
    };

    public static void main(final String[] args) throws Exception {
        gc("Before prepare - gflog is not initialized");
        prepare("noop", "UTF-8");

        try {
            gc("Before experiment - gflog is initialized");
            run();
            gc("After experiment - gflog is not yet destroyed");
        } finally {
            cleanup();
        }

        gc("After cleanup - gflog is destroyed");
    }

    private static void run() throws Exception {
        System.out.println(VM.current().details());

        final Generator generator = new Generator();
        final Log log = GflogBenchmarkUtil.getLog();

        int messages = 0;

        for (final int limit : MESSAGES_LIMIT) {
            for (; messages < limit; messages++) {
                final StringBuilder message = generator.nextMessage();
                log.info("%s").with(message);
            }

            Thread.sleep(1000);

            final String footprint = BenchmarkUtil.memoryFootprint(log);
            System.out.printf("%nMessages: %s. Memory: %s%n", messages, footprint);
        }
    }

}
