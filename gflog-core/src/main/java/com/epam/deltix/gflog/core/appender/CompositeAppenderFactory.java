package com.epam.deltix.gflog.core.appender;

import java.util.ArrayList;


public class CompositeAppenderFactory extends AbstactAppenderFactory {

    protected final ArrayList<Appender> appenders = new ArrayList<>();

    public CompositeAppenderFactory() {
        super("composite");
    }

    public void addAppender(final Appender appender) {
        appenders.add(appender);
    }

    public void removeAppender(final Appender appender) {
        appenders.remove(appender);
    }

    @Override
    protected CompositeAppender createAppender() {
        return new CompositeAppender(name, level, appenders.toArray(new Appender[0]));
    }

}
