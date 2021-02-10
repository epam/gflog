package com.epam.deltix.gflog.api;


public interface LogEntryTemplate {

    /**
     * Inserts the character.
     */
    LogEntryTemplate with(final char value);

    /**
     * Inserts the char sequence.
     */
    LogEntryTemplate with(final CharSequence value);

    /**
     * Inserts the char sequence from the start position to the end position.
     */
    LogEntryTemplate with(final CharSequence value, final int start, final int end);

    /**
     * Inserts the string.
     */
    LogEntryTemplate with(final String value);

    /**
     * Inserts the string from the start position to the end position.
     */
    LogEntryTemplate with(final String value, final int start, final int end);

    /**
     * Inserts the boolean.
     */
    LogEntryTemplate with(final boolean value);

    /**
     * Inserts the integer.
     */
    LogEntryTemplate with(final int value);

    /**
     * Inserts the long.
     */
    LogEntryTemplate with(final long value);

    /**
     * Inserts the double with the default precision.
     */
    LogEntryTemplate with(final double value);

    /**
     * Inserts the double with the specified precision.
     */
    LogEntryTemplate with(final double value, final int precision);

    /**
     * Inserts the object that implements Loggable interface.
     */
    LogEntryTemplate with(final Loggable object);

    /**
     * Inserts the object using toString() method.
     */
    LogEntryTemplate with(final Object object);

    /**
     * Inserts the exception.
     */
    LogEntryTemplate with(final Throwable exception);

    /**
     * Inserts the decimal-64 floating point value (IEEE 754-2008 Decimal Floating-Point Arithmetic specification).
     */
    LogEntryTemplate withDecimal64(final long decimal);

    /**
     * Inserts the timestamp in milliseconds since Epoch in "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" format.
     */
    LogEntryTemplate withTimestamp(final long timestamp);

    /**
     * Inserts the date part of the timestamp in milliseconds since Epoch in "yyyy-MM-dd" format.
     */
    LogEntryTemplate withDate(final long timestamp);

    /**
     * Inserts the time part of the timestamp in milliseconds since Epoch in "HH:mm:ss.SSS" format.
     */
    LogEntryTemplate withTime(final long timestamp);

    /**
     * Inserts the alphanumeric value.
     */
    LogEntryTemplate withAlphanumeric(final long alphanumeric);

}
