package com.epam.deltix.gflog.core.appender;

import com.epam.deltix.gflog.core.util.PropertyUtil;


public class SafeAppenderFactory extends CompositeAppenderFactory {

    protected static final int MAX_ENTRIES_PER_SECOND = PropertyUtil.getInteger("gflogger.safe.appender.max.entries.per.second", 500);

    protected int maxEntriesPerSecond;

    public SafeAppenderFactory() {
        name = "safe composite";
    }

    public int getMaxEntriesPerSecond() {
        return maxEntriesPerSecond;
    }

    public void setMaxEntriesPerSecond(final int maxEntriesPerSecond) {
        this.maxEntriesPerSecond = maxEntriesPerSecond;
    }

    @Override
    protected void conclude() {
        super.conclude();

        if (maxEntriesPerSecond == 0) {
            maxEntriesPerSecond = MAX_ENTRIES_PER_SECOND;
        }
    }

    @Override
    protected SafeAppender createAppender() {
        return new SafeAppender(name, level, maxEntriesPerSecond, appenders.toArray(new Appender[0]));
    }

}
