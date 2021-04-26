package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.core.util.PropertyUtil;

import static com.epam.deltix.gflog.core.util.Util.LINE_SEPARATOR;


final class LogEntryUtil {

    private static final int DEPTH_LIMIT = PropertyUtil.getInteger("gflog.exception.depth.limit", 16);

    public static void appendException(final Throwable e, final LogLimitedEntry entry) {
        entry.append(LINE_SEPARATOR);
        appendException(e, entry, 1, 1);
    }

    private static void appendException(final Throwable e,
                                        final LogLimitedEntry entry,
                                        final int indent,
                                        final int depth) {

        if (entry.truncated()) {
            return;
        }

        if (depth > DEPTH_LIMIT) {
            entry.append(">>(Cyclic Exception?)>>");
            return;
        }

        entry.append(e.getClass().getName());

        final String message = e.getMessage();
        if (message != null) {
            entry.append(": ");
            entry.append(message);
        }

        entry.append(LINE_SEPARATOR);

        final StackTraceElement[] stack = e.getStackTrace();
        if (stack != null) {
            appendStack(stack, entry, indent);
        }

        final Throwable[] suppresses = e.getSuppressed();
        if (suppresses != null) {
            appendSuppresses(suppresses, entry, indent, depth);
        }

        final Throwable cause = e.getCause();
        if (cause != null) {
            appendCause(cause, entry, indent, depth);
        }
    }

    private static void appendStack(final StackTraceElement[] stack,
                                    final LogLimitedEntry entry,
                                    final int indent) {

        for (final StackTraceElement element : stack) {
            appendTabs(indent, entry);

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

    private static void appendSuppresses(final Throwable[] suppresses,
                                         final LogLimitedEntry entry,
                                         final int indent,
                                         final int depth) {

        for (final Throwable suppress : suppresses) {
            entry.append(LINE_SEPARATOR);
            appendTabs(indent, entry);
            entry.append("suppressed: ");
            appendException(suppress, entry, indent + 1, depth + 1);
        }
    }

    private static void appendCause(final Throwable cause,
                                    final LogLimitedEntry entry,
                                    final int indent,
                                    final int depth) {

        entry.append(LINE_SEPARATOR);
        appendTabs(indent - 1, entry);
        entry.append("caused by: ");
        appendException(cause, entry, indent, depth + 1);
    }

    private static void appendTabs(final int tabs, final LogLimitedEntry entry) {
        for (int i = 0; i < tabs; i++) {
            entry.append('\t');
        }
    }

}
