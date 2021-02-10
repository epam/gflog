package com.epam.deltix.gflog.core.appender;

import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.LogRecord;


public abstract class Appender implements AutoCloseable {

    protected final String name;
    protected final LogLevel level;

    public Appender(final String name, final LogLevel level) {
        this.name = name;
        this.level = level;
    }

    public final String getName() {
        return name;
    }

    public final LogLevel getLevel() {
        return level;
    }


    public abstract void open() throws Exception;

    @Override
    public abstract void close() throws Exception;


    public abstract int append(final LogRecord record) throws Exception;

    public abstract int flush() throws Exception;

}
