package com.epam.deltix.gflog.core.clock;

public final class EpochClock implements Clock {

    public static final EpochClock INSTANCE = new EpochClock();

    private EpochClock() {
    }

    @Override
    public long nanoTime() {
        return System.currentTimeMillis() * 1000000;
    }

}
