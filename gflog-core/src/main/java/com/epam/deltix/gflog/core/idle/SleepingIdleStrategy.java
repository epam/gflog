package com.epam.deltix.gflog.core.idle;

import java.util.concurrent.locks.LockSupport;


public final class SleepingIdleStrategy implements IdleStrategy {

    private final long periodNs;

    public SleepingIdleStrategy(final long periodNs) {
        this.periodNs = periodNs;
    }

    @Override
    public void idle(final int workCount) {
        if (workCount <= 0) {
            LockSupport.parkNanos(periodNs);
        }
    }

}
