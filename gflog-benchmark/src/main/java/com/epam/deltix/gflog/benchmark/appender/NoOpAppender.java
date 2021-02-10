package com.epam.deltix.gflog.benchmark.appender;

import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.LogRecord;
import com.epam.deltix.gflog.core.appender.Appender;

public class NoOpAppender extends Appender {

    public NoOpAppender(final String name, final LogLevel level) {
        super(name, level);
    }

    @Override
    public void open() {
    }

    @Override
    public void close() {
    }

    @Override
    public int append(final LogRecord record) {
        return 0;
    }

    @Override
    public int flush() {
        return 0;
    }

}
