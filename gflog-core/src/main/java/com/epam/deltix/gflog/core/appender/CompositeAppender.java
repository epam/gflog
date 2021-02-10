package com.epam.deltix.gflog.core.appender;

import com.epam.deltix.gflog.api.LogDebug;
import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.LogRecord;


public class CompositeAppender extends Appender {

    protected final Appender[] appenders;

    protected CompositeAppender(final String name, final LogLevel level, final Appender[] appenders) {
        super(name, level);
        this.appenders = appenders;
    }

    @Override
    public void open() throws Exception {
        // skip
    }

    @Override
    public void close() throws Exception {
        // skip
    }

    @Override
    public int flush() throws Exception {
        // skip
        return 0;
    }

    @Override
    public int append(final LogRecord record) {
        int workDone = 0;

        for (final Appender appender : appenders) {
            try {
                workDone += appender.append(record);
            } catch (final Throwable e) {
                LogDebug.warn("appender: " + appender.getName() + " threw exception during processing record", e);
            }
        }

        return workDone;
    }

}
