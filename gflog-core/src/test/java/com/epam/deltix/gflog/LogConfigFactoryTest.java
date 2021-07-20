package com.epam.deltix.gflog;

import com.epam.deltix.gflog.core.LogConfig;
import com.epam.deltix.gflog.core.LogConfigFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LogConfigFactoryTest {
    LogConfig logConfig;

    @Before
    public void initilize() {
        logConfig = LogConfigFactory.loadDefault();
    }

    @Test
    public void checkInclude() {
        Assert.assertEquals("[Appender{name='file', level=TRACE}, Appender{name='file_included', level=INFO}]",
                logConfig.getAppenders().toString());

        Assert.assertEquals("[Logger{name='', level=INFO, appenders=[Appender{name='file', level=TRACE}]}, " +
                        "Logger{name='logger_included', level=INFO, appenders=[Appender{name='file_included', level=INFO}]}]",
                logConfig.getLoggers().toString());
    }
}