package com.epam.deltix.gflog.core.clock;

import com.epam.deltix.gflog.api.LogDebug;
import com.epam.deltix.gflog.core.util.Factory;
import com.epam.deltix.gflog.core.util.PropertyUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class ClockFactory implements Factory<Clock> {

    private static final Resolution RESOLUTION = getDefaultResolution();

    protected Resolution resolution = RESOLUTION;

    public Resolution getResolution() {
        return resolution;
    }

    public void setResolution(final Resolution resolution) {
        this.resolution = resolution;
    }

    @Override
    public Clock create() {
        if (resolution == Resolution.HIGH && NativeClocks.HANDLE_REALTIME_TIME != null) {
            return NativeClocks::realtimeTime;
        }

        if (resolution == Resolution.LOW && NativeClocks.HANDLE_REALTIME_COARSE_TIME != null) {
            return NativeClocks::realtimeCoarseTime;
        }

        return EpochClock.INSTANCE;
    }

    private static Resolution getDefaultResolution() {
        final String resolution = PropertyUtil.getString("gflog.clock.resolution", null);

        if (Resolution.HIGH.name().equals(resolution)) {
            return Resolution.HIGH;
        } else if (Resolution.LOW.name().equals(resolution)) {
            return Resolution.LOW;
        } else {
            return null;
        }
    }

    public enum Resolution {
        HIGH, LOW
    }

    private static final class NativeClocks {

        private final static MethodHandle HANDLE_REALTIME_TIME;
        private final static MethodHandle HANDLE_REALTIME_COARSE_TIME;

        static {
            MethodHandle precise = null;
            MethodHandle coarse = null;

            try {
                final Class<?> clazz = Class.forName("deltix.clock.Clocks");
                precise = lookupHandle(clazz, "REALTIME");
                coarse = lookupHandle(clazz, "REALTIME_COARSE");
            } catch (final ClassNotFoundException e) {
                // ignore
            } catch (final Throwable e) {
                LogDebug.warn("can't access native clocks: " + e.getMessage());
            }

            HANDLE_REALTIME_TIME = precise;
            HANDLE_REALTIME_COARSE_TIME = coarse;
        }

        private static long realtimeTime() {
            try {
                return (long) HANDLE_REALTIME_TIME.invoke();
            } catch (final Throwable throwable) {
                throw new IllegalStateException();
            }
        }

        private static long realtimeCoarseTime() {
            try {
                return (long) HANDLE_REALTIME_COARSE_TIME.invoke();
            } catch (final Throwable throwable) {
                throw new IllegalStateException();
            }
        }

        private static MethodHandle lookupHandle(final Class<?> clazz, final String key) throws Throwable {
            final Field field = clazz.getField(key);
            final Object clock = field.get(null);
            final Class<?> clockType = clock.getClass();

            final Method methodAvailable = clockType.getMethod("available"); // public method
            methodAvailable.setAccessible(true);

            final boolean available = (boolean) methodAvailable.invoke(clock);

            if (available) {
                final Method method = clockType.getDeclaredMethod("getTime"); // native method
                method.setAccessible(true);

                return MethodHandles.lookup().unreflect(method);
            }

            return null;
        }

    }

}
