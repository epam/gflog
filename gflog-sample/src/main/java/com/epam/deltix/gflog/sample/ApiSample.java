package com.epam.deltix.gflog.sample;

import com.epam.deltix.gflog.api.Log;
import com.epam.deltix.gflog.api.LogFactory;
import com.epam.deltix.gflog.api.LogLevel;


public class ApiSample {

    private static final Log LOG = LogFactory.getLog(Appendable.class);

    public static void main(final String[] args) {
        stringBuilderStyle();
        stringFormatStyle();
    }

    private static void stringBuilderStyle() {
        LOG.info()
                .append("Hello world! ")
                .append("This is a ")
                .append(LogLevel.INFO)
                .append(" message for you!")
                .commit();
    }

    private static void stringFormatStyle() {
        LOG.info("Hello %s! This is a %s message for %s!")
                .with("world")
                .with(LogLevel.INFO)
                .with("you");
    }

}
