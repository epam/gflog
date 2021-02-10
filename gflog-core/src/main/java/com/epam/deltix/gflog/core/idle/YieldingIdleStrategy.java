package com.epam.deltix.gflog.core.idle;

public final class YieldingIdleStrategy implements IdleStrategy {

    @Override
    public void idle(final int workCount) {
        if (workCount <= 0) {
            Thread.yield();
        }
    }

}
