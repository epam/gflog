package com.epam.deltix.gflog.core;

import com.epam.deltix.gflog.core.appender.Appender;
import com.epam.deltix.gflog.core.service.AsyncLogServiceFactory;
import com.epam.deltix.gflog.core.service.LogServiceFactory;
import com.epam.deltix.gflog.core.service.SyncLogServiceFactory;
import com.epam.deltix.gflog.core.util.PropertyUtil;

import java.util.ArrayList;
import java.util.Collection;


public final class LogConfig {

    public static final boolean SYNC = PropertyUtil.getBoolean("gflog.sync", false);

    private final ArrayList<Appender> appenders = new ArrayList<>();
    private final ArrayList<Logger> loggers = new ArrayList<>();

    private LogServiceFactory service;

    public void setService(final LogServiceFactory service) {
        this.service = service;
    }

    public LogServiceFactory getService() {
        return service;
    }

    public void addAppender(final Appender appender) {
        appenders.add(appender);
    }

    public void removeAppender(final Appender appender) {
        appenders.remove(appender);
    }

    public Appender getAppender(final String name) {
        for (final Appender appender : appenders) {
            if (appender.getName().equals(name)) {
                return appender;
            }
        }

        return null;
    }

    public Collection<Appender> getAppenders() {
        return appenders;
    }

    public void addLogger(final Logger logger) {
        loggers.add(logger);
    }

    public void removeLogger(final Logger logger) {
        loggers.add(logger);
    }

    public Logger getLogger(final String name) {
        for (final Logger logger : loggers) {
            if (logger.getName().equals(name)) {
                return logger;
            }
        }

        return null;
    }

    public Collection<Logger> getLoggers() {
        return loggers;
    }

    public void conclude() {
        if (service == null) {
            service = SYNC ? new SyncLogServiceFactory() : new AsyncLogServiceFactory();
        }
    }

}
