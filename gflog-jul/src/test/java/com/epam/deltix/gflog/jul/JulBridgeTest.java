package com.epam.deltix.gflog.jul;

import org.junit.Assert;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class JulBridgeTest {

    public static void main(final String[] args) {
        JulBridge.install();
        Assert.assertTrue(JulBridge.isInstalled());

        final Logger logger = LogManager.getLogManager().getLogger("");
        logger.info("Info");
        logger.log(Level.WARNING, "Warning: {0}", "dead socket");
        logger.log(Level.SEVERE, "Error: {0} {1}, {0} socket", new Object[]{"dead", "process"});
        logger.log(Level.SEVERE, "Exception", new Throwable());

        Assert.assertFalse(logger.isLoggable(Level.CONFIG));
        Assert.assertFalse(logger.isLoggable(Level.FINE));
        Assert.assertFalse(logger.isLoggable(Level.FINEST));
        Assert.assertFalse(logger.isLoggable(Level.ALL));
    }

}
