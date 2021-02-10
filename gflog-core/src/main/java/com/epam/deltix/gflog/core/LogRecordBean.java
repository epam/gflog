package com.epam.deltix.gflog.core;

import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.util.Buffer;


public final class LogRecordBean extends LogRecord {

    public void setMessage(final Buffer message) {
        this.message = message;
    }

    public void setLogName(final Buffer logName) {
        this.logName = logName;
    }

    public void setLogLevel(final LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public void setThreadName(final Buffer threadName) {
        this.threadName = threadName;
    }

    public void setAppenderMask(final long appenderMask) {
        this.appenderMask = appenderMask;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

}
