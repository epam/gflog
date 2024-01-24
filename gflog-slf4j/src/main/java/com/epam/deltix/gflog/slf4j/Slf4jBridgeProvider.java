package com.epam.deltix.gflog.slf4j;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

public final class Slf4jBridgeProvider implements SLF4JServiceProvider {

    private static final String MAX_SUPPORTED_API_VERSION = "2.0.99";

    private final Slf4jBridgeFactory loggerFactory = new Slf4jBridgeFactory();
    private final BasicMarkerFactory markerFactory = new BasicMarkerFactory();
    private final BasicMDCAdapter mdcAdapter = new BasicMDCAdapter();

    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return mdcAdapter;
    }

    @Override
    public String getRequestedApiVersion() {
        return MAX_SUPPORTED_API_VERSION;
    }

    @Override
    public void initialize() {
        // lazy initialization
    }

}
