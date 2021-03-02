package com.epam.deltix.gflog.jcl;

import com.epam.deltix.gflog.api.LogEntry;
import com.epam.deltix.gflog.api.LogFactory;
import org.apache.commons.logging.Log;


public final class JclBridge implements Log {

    private final com.epam.deltix.gflog.api.Log log;

    public JclBridge(final String name) {
        this.log = LogFactory.getLog(name);
    }

    @Override
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    @Override
    public void trace(final Object object) {
        if (isTraceEnabled()) {
            log(object, log.trace());
        }
    }

    @Override
    public void trace(final Object object, final Throwable exception) {
        if (isTraceEnabled()) {
            log(object, exception, log.trace());
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public void debug(final Object object) {
        if (isDebugEnabled()) {
            log(object, log.debug());
        }
    }

    @Override
    public void debug(final Object object, final Throwable exception) {
        if (isDebugEnabled()) {
            log(object, exception, log.debug());
        }
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public void info(final Object object) {
        if (isInfoEnabled()) {
            log(object, log.info());
        }
    }

    @Override
    public void info(final Object object, final Throwable exception) {
        if (isInfoEnabled()) {
            log(object, exception, log.info());
        }
    }

    @Override
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    @Override
    public void warn(final Object object) {
        if (isWarnEnabled()) {
            log(object, log.warn());
        }
    }

    @Override
    public void warn(final Object object, final Throwable exception) {
        if (isWarnEnabled()) {
            log(object, exception, log.warn());
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    @Override
    public void error(final Object object) {
        if (isErrorEnabled()) {
            log(object, log.error());
        }
    }

    @Override
    public void error(final Object object, final Throwable exception) {
        if (isErrorEnabled()) {
            log(object, exception, log.error());
        }
    }

    @Override
    public boolean isFatalEnabled() {
        return log.isFatalEnabled();
    }

    @Override
    public void fatal(final Object object) {
        if (isFatalEnabled()) {
            log(object, log.fatal());
        }
    }

    @Override
    public void fatal(final Object object, final Throwable exception) {
        if (isFatalEnabled()) {
            log(object, exception, log.fatal());
        }
    }

    private static void log(final Object object, final LogEntry entry) {
        entry.appendLast(object);
    }

    private static void log(final Object object, final Throwable exception, final LogEntry entry) {
        entry.append(object).appendLast(exception);
    }

}
