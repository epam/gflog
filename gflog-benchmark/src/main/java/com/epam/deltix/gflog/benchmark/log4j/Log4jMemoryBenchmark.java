package com.epam.deltix.gflog.benchmark.log4j;

import com.epam.deltix.gflog.benchmark.util.BenchmarkUtil;
import com.epam.deltix.gflog.benchmark.util.Generator;
import org.apache.logging.log4j.Logger;
import org.openjdk.jol.vm.VM;

import static com.epam.deltix.gflog.benchmark.log4j.Log4jBenchmarkUtil.cleanup;
import static com.epam.deltix.gflog.benchmark.log4j.Log4jBenchmarkUtil.prepare;


public class Log4jMemoryBenchmark {

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
        prepare("noop");

        try {
            run();
        } finally {
            cleanup();
        }
    }

    private static void run() throws Exception {
        System.out.println(VM.current().details());

        final Generator generator = new Generator();
        final Logger log = Log4jBenchmarkUtil.getLogger();

        int messages = 0;

        for (final int limit : MESSAGES_LIMIT) {
            for (; messages < limit; messages++) {
                final StringBuilder message = generator.nextMessage();
                log.info("{}", message);
            }

            Thread.sleep(1000);

            final String footprint = BenchmarkUtil.memoryFootprint(log);
            System.out.printf("%nMessages: %s. Memory: %s%n", messages, footprint);
        }
    }

}
