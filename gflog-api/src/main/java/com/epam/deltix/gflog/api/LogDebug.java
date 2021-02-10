package com.epam.deltix.gflog.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;


public final class LogDebug {

    private static final String DEBUG_PREFIX = "gflog debug: ";
    private static final String WARN_PREFIX = "gflog warn: ";

    private static final boolean VERBOSE = Boolean.getBoolean("gflog.debug.verbose");
    private static final boolean QUIET = Boolean.getBoolean("gflog.debug.quiet");
    private static final int MESSAGES = Integer.getInteger("gflog.debug.messages", 100);
    private static final String FILE = System.getProperty("gflog.debug.file");

    private static final PrintStream OUTPUT = createOutput();
    private static final AtomicInteger REMAINING = new AtomicInteger(MESSAGES);

    private LogDebug() {
    }

    public static boolean isDebugEnabled() {
        return !QUIET && VERBOSE && REMAINING.get() > 0;
    }

    public static void debug(final String message) {
        debug(message, null);
    }

    public static void debug(final Throwable e) {
        debug(e.getMessage(), e);
    }

    public static void debug(final String message, final Throwable exception) {
        if (isDebugEnabled()) {
            log(DEBUG_PREFIX, message, exception);
        }
    }

    public static boolean isWarnEnabled() {
        return !QUIET && REMAINING.get() > 0;
    }

    public static void warn(final String message) {
        warn(message, null);
    }

    public static void warn(final Throwable e) {
        warn(e.getMessage(), e);
    }

    public static void warn(final String message, final Throwable exception) {
        if (isWarnEnabled()) {
            log(WARN_PREFIX, message, exception);
        }
    }

    private static void log(final String prefix, final String message, final Throwable exception) {
        if (REMAINING.getAndDecrement() > 0) {
            try {
                OUTPUT.println(prefix + message);

                if (exception != null) {
                    exception.printStackTrace(OUTPUT);
                }
            } catch (final Throwable e) {
                REMAINING.set(0);
            }
        }
    }

    private static PrintStream createOutput() {
        PrintStream output = System.err;

        if (FILE != null) {
            try {
                final File file = new File(FILE);
                final File directory = file.getParentFile();

                if (directory != null) {
                    directory.mkdirs();
                }

                output = new PrintStream(new FileOutputStream(FILE, false));
            } catch (final Throwable e) {
                output.printf("%scan't create output stream to specified file: %s, error: %s%n", WARN_PREFIX, FILE, e.getMessage());
                e.printStackTrace(output);
            }
        }

        return output;
    }

}
