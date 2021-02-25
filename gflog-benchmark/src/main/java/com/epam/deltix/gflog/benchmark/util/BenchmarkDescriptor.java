package com.epam.deltix.gflog.benchmark.util;

import java.util.function.Consumer;


public final class BenchmarkDescriptor {

    private final String name;
    private final Runnable prepare;
    private final Runnable cleanup;
    private final Consumer<BenchmarkState> command;

    public BenchmarkDescriptor(final String name,
                               final Runnable prepare,
                               final Runnable cleanup,
                               final Consumer<BenchmarkState> command) {
        this.name = name;
        this.prepare = prepare;
        this.cleanup = cleanup;
        this.command = command;
    }

    public String getName() {
        return name;
    }

    public Runnable getPrepare() {
        return prepare;
    }

    public Runnable getCleanup() {
        return cleanup;
    }

    public Consumer<BenchmarkState> getCommand() {
        return command;
    }

}
