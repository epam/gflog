package com.epam.deltix.gflog.core.idle;

import com.epam.deltix.gflog.core.util.Util;

import java.util.concurrent.locks.LockSupport;

@SuppressWarnings("unused")
abstract class BackoffIdleStrategyLhs {
    byte b000, b001, b002, b003, b004, b005, b006, b007, b008, b009, b010, b011, b012, b013, b014, b015,
            b016, b017, b018, b019, b020, b021, b022, b023, b024, b025, b026, b027, b028, b029, b030, b031,
            b032, b033, b034, b035, b036, b037, b038, b039, b040, b041, b042, b043, b044, b045, b046, b047,
            b048, b049, b050, b051, b052, b053, b054, b055, b056, b057, b058, b059, b060, b061, b062, b063,
            b064, b065, b066, b067, b068, b069, b070, b071, b072, b073, b074, b075, b076, b077, b078, b079,
            b080, b081, b082, b083, b084, b085, b086, b087, b088, b089, b090, b091, b092, b093, b094, b095,
            b096, b097, b098, b099, b100, b101, b102, b103, b104, b105, b106, b107, b108, b109, b110, b111,
            b112, b113, b114, b115, b116, b117, b118, b119, b120, b121, b122, b123, b124, b125, b126, b127;
}

abstract class BackoffIdleStrategyData extends BackoffIdleStrategyLhs {

    static final int WORKING = 0;
    static final int SPINNING = 1;
    static final int YIELDING = 2;
    static final int PARKING = 3;

    final long maxSpins;
    final long maxYields;
    final long minParkPeriodNs;
    final long maxParkPeriodNs;

    int state = WORKING;
    long value;

    BackoffIdleStrategyData(
            final long maxSpins, final long maxYields, final long minParkPeriodNs, final long maxParkPeriodNs) {

        if (minParkPeriodNs < 1 || maxParkPeriodNs < minParkPeriodNs) {
            throw new IllegalArgumentException("Min park period " + minParkPeriodNs + " < 1 or max park period " + maxParkPeriodNs + " < min");
        }

        this.maxSpins = maxSpins;
        this.maxYields = maxYields;
        this.minParkPeriodNs = minParkPeriodNs;
        this.maxParkPeriodNs = maxParkPeriodNs;
    }
}

@SuppressWarnings("unused")
public final class BackoffIdleStrategy extends BackoffIdleStrategyData implements IdleStrategy {

    byte b000, b001, b002, b003, b004, b005, b006, b007, b008, b009, b010, b011, b012, b013, b014, b015,
            b016, b017, b018, b019, b020, b021, b022, b023, b024, b025, b026, b027, b028, b029, b030, b031,
            b032, b033, b034, b035, b036, b037, b038, b039, b040, b041, b042, b043, b044, b045, b046, b047,
            b048, b049, b050, b051, b052, b053, b054, b055, b056, b057, b058, b059, b060, b061, b062, b063,
            b064, b065, b066, b067, b068, b069, b070, b071, b072, b073, b074, b075, b076, b077, b078, b079,
            b080, b081, b082, b083, b084, b085, b086, b087, b088, b089, b090, b091, b092, b093, b094, b095,
            b096, b097, b098, b099, b100, b101, b102, b103, b104, b105, b106, b107, b108, b109, b110, b111,
            b112, b113, b114, b115, b116, b117, b118, b119, b120, b121, b122, b123, b124, b125, b126, b127;

    public BackoffIdleStrategy(final long maxSpins, final long maxYields, final long minParkPeriodNs, final long maxParkPeriodNs) {
        super(maxSpins, maxYields, minParkPeriodNs, maxParkPeriodNs);
    }

    @Override
    public void idle(final int workCount) {
        if (workCount > 0) {
            reset();
        } else {
            idle();
        }
    }

    private void idle() {
        switch (state) {
            case WORKING:
                value = 0;
                state = SPINNING;
                // fallthrough

            case SPINNING:
                if (++value <= maxSpins) {
                    Util.onSpinWait();
                    break;
                }

                value = 0;
                state = YIELDING;
                // fallthrough

            case YIELDING:
                if (++value <= maxYields) {
                    Thread.yield();
                    break;
                }

                value = minParkPeriodNs;
                state = PARKING;
                // fallthrough

            case PARKING:
                LockSupport.parkNanos(value);
                value = Math.min(value << 1, maxParkPeriodNs);
        }
    }

    private void reset() {
        state = WORKING;
    }

}



