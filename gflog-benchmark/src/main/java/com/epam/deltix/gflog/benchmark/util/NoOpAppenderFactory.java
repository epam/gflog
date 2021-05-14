package com.epam.deltix.gflog.benchmark.util;


import com.epam.deltix.gflog.core.appender.AbstactAppenderFactory;
import com.epam.deltix.gflog.core.appender.Appender;

public class NoOpAppenderFactory extends AbstactAppenderFactory {

    public NoOpAppenderFactory() {
        super("noop");
    }

    @Override
    protected Appender createAppender() {
        return new NoOpAppender(name, level);
    }

}
