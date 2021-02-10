package com.epam.deltix.gflog.api;

public enum LogLevel {

    TRACE, DEBUG, INFO, WARN, ERROR, FATAL;

    public boolean isLoggable(final LogLevel level) {
        return ordinal() <= level.ordinal();
    }

    public static LogLevel valueOf(final int ordinal) {
        switch (ordinal) {
            case 0:
                return LogLevel.TRACE;
            case 1:
                return LogLevel.DEBUG;
            case 2:
                return LogLevel.INFO;
            case 3:
                return LogLevel.WARN;
            case 4:
                return LogLevel.ERROR;
            case 5:
                return LogLevel.FATAL;
            default:
                return null;
        }
    }

}
