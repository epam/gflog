package com.epam.deltix.gflog.slf4j;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Slf4jBridgeTest {

    @Test
    public void test() {
        final Logger log = LoggerFactory.getLogger("test");

        log.info("1");
        log.info("2", new IllegalArgumentException());

        log.info("{}", "3");
        log.info("--{}--", "4");

        log.info("--{}--", "5", "6");   // lgtm
        log.info("--{}--", "6", new IllegalArgumentException());
        log.info("--{}", "7", new IllegalArgumentException());

        log.info("-{}--", new Object[]{"8", new IllegalArgumentException()});

        log.info("Create user {} ({})", "john", "#1234");
    }


}
