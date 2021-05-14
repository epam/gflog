package com.epam.deltix.gflog.slf4j;

import com.epam.deltix.gflog.api.Log;
import com.epam.deltix.gflog.api.LogEntry;
import org.slf4j.helpers.MarkerIgnoringBase;


public final class Slf4jBridge extends MarkerIgnoringBase {

    private static final String PLACEHOLDER = "{}";

    private final Log log;

    public Slf4jBridge(final String name, final Log log) {
        this.name = name;
        this.log = log;
    }

    @Override
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    @Override
    public void trace(final String message) {
        if (isTraceEnabled()) {
            log(message, log.trace());
        }
    }

    @Override
    public void trace(final String message, final Object object) {
        if (isTraceEnabled()) {
            log(message, object, log.trace());
        }
    }

    @Override
    public void trace(final String message, final Object object1, final Object object2) {
        if (isTraceEnabled()) {
            log(message, object1, object2, log.trace());
        }
    }

    @Override
    public void trace(final String message, final Object[] objects) {
        if (isTraceEnabled()) {
            log(message, objects, log.trace());
        }
    }

    @Override
    public void trace(final String message, final Throwable exception) {
        if (isTraceEnabled()) {
            log(message, exception, log.trace());
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public void debug(final String message) {
        if (isDebugEnabled()) {
            log(message, log.debug());
        }
    }

    @Override
    public void debug(final String message, final Object object) {
        if (isDebugEnabled()) {
            log(message, object, log.debug());
        }
    }

    @Override
    public void debug(final String message, final Object object1, final Object object2) {
        if (isDebugEnabled()) {
            log(message, object1, object2, log.debug());
        }
    }

    @Override
    public void debug(final String message, final Object[] objects) {
        if (isDebugEnabled()) {
            log(message, objects, log.debug());
        }
    }

    @Override
    public void debug(final String message, final Throwable exception) {
        if (isDebugEnabled()) {
            log(message, exception, log.debug());
        }
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public void info(final String message) {
        if (isInfoEnabled()) {
            log(message, log.info());
        }
    }

    @Override
    public void info(final String message, final Object object) {
        if (isInfoEnabled()) {
            log(message, object, log.info());
        }
    }

    @Override
    public void info(final String message, final Object object1, final Object object2) {
        if (isInfoEnabled()) {
            log(message, object1, object2, log.info());
        }
    }

    @Override
    public void info(final String message, final Object[] objects) {
        if (isInfoEnabled()) {
            log(message, objects, log.info());
        }
    }

    @Override
    public void info(final String message, final Throwable exception) {
        if (isInfoEnabled()) {
            log(message, exception, log.info());
        }
    }

    @Override
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    @Override
    public void warn(final String message) {
        if (isWarnEnabled()) {
            log(message, log.warn());
        }
    }

    @Override
    public void warn(final String message, final Object object) {
        if (isWarnEnabled()) {
            log(message, object, log.warn());
        }
    }

    @Override
    public void warn(final String message, final Object object1, final Object object2) {
        if (isWarnEnabled()) {
            log(message, object1, object2, log.warn());
        }
    }

    @Override
    public void warn(final String message, final Object[] objects) {
        if (isWarnEnabled()) {
            log(message, objects, log.warn());
        }
    }

    @Override
    public void warn(final String message, final Throwable exception) {
        if (isWarnEnabled()) {
            log(message, exception, log.warn());
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    @Override
    public void error(final String message) {
        if (isErrorEnabled()) {
            log(message, log.error());
        }
    }

    @Override
    public void error(final String message, final Object object) {
        if (isErrorEnabled()) {
            log(message, object, log.error());
        }
    }

    @Override
    public void error(final String message, final Object object1, final Object object2) {
        if (isErrorEnabled()) {
            log(message, object1, object2, log.error());
        }
    }

    @Override
    public void error(final String message, final Object[] objects) {
        if (isErrorEnabled()) {
            log(message, objects, log.error());
        }
    }

    @Override
    public void error(final String message, final Throwable exception) {
        if (isErrorEnabled()) {
            log(message, exception, log.error());
        }
    }

    private static void log(final String message, final Throwable exception, final LogEntry entry) {
        entry.append(message).appendLast(exception);
    }

    private static void log(final String message, final LogEntry entry) {
        entry.appendLast(message);
    }

    private static void log(final String message, final Object object, final LogEntry entry) {
        if (message == null) {
            entry.appendLast((String) null);
            return;
        }

        final int offset = appendChunk(message, 0, object, entry);
        appendTail(message, offset, entry);

        entry.commit();
    }

    private static void log(final String message, final Object object1, final Object object2, final LogEntry entry) {
        if (message == null) {
            entry.appendLast((String) null);
            return;
        }

        int offset = appendChunk(message, 0, object1, entry);
        boolean last = false;

        if (offset > 0) {
            final int i = appendChunk(message, offset, object2, entry);
            last = (i == offset);
            offset = i;
        }

        appendTail(message, offset, entry);

        // https://www.slf4j.org/faq.html#paramException
        if (last) {
            appendLast(object2, entry);
        }

        entry.commit();
    }

    private static void log(final String message, final Object[] objects, final LogEntry entry) {
        if (message == null || objects == null) {
            log(message, entry);
            return;
        }

        int index = 0;
        int offset = 0;

        while (index < objects.length) {
            final Object object = objects[index];
            final int i = appendChunk(message, offset, object, entry);

            if (i == offset) {
                break;
            }

            offset = i;
            index++;
        }

        appendTail(message, offset, entry);

        // https://www.slf4j.org/faq.html#paramException
        if (index == objects.length - 1) {
            appendLast(objects[index], entry);
        }

        entry.commit();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void appendLast(final Object object, final LogEntry entry) {
        if (object instanceof Throwable) {
            entry.append(((Throwable) object));
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static int appendChunk(final String message, final int offset, final Object object, final LogEntry entry) {
        final int index = message.indexOf(PLACEHOLDER, offset);
        if (index < 0) {
            return offset;
        }

        entry.append(message, offset, index);
        entry.append(object);

        return index + PLACEHOLDER.length();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void appendTail(final String message, final int offset, final LogEntry entry) {
        if (offset < message.length()) {
            entry.append(message, offset, message.length());
        }
    }

}
