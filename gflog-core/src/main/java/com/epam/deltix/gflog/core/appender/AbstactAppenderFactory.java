package com.epam.deltix.gflog.core.appender;

import com.epam.deltix.gflog.api.LogLevel;

import static java.util.Objects.requireNonNull;


public abstract class AbstactAppenderFactory implements AppenderFactory {

    protected String name;
    protected LogLevel level = LogLevel.TRACE;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(final LogLevel level) {
        this.level = level;
    }

    public AbstactAppenderFactory(final String name) {
        this.name = name;
    }

    @Override
    public final Appender create() {
        conclude();
        return createAppender();
    }

    protected void conclude() {
        requireNonNull(name, "name is null");
        requireNonNull(level, "level is null");
    }

    protected abstract Appender createAppender();

}
