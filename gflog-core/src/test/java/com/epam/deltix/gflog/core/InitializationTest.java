package com.epam.deltix.gflog.core;

import com.epam.deltix.gflog.api.Log;
import com.epam.deltix.gflog.api.LogFactory;
import org.junit.Test;

public class InitializationTest {

    @Test
    public void testInitializationShouldNotBeDeadLocked() throws Exception {
        final Thread blocker = new Thread(() -> {
            final Log log = LogFactory.getLog("test-logger");
            log.info("Initialization should not be dead locked");
        });
        blocker.start();

        LogConfigurator.configureIfNot();
        blocker.join(5000);
    }
}
