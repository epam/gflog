package com.epam.deltix.gflog.jul;

import com.epam.deltix.gflog.api.Log;
import com.epam.deltix.gflog.api.LogEntry;
import com.epam.deltix.gflog.api.LogFactory;
import com.epam.deltix.gflog.api.LogLevel;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


/**
 * Implemented in the same way as log4j-jul bridge.
 */
final class JulBridgeLogger extends Logger {

    private final Log log;

    JulBridgeLogger(final String name) {
        super(name, null);
        this.log = LogFactory.getLog(name);
    }

    @Override
    public void log(final LogRecord record) {
        final LogLevel level = getLogLevel(record.getLevel());
        if (log.isEnabled(level)) {
            final LogEntry entry = log.log(level);
            appendMessage(record.getMessage(), record.getParameters(), entry);
            appendException(record.getThrown(), entry);
            entry.commit();
        }
    }

    @Override
    public boolean isLoggable(final Level level) {
        final LogLevel logLevel = getLogLevel(level);
        return log.isEnabled(logLevel);
    }

    @Override
    public void setLevel(final Level newLevel) {
        // skip
    }

    @Override
    public void setParent(final Logger parent) {
        throw new UnsupportedOperationException("Can't set parent logger");
    }

    @Override
    public void log(final Level level, final String msg) {
        final LogLevel logLevel = getLogLevel(level);
        doLog(logLevel, msg);
    }

    @Override
    public void log(final Level level, final String msg, final Object param1) {
        final LogLevel logLevel = getLogLevel(level);
        if (log.isEnabled(logLevel)) {
            final LogEntry entry = log.log(logLevel);
            appendMessageWithParam(msg, param1, entry);
            entry.commit();
        }
    }

    @Override
    public void log(final Level level, final String msg, final Object[] params) {
        final LogLevel logLevel = getLogLevel(level);
        if (log.isEnabled(logLevel)) {
            final LogEntry entry = log.log(logLevel);
            appendMessage(msg, params, entry);
            entry.commit();
        }
    }

    @Override
    public void log(final Level level, final String msg, final Throwable thrown) {
        final LogLevel logLevel = getLogLevel(level);
        if (log.isEnabled(logLevel)) {
            final LogEntry entry = log.log(logLevel);
            appendMessage(msg, null, entry);
            appendException(thrown, entry);
            entry.commit();
        }
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg) {
        log(level, msg);
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg,
                     final Object param1) {
        log(level, msg, param1);
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg,
                     final Object[] params) {
        log(level, msg, params);
    }

    @Override
    public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg,
                     final Throwable thrown) {
        log(level, msg, thrown);
    }

    @Override
    public void severe(final String msg) {
        doLog(LogLevel.ERROR, msg);
    }

    @Override
    public void warning(final String msg) {
        doLog(LogLevel.WARN, msg);
    }

    @Override
    public void info(final String msg) {
        doLog(LogLevel.INFO, msg);
    }

    @Override
    public void config(final String msg) {
        doLog(LogLevel.DEBUG, msg);
    }

    @Override
    public void fine(final String msg) {
        doLog(LogLevel.DEBUG, msg);
    }

    @Override
    public void finer(final String msg) {
        doLog(LogLevel.DEBUG, msg);
    }

    @Override
    public void finest(final String msg) {
        doLog(LogLevel.TRACE, msg);
    }

    // region Implementation

    private void doLog(final LogLevel level, final String msg) {
        if (log.isEnabled(level)) {
            log.log(level).append(msg).commit();
        }
    }

    private static LogLevel getLogLevel(final Level level) {
        final int intLevel = level.intValue();
        if (intLevel < Level.FINER.intValue()) {
            return LogLevel.TRACE;
        } else if (intLevel < Level.INFO.intValue()) {
            return LogLevel.DEBUG;
        } else if (intLevel < Level.WARNING.intValue()) {
            return LogLevel.INFO;
        } else if (intLevel < Level.SEVERE.intValue()) {
            return LogLevel.WARN;
        } else {
            return LogLevel.ERROR;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void appendMessage(final String message, final Object[] params, final LogEntry entry) {
        if (params == null || params.length == 0 || message == null) {
            entry.append(message);
        } else {
            appendMessageWithParams(message, params, entry);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void appendMessageWithParam(final String message, final Object param, final LogEntry entry) {
        int searchFrom = 0;
        int appendFrom = 0;

        while (true) {
            final int start = message.indexOf('{', searchFrom);
            if (start < 0) {
                break;
            }

            final int end = message.indexOf('}', start + 2);
            if (end < 0) {
                break;
            }

            searchFrom = end + 1;
            final int index = getIndex(message, start + 1, end);
            if (index != 0) {
                continue;
            }

            entry.append(message, appendFrom, start);
            entry.append(param);

            appendFrom = searchFrom;
        }

        entry.append(message, appendFrom, message.length());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void appendMessageWithParams(final String message, final Object[] params, final LogEntry entry) {
        int searchFrom = 0;
        int appendFrom = 0;

        while (true) {
            final int start = message.indexOf('{', searchFrom);
            if (start < 0) {
                break;
            }

            final int end = message.indexOf('}', start + 2);
            if (end < 0) {
                break;
            }

            searchFrom = end + 1;
            final int index = getIndex(message, start + 1, end);
            if (index < 0 | index >= params.length) {
                continue;
            }

            final Object param = params[index];

            entry.append(message, appendFrom, start);
            entry.append(param);

            appendFrom = searchFrom;
        }

        entry.append(message, appendFrom, message.length());
    }

    private static int getIndex(final String message, int start, int end) {
        int index = 0;

        while (start < end) {
            final char c = message.charAt(start++);
            if (c < '0' | c > '9') {
                return -1;
            }

            index = 10 * index + (c - '0');
        }

        return index;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void appendException(final Throwable exception, final LogEntry entry) {
        if (exception != null) {
            entry.append(exception);
        }
    }

    // endregion

}
