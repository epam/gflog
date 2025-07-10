package com.epam.deltix.gflog.core;

import com.epam.deltix.gflog.api.Log;
import com.epam.deltix.gflog.api.LogFactory;
import com.epam.deltix.gflog.core.service.LogService;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


final class LogFactoryImpl extends LogFactory {

    static final LogFactoryImpl INSTANCE = new LogFactoryImpl();

    private final ConcurrentMap<String, LogView> loggerMap = new ConcurrentHashMap<>();
    private final Collection<Log> loggerSet = Collections.unmodifiableCollection(loggerMap.values());

    private LogService service;
    private int index;

    private LogFactoryImpl() {
    }

    @Override
    protected Log getLogger(final String name) {
        LogView logger = loggerMap.get(name);

        if (logger == null) {
            synchronized (this) {
                logger = loggerMap.get(name);

                if (logger == null) {
                    logger = new LogView(name, index++);
                    final LogService service = this.service;

                    if (service != null) {
                        logger.initialize(service);
                    }

                    loggerMap.put(name, logger);
                }
            }
        }

        return logger;
    }

    @Override
    protected Collection<Log> getLoggers() {
        return loggerSet;
    }

    void initialize(final LogService service) {
        service.open();

        synchronized (this) {
            this.service = service;

            for (final LogView logger : loggerMap.values()) {
                logger.initialize(service);
            }
        }
    }

    void invalidate() {
        final LogService service;

        synchronized (this) {
            service = this.service;
            this.service = null;

            for (final LogView logger : loggerMap.values()) {
                logger.invalidate();
            }
        }

        service.close();
    }

}
