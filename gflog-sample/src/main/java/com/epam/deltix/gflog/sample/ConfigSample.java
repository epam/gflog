package com.epam.deltix.gflog.sample;

import com.epam.deltix.gflog.api.Log;
import com.epam.deltix.gflog.api.LogFactory;
import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.LogConfig;
import com.epam.deltix.gflog.core.LogConfigurator;
import com.epam.deltix.gflog.core.Logger;
import com.epam.deltix.gflog.core.appender.Appender;
import com.epam.deltix.gflog.core.appender.ConsoleAppenderFactory;


public class ConfigSample {

    public static void main(final String[] args) {
        configFromSystemProperties();
        message();

        configProgrammable();
        message();
    }

    protected static void configFromSystemProperties() {
        // same as -Dgflog.config=classpath:gflog-sample.xml jvm option
        System.setProperty("gflog.config", "classpath:gflog-sample.xml");
        // same as -Dgflog.sync=true jvm option
        // good for tests if you aggregate output per test
        System.setProperty("gflog.sync", "true");
    }

    protected static void configProgrammable() {
        final ConsoleAppenderFactory appenderFactory = new ConsoleAppenderFactory();
        appenderFactory.setStderr(true);

        final Appender appender = appenderFactory.create();
        final Logger logger = new Logger(LogLevel.DEBUG, appender);

        final LogConfig config = new LogConfig();

        config.addAppender(appender);
        config.addLogger(logger);

        LogConfigurator.configureWithShutdown(config);
    }

    private static void message() {
        final Log log = LogFactory.getLog(ConfigSample.class);
        log.info("Hello there!");
    }

}
