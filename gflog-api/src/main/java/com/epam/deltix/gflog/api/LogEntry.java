package com.epam.deltix.gflog.api;

import javax.annotation.CheckReturnValue;


public interface LogEntry extends AppendableEntry {

    /**
     * Appends the character.
     */
    @CheckReturnValue
    LogEntry append(final char value);

    /**
     * Appends the char sequence.
     */
    @CheckReturnValue
    LogEntry append(final CharSequence value);

    /**
     * Appends the char sequence from the start position to the end position.
     */
    @CheckReturnValue
    LogEntry append(final CharSequence value, final int start, final int end);

    /**
     * Appends the string.
     */
    @CheckReturnValue
    LogEntry append(final String value);

    /**
     * Appends the string from the start position to the end position.
     */
    @CheckReturnValue
    LogEntry append(final String value, final int start, final int end);

    /**
     * Appends the boolean.
     */
    @CheckReturnValue
    LogEntry append(final boolean value);

    /**
     * Appends the integer.
     */
    @CheckReturnValue
    LogEntry append(final int value);

    /**
     * Appends the long.
     */
    @CheckReturnValue
    LogEntry append(final long value);

    /**
     * Appends the double with the default precision.
     */
    @CheckReturnValue
    LogEntry append(final double value);

    /**
     * Appends the double with the specified precision.
     */
    @CheckReturnValue
    LogEntry append(final double value, final int precision);

    /**
     * Appends the object that implements Loggable interface.
     */
    @CheckReturnValue
    LogEntry append(final Loggable object);

    /**
     * Appends the object using toString() method.
     */
    @CheckReturnValue
    LogEntry append(final Object object);

    /**
     * Appends the exception.
     */
    @CheckReturnValue
    LogEntry append(final Throwable exception);

    /**
     * Appends the decimal-64 floating point value (IEEE 754-2008 Decimal Floating-Point Arithmetic specification).
     */
    @CheckReturnValue
    LogEntry appendDecimal64(final long decimal);

    /**
     * Appends the timestamp in milliseconds since Epoch in "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" format.
     */
    @CheckReturnValue
    LogEntry appendTimestamp(final long timestamp);

    /**
     * Appends the date part of the timestamp in milliseconds since Epoch in "yyyy-MM-dd" format.
     */
    @CheckReturnValue
    LogEntry appendDate(final long timestamp);

    /**
     * Appends the time part of the timestamp in milliseconds since Epoch in "HH:mm:ss.SSS" format.
     */
    @CheckReturnValue
    LogEntry appendTime(final long timestamp);

    /**
     * Appends the alphanumeric value.
     */
    @CheckReturnValue
    LogEntry appendAlphanumeric(final long alphanumeric);


    /**
     * Appends the character and commits the log entry.
     */
    void appendLast(final char value);

    /**
     * Appends the char sequence and commits the log entry.
     */
    void appendLast(final CharSequence value);

    /**
     * Appends the char sequence from start position to end position and commits the log entry.
     */
    void appendLast(final CharSequence value, final int start, final int end);

    /**
     * Appends the string and commits the log entry.
     */
    void appendLast(final String value);

    /**
     * Appends the string from start position to end position and commits the log entry.
     */
    void appendLast(final String value, final int start, final int end);

    /**
     * Appends the boolean and commits the log entry.
     */
    void appendLast(final boolean value);

    /**
     * Appends the integer and commits the log entry.
     */
    void appendLast(final int value);

    /**
     * Appends the long and commits the log entry.
     */
    void appendLast(final long value);

    /**
     * Appends the double with the default precision and commits the log entry.
     */
    void appendLast(final double value);

    /**
     * Appends the double with the specified precision and commits the log entry.
     */
    void appendLast(final double value, final int precision);

    /**
     * Appends the object that implements Loggable interface and commits the log entry.
     */
    void appendLast(final Loggable object);

    /**
     * Appends the object using toString() method and commits the log entry.
     */
    void appendLast(final Object object);

    /**
     * Appends the exception and commits the log entry.
     */
    void appendLast(final Throwable exception);

    /**
     * Appends the decimal-64 floating point value (IEEE 754-2008 Decimal Floating-Point Arithmetic specification) and commits the log entry.
     */
    void appendDecimal64Last(final long decimal);

    /**
     * Appends the timestamp in milliseconds since Epoch in "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" format and commits the log entry.
     */
    void appendTimestampLast(final long timestamp);

    /**
     * Appends the date part of the timestamp in milliseconds since Epoch in "yyyy-MM-dd" format and commits the log entry.
     */
    void appendDateLast(final long timestamp);

    /**
     * Appends the time part of the timestamp in milliseconds since Epoch in "HH:mm:ss.SSS" format and commits the log entry.
     */
    void appendTimeLast(final long timestamp);

    /**
     * Appends the alphanumeric value and commits the log entry.
     */
    void appendAlphanumericLast(final long alphanumeric);


    /**
     * Aborts the log operation.
     */
    void abort();

    /**
     * Commits the log operation.
     */
    void commit();

}

