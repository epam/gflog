package com.epam.deltix.gflog.core;

import com.epam.deltix.gflog.core.appender.Appender;
import com.epam.deltix.gflog.core.service.LogService;
import com.epam.deltix.gflog.core.service.LogServiceFactory;

import java.util.Collection;
import java.util.Properties;


public final class LogConfigurator {

    private static boolean configured = false;

    private LogConfigurator() {
    }

    static synchronized void configureIfNot() {
        if (!configured) {
            final LogConfig config = LogConfigFactory.loadDefault();
            configureWithShutdown(config);
        }
    }

    public static void configureWithShutdown(final String url) throws Exception {
        configureWithShutdown(url, System.getProperties());
    }

    public static void configureWithShutdown(final String url, final Properties properties) throws Exception {
        configure(url, properties);
        activateShutdownHook();

    }

    public static synchronized void configureWithShutdown(final LogConfig configuration) {
        configure(configuration);
        activateShutdownHook();
    }

    public static void configure(final String url) throws Exception {
        configure(url, System.getProperties());
    }

    public static void configure(final String url, final Properties properties) throws Exception {
        unconfigure();
        final LogConfig config = LogConfigFactory.load(url, properties);
        configure(config);
    }

    public static synchronized void configure(final LogConfig config) {
        config.conclude();

        unconfigure();
        configured = true;

        try {
            final LogServiceFactory logServiceFactory = config.getService();
            final Collection<Logger> loggers = config.getLoggers();
            final Collection<Appender> appenders = config.getAppenders();

            final LogService service = logServiceFactory.create(loggers, appenders);
            LogFactoryImpl.INSTANCE.initialize(service);
        } catch (final Throwable e) {
            configured = false;
            throw e;
        }
    }

    public static synchronized void unconfigure() {
        if (configured) {
            configured = false;
            deactivateShutdownHook();
            LogFactoryImpl.INSTANCE.invalidate();
        }
    }

    public static synchronized boolean isConfigured() {
        return configured;
    }

    private static void activateShutdownHook() {
        LogShutdownHook.INSTANCE.activate();
    }

    private static void deactivateShutdownHook() {
        LogShutdownHook.INSTANCE.deactivate();
    }

    private static final class LogShutdownHook extends Thread {

        private static final LogShutdownHook INSTANCE = new LogShutdownHook();

        private boolean registered = false;

        private volatile boolean active = true;

        private LogShutdownHook() {
            super("gflog-shutdown-hook");
        }

        @Override
        public void run() {
            if (active) {
                LogConfigurator.unconfigure();
            }
        }

        public void activate() {
            if (!registered) {
                Runtime.getRuntime().addShutdownHook(this);
                registered = true;
            }

            active = true;
        }

        public void deactivate() {
            active = false;
        }

    }

}