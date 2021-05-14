package com.epam.deltix.gflog.api;

import javax.annotation.CheckReturnValue;


public interface LogEntry extends AppendableEntry {

    /**
     * Appends the character to this entry.
     *
     * @param value to append.
     * @return a reference to this object.
     */
    @CheckReturnValue
    LogEntry append(final char value);

    /**
     * Appends the sequence to this entry.
     *
     * @param value to append.
     * @return a reference to this object.
     */
    @CheckReturnValue
    LogEntry append(final CharSequence value);

    /**
     * Appends the sequence from the start position (inclusively) to the end position (exclusively) to this entry.
     *
     * @param value to append.
     * @param start which position (inclusively) to start from.
     * @param end   which position (exclusively) to end to.
     * @return a reference to this object.
     */
    @CheckReturnValue
    LogEntry append(final CharSequence value, final int start, final int end);

    /**
     * Appends the string to this entry.
     *
     * @param value to append.
     * @return a reference to this object.
     */
    @CheckReturnValue
    LogEntry append(final String value);

    /**
     * Appends the string from the start position (inclusively) to the end position (exclusively) to this entry.
     *
     * @param value to append.
     * @param start which position (inclusively) to start from.
     * @param end   which position (exclusively) to end to.
     * @return a reference to this object.
     */
    @CheckReturnValue
    LogEntry append(final String value, final int start, final int end);

    /**
     * Appends the boolean to this entry.
     *
     * @param value to append.
     * @return a reference to this object.
     */
    @CheckReturnValue
    LogEntry append(final boolean value);

    /**
     * Appends the integer to this entry.
     *
     * @param value to append.
     * @return a reference to this object.
     */
    @CheckReturnValue
    LogEntry append(final int value);

    /**
     * Appends the long to this entry.
     *
     * @param value to append.
     * @return a reference to this object.
     */
    @CheckReturnValue
    LogEntry append(final long value);

    /**
     * Appends the double to this entry.
     *
     * @param value to append.
     * @return a reference to this object.
     */
    @CheckReturnValue
    LogEntry append(final double value);

    /**
     * Appends the double with the precision to this entry.
     *
     * @param value     to append.
     * @param precision after the point.
     * @return a reference to this object.
     */
    @CheckReturnValue
    LogEntry append(final double value, final int precision);

    /**
     * Appends the object which implements com.epam.deltix.gflog.api.Loggable interface to this entry.
     *
     * @param object to append.
     * @return a reference to this object.
     */
    @CheckReturnValue
    LogEntry append(final Loggable object);

    /**
     * Appends the object to this entry.
     *
     * @param object to append.
     * @return a reference to this object.
     */
    @CheckReturnValue
    LogEntry append(final Object object);

    /**
     * Appends the exception to this entry.
     *
     * @param exception to append.
     * @return a reference to this object.
     */
    @CheckReturnValue
    LogEntry append(final Throwable exception);

    /**
     * Appends the decimal-64 floating-point value (IEEE 754-2008 Decimal Floating-Point Arithmetic) to this entry.
     *
     * @param decimal to append.
     * @return a reference to this object.
     */
    @CheckReturnValue
    LogEntry appendDecimal64(final long decimal);

    /**
     * Appends the timestamp in milliseconds since Epoch in "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" format to this entry.
     *
     * @param timestamp in milliseconds since Epoch to append.
     * @return a reference to this object.
     */
    @CheckReturnValue
    LogEntry appendTimestamp(final long timestamp);

    /**
     * Appends the date part of the timestamp in milliseconds since Epoch in "yyyy-MM-dd" format to this entry.
     *
     * @param timestamp in milliseconds since Epoch to append.
     * @return a reference to this object.
     */
    @CheckReturnValue
    LogEntry appendDate(final long timestamp);

    /**
     * Appends the time part of the timestamp in milliseconds since Epoch in "HH:mm:ss.SSS" format to this entry.
     *
     * @param timestamp in milliseconds since Epoch to append.
     * @return a reference to this object.
     */
    @CheckReturnValue
    LogEntry appendTime(final long timestamp);

    /**
     * Appends the alphanumeric value to this entry.
     *
     * @param alphanumeric to append.
     * @return a reference to this object.
     */
    @CheckReturnValue
    LogEntry appendAlphanumeric(final long alphanumeric);


    /**
     * Appends the character to this entry.
     * Commits this entry.
     *
     * @param value to append.
     */
    void appendLast(final char value);

    /**
     * Appends the sequence to this entry.
     * Commits this entry.
     *
     * @param value to append.
     */
    void appendLast(final CharSequence value);

    /**
     * Appends the sequence from the start position (inclusively) to the end position (exclusively) to this entry.
     * Commits this entry.
     *
     * @param value to append.
     * @param start which position (inclusively) to start from.
     * @param end   which position (exclusively) to end to.
     */
    void appendLast(final CharSequence value, final int start, final int end);

    /**
     * Appends the string to this entry.
     * Commits this entry.
     *
     * @param value to append.
     */
    void appendLast(final String value);

    /**
     * Appends the string from the start position (inclusively) to the end position (exclusively) to this entry.
     * Commits this entry.
     *
     * @param value to append.
     * @param start which position (inclusively) to start from.
     * @param end   which position (exclusively) to end to.
     */
    void appendLast(final String value, final int start, final int end);

    /**
     * Appends the boolean to this entry.
     * Commits this entry.
     *
     * @param value to append.
     */
    void appendLast(final boolean value);

    /**
     * Appends the integer to this entry.
     * Commits this entry.
     *
     * @param value to append.
     */
    void appendLast(final int value);

    /**
     * Appends the long to this entry.
     * Commits this entry.
     *
     * @param value to append.
     */
    void appendLast(final long value);

    /**
     * Appends the double to this entry.
     * Commits this entry.
     *
     * @param value to append.
     */
    void appendLast(final double value);

    /**
     * Appends the double with the precision to this entry.
     * Commits this entry.
     *
     * @param value     to append.
     * @param precision after the point.
     */
    void appendLast(final double value, final int precision);

    /**
     * Appends the object which implements com.epam.deltix.gflog.api.Loggable interface to this entry.
     * Commits this entry.
     *
     * @param object to append.
     */
    void appendLast(final Loggable object);

    /**
     * Appends the object to this entry.
     * Commits this entry.
     *
     * @param object to append.
     */
    void appendLast(final Object object);

    /**
     * Appends the exception to this entry.
     * Commits this entry.
     *
     * @param exception to append.
     */
    void appendLast(final Throwable exception);

    /**
     * Appends the decimal-64 floating-point value (IEEE 754-2008 Decimal Floating-Point Arithmetic) to this entry.
     * Commits this entry.
     *
     * @param decimal to append.
     */
    void appendDecimal64Last(final long decimal);

    /**
     * Appends the timestamp in milliseconds since Epoch in "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" format to this entry.
     * Commits this entry.
     *
     * @param timestamp in milliseconds since Epoch to append.
     */
    void appendTimestampLast(final long timestamp);

    /**
     * Appends the date part of the timestamp in milliseconds since Epoch in "yyyy-MM-dd" format to this entry.
     * Commits this entry.
     * Commits this entry.
     *
     * @param timestamp in milliseconds since Epoch to append.
     */
    void appendDateLast(final long timestamp);

    /**
     * Appends the time part of the timestamp in milliseconds since Epoch in "HH:mm:ss.SSS" format to this entry.
     * Commits this entry.
     *
     * @param timestamp in milliseconds since Epoch to append.
     */
    void appendTimeLast(final long timestamp);

    /**
     * Appends the alphanumeric value to this entry.
     * Commits this entry.
     *
     * @param alphanumeric to append.
     */
    void appendAlphanumericLast(final long alphanumeric);


    /**
     * Aborts this entry. Clears its content after.
     */
    void abort();

    /**
     * Commits this entry. Clears its content after.
     */
    void commit();

}

