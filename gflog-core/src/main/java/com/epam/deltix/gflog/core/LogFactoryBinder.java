package com.epam.deltix.gflog.core;

import com.epam.deltix.gflog.api.LogFactory;

public final class LogFactoryBinder {

    private LogFactoryBinder() {
    }

    public static LogFactory getLogFactory() {
        return LogFactoryImpl.INSTANCE;
    }
}
