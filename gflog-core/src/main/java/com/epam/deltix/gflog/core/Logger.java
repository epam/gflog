package com.epam.deltix.gflog.core;

import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.appender.Appender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;


public final class Logger {

    private String name;
    private LogLevel level;

    private final List<Appender> appenders = new ArrayList<>(4);

    public Logger(final LogLevel level, final Appender... appenders) {
        this("", level, appenders);
    }

    public Logger(final String name, final LogLevel level, final Appender... appenders) {
        this.name = requireNonNull(name);
        this.level = requireNonNull(level);

        Collections.addAll(this.appenders, appenders);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = requireNonNull(name);
    }

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(final LogLevel level) {
        this.level = requireNonNull(level);
    }

    public void addAppender(final Appender appender) {
        appenders.add(requireNonNull(appender));
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

    public List<Appender> getAppenders() {
        return appenders;
    }

    @Override
    public String toString() {
        return "Logger{" +
                "name='" + name + '\'' +
                ", level=" + level +
                ", appenders=" + appenders +
                '}';
    }
}
