package com.epam.deltix.gflog.api;

import java.util.Collection;
import java.util.Collections;

public final class NoOpLogFactory extends LogFactory {

    public static final NoOpLogFactory INSTANCE = new NoOpLogFactory();

    private NoOpLogFactory() {
    }

    @Override
    protected Log getLogger(final String name) {
        return NoOpLog.INSTANCE;
    }

    @Override
    protected Collection<Log> getLoggers() {
        return Collections.emptyList();
    }

}
