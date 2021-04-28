package com.epam.deltix.gflog.benchmark.log4j;

import com.epam.deltix.gflog.benchmark.util.Allocator;
import com.epam.deltix.gflog.benchmark.util.BenchmarkUtil;
import com.epam.deltix.gflog.benchmark.util.Generator;
import org.apache.logging.log4j.Logger;
import org.openjdk.jol.vm.VM;

import static com.epam.deltix.gflog.benchmark.log4j.Log4jBenchmarkUtil.cleanup;
import static com.epam.deltix.gflog.benchmark.log4j.Log4jBenchmarkUtil.prepare;


public class Log4jMemoryBenchmark {

    private static final int[] MESSAGES_LIMITS = {1_000_000, 10_000_000, 100_000_000, 1_000_000_000};

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

        final Allocator allocator = Allocator.install();
        final Generator generator = new Generator();
        final Logger log = Log4jBenchmarkUtil.getLogger();

        log.info("{}", generator.nextMessage());
        System.out.println("Messages: 1. " + BenchmarkUtil.memoryFootprint(log));

        int messages = 1;

        for (final int messagesLimit : MESSAGES_LIMITS) {
            allocator.start();

            for (; messages < messagesLimit; messages++) {
                final StringBuilder message = generator.nextMessage();
                log.info("{}", message);
            }

            Thread.sleep(1000);
            allocator.stop();

            System.out.println();
            System.out.println("Messages: " + messages);

            System.out.println();
            System.out.println("Footprint: " + BenchmarkUtil.memoryFootprint(log));

            System.out.println();
            System.out.println("Allocations: " + allocator.toFootprint());
        }
    }

}
