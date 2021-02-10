package com.epam.deltix.gflog.core.service;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;


@RunWith(Parameterized.class)
public class SyncLogServiceTest extends LogServiceTest {

    @Parameterized.Parameters(name = "producers={0}")
    public static Collection<?> parameters() {
        return Arrays.asList(1, 2, 3);
    }

    public SyncLogServiceTest(final int producerCount) {
        super(producerCount, new SyncLogServiceFactory());
    }

}
