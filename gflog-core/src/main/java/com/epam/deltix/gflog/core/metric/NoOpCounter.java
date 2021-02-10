package com.epam.deltix.gflog.core.metric;

public final class NoOpCounter implements Counter {

    public static final NoOpCounter INSTANCE = new NoOpCounter();

    private NoOpCounter() {
    }

    @Override
    public void increment() {
    }

}
