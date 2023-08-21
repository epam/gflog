package com.epam.deltix.gflog.api;


public interface LogEntryTemplate {

    /**
     * Inserts the character to this template.
     *
     * @param value to insert.
     * @return a reference to this object.
     */
    LogEntryTemplate with(final char value);

    /**
     * Inserts the sequence to this template.
     *
     * @param value to insert.
     * @return a reference to this object.
     */
    LogEntryTemplate with(final CharSequence value);

    /**
     * Inserts the sequence from the start position (inclusively) to the end position (exclusively) to this template.
     *
     * @param value to insert.
     * @param start which position (inclusively) to start from.
     * @param end   which position (exclusively) to end to.
     * @return a reference to this object.
     */
    LogEntryTemplate with(final CharSequence value, final int start, final int end);

    /**
     * Appends the string to this template.
     *
     * @param value to insert.
     * @return a reference to this object.
     */
    LogEntryTemplate with(final String value);

    /**
     * Inserts the string from the start position (inclusively) to the end position (exclusively) to this template.
     *
     * @param value to insert.
     * @param start which position (inclusively) to start from.
     * @param end   which position (exclusively) to end to.
     * @return a reference to this object.
     */
    LogEntryTemplate with(final String value, final int start, final int end);

    /**
     * Inserts the boolean to this template.
     *
     * @param value to insert.
     * @return a reference to this object.
     */
    LogEntryTemplate with(final boolean value);

    /**
     * Inserts the integer to this template.
     *
     * @param value to insert.
     * @return a reference to this object.
     */
    LogEntryTemplate with(final int value);

    /**
     * Inserts the long to this template.
     *
     * @param value to insert.
     * @return a reference to this object.
     */
    LogEntryTemplate with(final long value);

    /**
     * Inserts the double to this template.
     *
     * @param value to insert.
     * @return a reference to this object.
     */
    LogEntryTemplate with(final double value);

    /**
     * Inserts the double with the precision to this template.
     *
     * @param value     to insert.
     * @param precision after the point.
     * @return a reference to this object.
     */
    LogEntryTemplate with(final double value, final int precision);

    /**
     * Inserts the object which implements com.epam.deltix.gflog.api.Loggable interface to this template.
     *
     * @param object to insert.
     * @return a reference to this object.
     */
    LogEntryTemplate with(final Loggable object);

    /**
     * Inserts the object to this template.
     *
     * @param object to insert.
     * @return a reference to this object.
     */
    LogEntryTemplate with(final Object object);

    /**
     * Inserts the exception to this template.
     *
     * @param exception to insert.
     * @return a reference to this object.
     */
    LogEntryTemplate with(final Throwable exception);

    /**
     * Inserts the decimal-64 floating-point value (IEEE 754-2008 Decimal Floating-Point Arithmetic) to this template.
     *
     * @param decimal to insert.
     * @return a reference to this object.
     */
    LogEntryTemplate withDecimal64(final long decimal);

    /**
     * Inserts the timestamp in milliseconds since Epoch in "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" format to this template.
     *
     * @param timestamp in milliseconds since Epoch to insert.
     * @return a reference to this object.
     */
    LogEntryTemplate withTimestamp(final long timestamp);

    /**
     * Inserts the timestamp in nanoseconds since Epoch in "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'" format to this template.
     *
     * @param timestampNs in nanoseconds since Epoch to insert.
     * @return a reference to this object.
     */
    LogEntryTemplate withTimestampNs(final long timestampNs);

    /**
     * Inserts the date part of the timestamp in milliseconds since Epoch in "yyyy-MM-dd" format to this template.
     *
     * @param timestamp in milliseconds since Epoch to insert.
     * @return a reference to this object.
     */
    LogEntryTemplate withDate(final long timestamp);

    /**
     * Inserts the date part of the timestamp in nanoseconds since Epoch in "yyyy-MM-dd" format to this template.
     *
     * @param timestampNs in nanoseconds since Epoch to insert.
     * @return a reference to this object.
     */
    LogEntryTemplate withDateNs(final long timestampNs);

    /**
     * Inserts the time part of the timestamp in milliseconds since Epoch in "HH:mm:ss.SSS" format to this template.
     *
     * @param timestamp in milliseconds since Epoch to insert.
     * @return a reference to this object.
     */
    LogEntryTemplate withTime(final long timestamp);

    /**
     * Inserts the time part of the timestamp in nanoseconds since Epoch in "HH:mm:ss.SSSSSSSSS" format to this template.
     *
     * @param timestampNs in nanoseconds since Epoch to insert.
     * @return a reference to this object.
     */
    LogEntryTemplate withTimeNs(final long timestampNs);

    /**
     * Inserts the alphanumeric value to this template.
     *
     * @param alphanumeric to insert.
     * @return a reference to this object.
     */
    LogEntryTemplate withAlphanumeric(final long alphanumeric);

}
