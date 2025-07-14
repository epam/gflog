package com.epam.deltix.gflog.core;

import com.epam.deltix.gflog.api.LogFactory;

public final class LogFactoryBinder {

    static {
        LogConfigurator.configureIfNot();
    }

    private LogFactoryBinder() {
    }

    public static LogFactory getLogFactory() {
        return LogFactoryImpl.INSTANCE;
    }
}
