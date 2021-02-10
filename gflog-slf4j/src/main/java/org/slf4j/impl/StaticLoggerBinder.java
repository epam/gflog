package org.slf4j.impl;

import com.epam.deltix.gflog.slf4j.Slf4jBridgeFactory;
import org.slf4j.ILoggerFactory;


public final class StaticLoggerBinder {

    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    private final ILoggerFactory loggerFactory = new Slf4jBridgeFactory();

    private StaticLoggerBinder() {
    }

    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    public String getLoggerFactoryClassStr() {
        return Slf4jBridgeFactory.class.getName();
    }

}
