package com.epam.deltix.gflog.core.idle;

import com.epam.deltix.gflog.core.util.Factory;


public class IdleStrategyFactory implements Factory<IdleStrategy> {

    private long maxSpins = 1;
    private long maxYields = 1;

    private long minParkPeriod = 250 * 1000;
    private long maxParkPeriod = 16 * 1000 * 1000;

    public void setMaxSpins(final long maxSpins) {
        this.maxSpins = maxSpins;
    }

    public void setMaxYields(final long maxYields) {
        this.maxYields = maxYields;
    }

    public void setMinParkPeriod(final long minParkPeriod) {
        this.minParkPeriod = minParkPeriod;
    }

    public void setMaxParkPeriod(final long maxParkPeriod) {
        this.maxParkPeriod = maxParkPeriod;
    }

    public long getMaxSpins() {
        return maxSpins;
    }

    public long getMaxYields() {
        return maxYields;
    }

    public long getMinParkPeriod() {
        return minParkPeriod;
    }

    public long getMaxParkPeriod() {
        return maxParkPeriod;
    }

    public boolean hasMaxSpins() {
        return maxSpins > 0;
    }

    public boolean hasMaxYields() {
        return maxYields > 0;
    }

    public boolean hasMinParkPeriod() {
        return minParkPeriod > 0;
    }

    public boolean hasMaxParkPeriod() {
        return maxParkPeriod > 0;
    }

    @Override
    public IdleStrategy create() {
        if (!hasMaxYields() && !hasMinParkPeriod() && !hasMaxParkPeriod()) {
            return new BusySpinIdleStrategy();
        }

        if (!hasMaxSpins() && hasMaxYields() && !hasMinParkPeriod() && !hasMaxParkPeriod()) {
            return new YieldingIdleStrategy();
        }

        if (!hasMaxSpins() && !hasMaxYields() && hasMinParkPeriod()) {
            if (!hasMaxParkPeriod() || minParkPeriod == maxParkPeriod) {
                return new SleepingIdleStrategy(minParkPeriod);
            }
        }

        return new BackoffIdleStrategy(maxSpins, maxYields, minParkPeriod, maxParkPeriod);
    }

}
