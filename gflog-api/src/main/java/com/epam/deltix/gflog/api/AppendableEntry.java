package com.epam.deltix.gflog.api;


public interface AppendableEntry extends Appendable {

    /**
     * Appends the character.
     */
    AppendableEntry append(final char value);

    /**
     * Appends the char sequence.
     */
    AppendableEntry append(final CharSequence value);

    /**
     * Appends the char sequence from the start position to the end position.
     */
    AppendableEntry append(final CharSequence value, final int start, final int end);

    /**
     * Appends the string.
     */
    AppendableEntry append(final String value);

    /**
     * Appends the string from the start position to the end position.
     */
    AppendableEntry append(final String value, final int start, final int end);

    /**
     * Appends the boolean.
     */
    AppendableEntry append(final boolean value);

    /**
     * Appends the integer.
     */
    AppendableEntry append(final int value);

    /**
     * Appends the long.
     */
    AppendableEntry append(final long value);

    /**
     * Appends the double with the default precision.
     */
    AppendableEntry append(final double value);

    /**
     * Appends the double with the specified precision.
     */
    AppendableEntry append(final double value, final int precision);

    /**
     * Appends the object that implements Loggable interface.
     */
    AppendableEntry append(final Loggable object);

    /**
     * Appends the object using toString() method.
     */
    AppendableEntry append(final Object object);

    /**
     * Appends the exception.
     */
    AppendableEntry append(final Throwable exception);

    /**
     * Appends the decimal-64 floating point value (IEEE 754-2008 Decimal Floating-Point Arithmetic specification).
     */
    AppendableEntry appendDecimal64(final long decimal);

    /**
     * Appends the timestamp in milliseconds since Epoch in "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" format.
     */
    AppendableEntry appendTimestamp(final long timestamp);

    /**
     * Appends the date part of the timestamp in milliseconds since Epoch in "yyyy-MM-dd" format.
     */
    AppendableEntry appendDate(final long timestamp);

    /**
     * Appends the time part of the timestamp in milliseconds since Epoch in "HH:mm:ss.SSS" format.
     */
    AppendableEntry appendTime(final long timestamp);

    /**
     * Appends the alphanumeric value.
     */
    AppendableEntry appendAlphanumeric(final long alphanumeric);

}
