package com.epam.deltix.gflog.api;


public interface AppendableEntry extends Appendable {

    /**
     * Appends the character to this entry.
     *
     * @param value to append.
     * @return a reference to this object.
     */
    AppendableEntry append(final char value);

    /**
     * Appends the sequence to this entry.
     *
     * @param value to append.
     * @return a reference to this object.
     */
    AppendableEntry append(final CharSequence value);

    /**
     * Appends the sequence from the start position (inclusively) to the end position (exclusively) to this entry.
     *
     * @param value to append.
     * @param start which position (inclusively) to start from.
     * @param end   which position (exclusively) to end to.
     * @return a reference to this object.
     */
    AppendableEntry append(final CharSequence value, final int start, final int end);

    /**
     * Appends the string to this entry.
     *
     * @param value to append.
     * @return a reference to this object.
     */
    AppendableEntry append(final String value);

    /**
     * Appends the string from the start position (inclusively) to the end position (exclusively) to this entry.
     *
     * @param value to append.
     * @param start which position (inclusively) to start from.
     * @param end   which position (exclusively) to end to.
     * @return a reference to this object.
     */
    AppendableEntry append(final String value, final int start, final int end);

    /**
     * Appends the boolean to this entry.
     *
     * @param value to append.
     * @return a reference to this object.
     */
    AppendableEntry append(final boolean value);

    /**
     * Appends the integer to this entry.
     *
     * @param value to append.
     * @return a reference to this object.
     */
    AppendableEntry append(final int value);

    /**
     * Appends the long to this entry.
     *
     * @param value to append.
     * @return a reference to this object.
     */
    AppendableEntry append(final long value);

    /**
     * Appends the double to this entry.
     *
     * @param value to append.
     * @return a reference to this object.
     */
    AppendableEntry append(final double value);

    /**
     * Appends the double with the precision to this entry.
     *
     * @param value     to append.
     * @param precision after the point.
     * @return a reference to this object.
     */
    AppendableEntry append(final double value, final int precision);

    /**
     * Appends the object which implements com.epam.deltix.gflog.api.Loggable interface to this entry.
     *
     * @param object to append.
     * @return a reference to this object.
     */
    AppendableEntry append(final Loggable object);

    /**
     * Appends the object to this entry.
     *
     * @param object to append.
     * @return a reference to this object.
     */
    AppendableEntry append(final Object object);

    /**
     * Appends the exception to this entry.
     *
     * @param exception to append.
     * @return a reference to this object.
     */
    AppendableEntry append(final Throwable exception);

    /**
     * Appends the decimal-64 floating-point value (IEEE 754-2008 Decimal Floating-Point Arithmetic) to this entry.
     *
     * @param decimal to append.
     * @return a reference to this object.
     */
    AppendableEntry appendDecimal64(final long decimal);

    /**
     * Appends the timestamp in milliseconds since Epoch in "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" format to this entry.
     *
     * @param timestamp in milliseconds since Epoch to append.
     * @return a reference to this object.
     */
    AppendableEntry appendTimestamp(final long timestamp);

    /**
     * Appends the timestamp in nanoseconds since Epoch in "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'" format to this entry.
     *
     * @param timestampNs in nanoseconds since Epoch to append.
     * @return a reference to this object.
     */
    AppendableEntry appendTimestampNs(final long timestampNs);

    /**
     * Appends the date part of the timestamp in milliseconds since Epoch in "yyyy-MM-dd" format to this entry.
     *
     * @param timestamp in milliseconds since Epoch to append.
     * @return a reference to this object.
     */
    AppendableEntry appendDate(final long timestamp);

    /**
     * Appends the date part of the timestamp in nanoseconds since Epoch in "yyyy-MM-dd" format to this entry.
     *
     * @param timestampNs in nanoseconds since Epoch to append.
     * @return a reference to this object.
     */
    AppendableEntry appendDateNs(final long timestampNs);

    /**
     * Appends the time part of the timestamp in milliseconds since Epoch in "HH:mm:ss.SSS" format to this entry.
     *
     * @param timestamp in milliseconds since Epoch to append.
     * @return a reference to this object.
     */
    AppendableEntry appendTime(final long timestamp);

    /**
     * Appends the time part of the timestamp in nanoseconds since Epoch in "HH:mm:ss.SSSSSSSSS" format to this entry.
     *
     * @param timestampNs in nanoseconds since Epoch to append.
     * @return a reference to this object.
     */
    AppendableEntry appendTimeNs(final long timestampNs);

    /**
     * Appends the alphanumeric value to this entry.
     *
     * @param alphanumeric to append.
     * @return a reference to this object.
     */
    AppendableEntry appendAlphanumeric(final long alphanumeric);

}
