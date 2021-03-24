package com.epam.deltix.gflog.core.service;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;


@RunWith(Parameterized.class)
public class SyncLogServiceTest extends LogServiceTest {

    @Parameterized.Parameters(name = "producers={0}, encoding={1}")
    public static Collection<?> parameters() {
        final int[] producers = {1, 2, 3, 4, 5};
        final String[] encodings = {"ASCII", "UTF-8"};

        final ArrayList<Object[]> parameters = new ArrayList<>();

        for (final int producer : producers) {
            for (final String encoding : encodings) {
                final Object[] oneCase = {producer, encoding};
                parameters.add(oneCase);
            }
        }

        return parameters;
    }

    public SyncLogServiceTest(final int producers, final String encoding) {
        super(producers, encoding, new SyncLogServiceFactory());
    }

}
