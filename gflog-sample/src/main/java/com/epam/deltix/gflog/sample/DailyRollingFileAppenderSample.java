package com.epam.deltix.gflog.sample;

import com.epam.deltix.gflog.api.Log;
import com.epam.deltix.gflog.api.LogFactory;
import com.epam.deltix.gflog.core.LogConfigurator;


public class DailyRollingFileAppenderSample {

    public static void main(final String[] args) throws Exception {
        LogConfigurator.configureWithShutdown("classpath:daily-rolling-file-appender.xml");
        Log log = LogFactory.getLog(ConfigSample.class);

        for (int i = 0; i < 1000; i++) {
            log.info("Test #%s").with(i);
        }
    }

}
