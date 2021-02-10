package com.epam.deltix.gflog.core.appender;

import com.epam.deltix.gflog.core.LogRecord;


public interface AppenderListener {

    int onLogRecord(LogRecord record);

    int onFlush();

}
