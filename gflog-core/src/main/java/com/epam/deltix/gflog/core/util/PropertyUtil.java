package com.epam.deltix.gflog.core.util;

import com.epam.deltix.gflog.api.LogDebug;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


public final class PropertyUtil {

    private static final String SUBSTITUTION_START = "${";
    private static final char SUBSTITUTION_END = '}';

    private PropertyUtil() {
    }

    public static String getString(final String name, final String defaultValue) {
        return System.getProperty(name, defaultValue);
    }

    public static boolean getBoolean(final String name, boolean defaultValue) {
        final String value = getString(name, null);

        if (value != null) {
            try {
                defaultValue = toBoolean(value);
            } catch (final Throwable e) {
                LogDebug.warn(String.format("boolean property \"%s\" with invalid value \"%s\". Expected true/false", name, value));
            }
        }

        return defaultValue;
    }

    public static int getInteger(final String name, int defaultValue) {
        final String value = getString(name, null);

        if (value != null) {
            try {
                defaultValue = Integer.parseInt(value);
            } catch (final Throwable e) {
                LogDebug.warn(String.format("integer property \"%s\" with invalid value \"%s\"", name, value));
            }
        }

        return defaultValue;
    }

    public static int getMemory(final String name, int defaultValue) {
        final String value = getString(name, null);

        if (value != null) {
            try {
                defaultValue = Math.toIntExact(toMemory(value));
            } catch (final Throwable e) {
                LogDebug.warn(String.format("memory property \"%s\" with invalid value \"%s\". " +
                        "Expected integer with optional suffix k/kb/m/mb/g/gb or K/KB/M/MB/G/GB", name, value));
            }
        }

        return defaultValue;
    }

    public static long getMemory(final String name, long defaultValue) {
        final String value = getString(name, null);

        if (value != null) {
            try {
                defaultValue = toMemory(value);
            } catch (final Throwable e) {
                LogDebug.warn(String.format("memory property \"%s\" with invalid value \"%s\". " +
                        "Expected integer with optional suffix k/kb/m/mb/g/gb or K/KB/M/MB/G/GB", name, value));
            }
        }

        return defaultValue;
    }

    public static long getDuration(final String name, final TimeUnit units, long defaultValue) {
        final String value = getString(name, null);

        if (value != null) {
            try {
                final Duration duration = toDuration(value);
                defaultValue = units.convert(duration.toNanos(), TimeUnit.NANOSECONDS);
            } catch (final Throwable e) {
                LogDebug.warn(String.format("duration property \"%s\" with invalid value \"%s\". " +
                        "Expected integer with optional suffix d/h/m/s/ms/us/ns", name, value));
            }
        }

        return defaultValue;
    }

    public static boolean toBoolean(final String value) {
        if ("true".equals(value)) {
            return true;
        } else if ("false".equals(value)) {
            return false;
        }

        throw new IllegalArgumentException("Unsupported boolean value: " + value);
    }

    public static long toMemory(final String value) {
        long multiplier = 1;
        int end = value.length();

        if (value.endsWith("K") || value.endsWith("k")) {
            multiplier = 1024;
            end = end - 1;
        } else if (value.endsWith("KB") || value.endsWith("kb")) {
            multiplier = 1024;
            end = end - 2;
        } else if (value.endsWith("M") || value.endsWith("m")) {
            multiplier = 1024 * 1024;
            end = end - 1;
        } else if (value.endsWith("MB") || value.endsWith("mb")) {
            multiplier = 1024 * 1024;
            end = end - 2;
        } else if (value.endsWith("G") || value.endsWith("g")) {
            multiplier = 1024 * 1024 * 1024;
            end = end - 1;
        } else if (value.endsWith("GB") || value.endsWith("gb")) {
            multiplier = 1024 * 1024 * 1024;
            end = end - 2;
        }

        return Math.multiplyExact(multiplier, Long.parseLong(value.substring(0, end)));
    }

    public static Duration toDuration(final String value) {
        TimeUnit units = TimeUnit.MILLISECONDS;
        int end = value.length();

        if (value.endsWith("ms")) {
            end = end - 2;
        } else if (value.endsWith("us")) {
            units = TimeUnit.MICROSECONDS;
            end = end - 2;
        } else if (value.endsWith("ns")) {
            units = TimeUnit.NANOSECONDS;
            end = end - 2;
        } else if (value.endsWith("s")) {
            units = TimeUnit.SECONDS;
            end = end - 1;
        } else if (value.endsWith("m")) {
            units = TimeUnit.MINUTES;
            end = end - 1;
        } else if (value.endsWith("h")) {
            units = TimeUnit.HOURS;
            end = end - 1;
        } else if (value.endsWith("d")) {
            units = TimeUnit.DAYS;
            end = end - 1;
        }

        final long duration = Long.parseUnsignedLong(value.substring(0, end));
        return Duration.ofNanos(units.toNanos(duration));
    }

    public static String substitute(final String value, final Properties properties) {
        if (value.contains(SUBSTITUTION_START)) {
            final StringBuilder builder = new StringBuilder();

            final int length = value.length();
            int i = 0;

            while (true) {
                final int j = value.indexOf(SUBSTITUTION_START, i);
                if (j == -1) {
                    break;
                }

                final int k = j + SUBSTITUTION_START.length();
                final int m = value.indexOf(SUBSTITUTION_END, k);
                if (m == -1) {
                    break;
                }

                if (i < j) {
                    builder.append(value, i, j);
                }

                final String name = value.substring(k, m);
                final String substitution = properties.getProperty(name, "");

                builder.append(substitution);
                i = m + 1;
            }

            if (i < length) {
                builder.append(value, i, length);
            }

            return builder.toString();
        }

        return value;
    }

}
