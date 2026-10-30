package com.epam.deltix.gflog.jul;

import com.epam.deltix.gflog.api.LogEntry;


/**
 * Writes a JUL message to a log entry without building an intermediate string.
 * {@code {0}} is a zero-based MessageFormat argument. Format types such as {@code {0,number}}
 * are left unchanged. Printf conversions with no flags, width, or precision are substituted when
 * the value can be written directly: {@code %s}, {@code %d}, {@code %b}, an explicit index such as
 * {@code %1$s}, and {@code %%}. {@code %s} takes the next argument. Explicit indexes are one-based.
 * Anything else is left unchanged.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
final class JulMessageAppender {

    private JulMessageAppender() {
    }

    static void append(final String message, final Object param, final LogEntry entry) {
        if (message == null) {
            entry.append(message);
            return;
        }

        append(message, param, null, entry);
    }

    static void append(final String message, final Object[] params, final LogEntry entry) {
        if (message == null || params == null || params.length == 0) {
            entry.append(message);
            return;
        }

        append(message, null, params, entry);
    }

    private static void append(final String message,
                               final Object single,
                               final Object[] params,
                               final LogEntry entry) {
        int searchFrom = 0;
        int appendFrom = 0;
        int implicit = 0;
        final int length = message.length();

        while (searchFrom < length) {
            final int brace = message.indexOf('{', searchFrom);
            final int percent = message.indexOf('%', searchFrom);
            if (brace < 0 && percent < 0) {
                break;
            }

            if (percent < 0 || (brace >= 0 && brace < percent)) {
                if (brace + 1 >= length || !isDigit(message.charAt(brace + 1))) {
                    searchFrom = brace + 1;
                    continue;
                }

                final int end = message.indexOf('}', brace + 2);
                if (end < 0) {
                    break;
                }

                searchFrom = end + 1;
                final int index = indexBetween(message, brace + 1, end);
                if (index < 0 || !hasParam(index, single, params)) {
                    continue;
                }

                appendText(message, appendFrom, brace, entry);
                entry.append(paramAt(index, single, params));
                appendFrom = searchFrom;
                continue;
            }

            final int cursor = percent + 1;
            if (cursor >= length) {
                break;
            }

            final char marker = message.charAt(cursor);
            if (marker == '%') {
                appendText(message, appendFrom, percent, entry);
                appendText(message, percent, cursor, entry);
                appendFrom = cursor + 1;
                searchFrom = appendFrom;
                continue;
            }

            final int argIndex;
            final int specEnd;
            final char conversion;
            if (isDigit(marker)) {
                final int dollar = skipDigits(message, cursor);
                final int conversionAt = dollar + 1;
                if (dollar >= length || message.charAt(dollar) != '$' || conversionAt >= length) {
                    searchFrom = cursor;
                    continue;
                }

                final int explicit = indexBetween(message, cursor, dollar);
                conversion = message.charAt(conversionAt);
                if (explicit < 0 || !isSupportedConversion(conversion)) {
                    searchFrom = cursor;
                    continue;
                }

                argIndex = explicit - 1;
                specEnd = conversionAt + 1;
            } else if (isSupportedConversion(marker)) {
                argIndex = implicit++;
                conversion = marker;
                specEnd = cursor + 1;
            } else {
                searchFrom = cursor;
                continue;
            }

            searchFrom = specEnd;
            if (argIndex < 0 || !hasParam(argIndex, single, params)) {
                continue;
            }

            final Object param = paramAt(argIndex, single, params);
            if (!canAppend(conversion, param)) {
                continue;
            }

            appendText(message, appendFrom, percent, entry);
            appendConverted(conversion, param, entry);
            appendFrom = specEnd;
        }

        appendText(message, appendFrom, length, entry);
    }

    private static void appendText(final String message, final int from, final int to, final LogEntry entry) {
        if (from < to) {
            entry.append(message, from, to);
        }
    }

    private static boolean canAppend(final char conversion, final Object param) {
        return conversion == 's' || conversion == 'b' || isIntegral(param);
    }

    private static boolean isIntegral(final Object param) {
        return param instanceof Long ||
                param instanceof Integer ||
                param instanceof Short ||
                param instanceof Byte;
    }

    private static void appendConverted(final char conversion, final Object param, final LogEntry entry) {
        if (conversion == 'b') {
            appendBoolean(param, entry);
        } else if (conversion == 'd') {
            appendIntegral(param, entry);
        } else {
            entry.append(param);
        }
    }

    private static void appendIntegral(final Object param, final LogEntry entry) {
        if (param instanceof Long) {
            entry.append(((Long) param).longValue());
        } else if (param instanceof Integer) {
            entry.append(((Integer) param).intValue());
        } else if (param instanceof Short) {
            entry.append(((Short) param).intValue());
        } else {
            entry.append(((Byte) param).intValue());
        }
    }

    private static void appendBoolean(final Object param, final LogEntry entry) {
        final boolean value;
        if (param instanceof Boolean) {
            value = ((Boolean) param).booleanValue();
        } else {
            value = param != null;
        }

        entry.append(value);
    }

    private static boolean isSupportedConversion(final char conversion) {
        return conversion == 's' || conversion == 'd' || conversion == 'b';
    }

    private static boolean hasParam(final int index, final Object single, final Object[] params) {
        if (index < 0) {
            return false;
        }

        if (params != null) {
            return index < params.length;
        }

        return index == 0;
    }

    private static Object paramAt(final int index, final Object single, final Object[] params) {
        return (params != null) ? params[index] : single;
    }

    private static int skipDigits(final String message, final int start) {
        int index = start;
        final int length = message.length();

        while (index < length) {
            final char c = message.charAt(index);
            if (c < '0' | c > '9') {
                break;
            }

            index++;
        }

        return index;
    }

    private static int indexBetween(final String message, final int start, final int end) {
        int index = 0;

        for (int i = start; i < end; i++) {
            final char c = message.charAt(i);
            if (c < '0' | c > '9') {
                return -1;
            }

            final int digit = c - '0';
            if (index > (Integer.MAX_VALUE - digit) / 10) {
                return -1;
            }

            index = 10 * index + digit;
        }

        return index;
    }

    private static boolean isDigit(final char c) {
        return c >= '0' && c <= '9';
    }

}
