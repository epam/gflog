package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.core.Logger;
import com.epam.deltix.gflog.core.appender.Appender;
import com.epam.deltix.gflog.core.clock.Clock;


public final class SyncLogServiceFactory extends LogServiceFactory {

    @Override
    protected LogService createService(final Logger[] loggers,
                                       final Appender[] appenders,
                                       final Clock clock,
                                       final String entryTruncationSuffix,
                                       final int entryInitialCapacity,
                                       final int entryMaxCapacity,
                                       final boolean entryUtf8) {

        return new SyncLogService(
                loggers,
                appenders,
                clock,
                entryTruncationSuffix,
                entryInitialCapacity,
                entryMaxCapacity,
                entryUtf8
        );
    }

}
