package com.epam.deltix.gflog.core;

import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.util.Buffer;


public abstract class LogRecord {

    protected Buffer logName;
    protected Buffer threadName;
    protected Buffer message;

    protected LogLevel logLevel;
    protected long appenderMask;
    protected long timestamp;

    public final Buffer getLogName() {
        return logName;
    }

    public final Buffer getThreadName() {
        return threadName;
    }

    public final Buffer getMessage() {
        return message;
    }

    public final LogLevel getLogLevel() {
        return logLevel;
    }

    public final long getAppenderMask() {
        return appenderMask;
    }

    public final long getTimestamp() {
        return timestamp;
    }

}
