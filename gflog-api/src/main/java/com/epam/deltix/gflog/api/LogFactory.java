package com.epam.deltix.gflog.api;

import java.lang.reflect.Method;
import java.util.Collection;


public abstract class LogFactory {

    public static final String BINDER_CLASS = "com.epam.deltix.gflog.core.LogFactoryBinder";
    public static final String BINDER_METHOD = "getLogFactory";

    /**
     * Looks up the log instance for the class.
     *
     * @param clazz to look up for.
     * @return the log instance for the specified clazz.
     */
    public static Log getLog(final Class<?> clazz) {
        return getLog(clazz.getCanonicalName());
    }

    /**
     * Looks up the log instance for the name.
     *
     * @param name to look up for.
     * @return the log instance for the specified name.
     */
    public static Log getLog(final String name) {
        return Implementation.INSTANCE.getLogger(name);
    }

    /**
     * Returns all available logs. The collection instance might be immutable.
     *
     * @return all logs.
     */
    public static Collection<Log> getLogs() {
        return Implementation.INSTANCE.getLoggers();
    }

    protected abstract Log getLogger(final String name);

    protected abstract Collection<Log> getLoggers();


    private static class Implementation {

        private static final LogFactory INSTANCE;

        static {
            LogFactory instance = NoOpLogFactory.INSTANCE;

            try {
                final Class<?> clazz = Class.forName(BINDER_CLASS);
                final Method method = clazz.getMethod(BINDER_METHOD);
                instance = (LogFactory) method.invoke(null);
            } catch (final Exception e) {
                LogDebug.warn("can't bind Log Factory due to: " + e.getMessage() + ". Will use No Op Log Factory.");
            }

            INSTANCE = instance;
        }

    }

}
