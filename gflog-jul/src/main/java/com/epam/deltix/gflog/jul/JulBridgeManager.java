package com.epam.deltix.gflog.jul;

import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implemented in the same way as log4j-jul bridge.
 */
public final class JulBridgeManager extends java.util.logging.LogManager {

    private final ConcurrentHashMap<String, JulBridgeLogger> loggers = new ConcurrentHashMap<>();

    @Override
    public boolean addLogger(final Logger logger) {
        // in order to prevent non-bridged loggers from being registered, we always return false to indicate that
        // the named logger should be obtained through getLogger(name)
        return false;
    }

    @Override
    public Logger getLogger(String name) {
        name = (name == null) ? "" : name;
        JulBridgeLogger bridge = loggers.get(name);

        if (bridge == null) {
            bridge = new JulBridgeLogger(name);
            final JulBridgeLogger existed = loggers.putIfAbsent(name, bridge);

            if (existed != null) {
                bridge = existed;
            }
        }

        return bridge;
    }

    @Override
    public Enumeration<String> getLoggerNames() {
        return Collections.enumeration(
                loggers.values()
                        .stream()
                        .map(Logger::getName)
                        .collect(Collectors.toList())
        );
    }

}
