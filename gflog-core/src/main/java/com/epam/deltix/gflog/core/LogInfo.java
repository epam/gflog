package com.epam.deltix.gflog.core;


import com.epam.deltix.gflog.api.LogLevel;

public final class LogInfo {

    private final String name;
    private final LogLevel level;
    private final long[] appenderMask;

    public LogInfo(final String name, final LogLevel level, final long[] appenderMask) {
        this.name = name;
        this.level = level;
        this.appenderMask = appenderMask;
    }

    public String getName() {
        return name;
    }

    public LogLevel getLevel() {
        return level;
    }

    public long[] getAppenderMask() {
        return appenderMask;
    }

}
