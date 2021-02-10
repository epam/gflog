package com.epam.deltix.gflog.core.service;

import static com.epam.deltix.gflog.core.util.Util.LINE_SEPARATOR;


final class LogEntryUtil {

    public static void appendException(final Throwable e, final LogLimitedEntry entry) {
        entry.append(LINE_SEPARATOR);
        appendException(e, entry, 1);
    }

    private static void appendException(final Throwable e, final LogLimitedEntry entry, final int depth) {
        entry.append(e.getClass().getName());

        final String message = e.getMessage();
        if (message != null) {
            entry.append(": ");
            entry.append(message);
        }

        entry.append(LINE_SEPARATOR);

        final StackTraceElement[] stack = e.getStackTrace();
        if (stack != null) {
            appendStack(stack, entry, depth);
        }

        final Throwable[] suppresses = e.getSuppressed();
        if (suppresses != null) {
            appendSuppresses(suppresses, entry, depth);
        }

        final Throwable cause = e.getCause();
        if (cause != null) {
            appendCause(cause, entry, depth);
        }
    }

    private static void appendStack(final StackTraceElement[] stack, final LogLimitedEntry entry, final int depth) {
        for (final StackTraceElement element : stack) {
            appendTabs(depth, entry);

            entry.append("at ");
            entry.append(element.getClassName());
            entry.append('.');
            entry.append(element.getMethodName());
            entry.append('(');

            if (element.isNativeMethod()) {
                entry.append("native");
            } else {
                final String fileName = element.getFileName();
                final int lineNumber = element.getLineNumber();

                if (fileName == null) {
                    entry.append("unknown");
                } else {
                    entry.append(fileName);

                    if (lineNumber >= 0) {
                        entry.append(':');
                        entry.append(lineNumber);
                    }
                }
            }

            entry.append(')');
            entry.append(LINE_SEPARATOR);
        }
    }

    private static void appendSuppresses(final Throwable[] suppresses, final LogLimitedEntry entry, final int depth) {
        for (final Throwable suppress : suppresses) {
            entry.append(LINE_SEPARATOR);
            appendTabs(depth, entry);
            entry.append("suppressed: ");
            appendException(suppress, entry, depth + 1);
        }
    }

    private static void appendCause(final Throwable cause, final LogLimitedEntry entry, final int depth) {
        entry.append(LINE_SEPARATOR);
        appendTabs(depth - 1, entry);
        entry.append("caused by: ");
        appendException(cause, entry, depth);
    }

    private static void appendTabs(final int tabs, final LogLimitedEntry entry) {
        for (int i = 0; i < tabs; i++) {
            entry.append('\t');
        }
    }

}
