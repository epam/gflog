package com.epam.deltix.gflog.core.idle;

import com.epam.deltix.gflog.core.util.Util;


public final class BusySpinIdleStrategy implements IdleStrategy {

    @Override
    public void idle(final int workCount) {
        if (workCount <= 0) {
            Util.onSpinWait();
        }
    }

}
