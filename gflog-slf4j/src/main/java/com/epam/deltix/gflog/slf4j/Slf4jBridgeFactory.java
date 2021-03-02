package com.epam.deltix.gflog.slf4j;

import com.epam.deltix.gflog.api.Log;
import com.epam.deltix.gflog.api.LogFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;


public final class Slf4jBridgeFactory implements ILoggerFactory {

    private final ConcurrentHashMap<String, Slf4jBridge> loggers = new ConcurrentHashMap<>();

    @Override
    public Logger getLogger(final String name) {
        Slf4jBridge logger = loggers.get(name);

        if (logger == null) {
            final Log log = LogFactory.getLog(toLogName(name));

            logger = new Slf4jBridge(name, log);
            final Slf4jBridge existed = loggers.putIfAbsent(name, logger);

            if (existed != null) {
                logger = existed;
            }
        }

        return logger;
    }

    private static String toLogName(final String loggerName) {
        return Logger.ROOT_LOGGER_NAME.equals(loggerName) ? "" : loggerName;
    }

}
