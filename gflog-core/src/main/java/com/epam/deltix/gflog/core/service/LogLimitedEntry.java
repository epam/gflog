package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.api.AppendableEntry;
import com.epam.deltix.gflog.api.Loggable;
import com.epam.deltix.gflog.core.util.Formatting;
import com.epam.deltix.gflog.core.util.Util;

import javax.annotation.Nonnegative;
import java.math.BigDecimal;
import java.util.Arrays;

import static com.epam.deltix.gflog.core.util.Formatting.*;


abstract class LogLimitedEntry implements AppendableEntry {

    static final int MAX_DIFFERENCE_BETWEEN_LIMIT_AND_CAPACITY = 512;

    final String truncationSuffix;
    final int limit;

    byte[] array;
    int length;
    int capacity;

    boolean truncated;

    LogLimitedEntry(final String truncationSuffix, final int initialCapacity, final int maxCapacity) {
        final int capacity = Util.align(initialCapacity, Util.SIZE_OF_LONG);

        this.truncationSuffix = truncationSuffix;
        this.limit = maxCapacity;
        this.array = new byte[capacity];
        this.capacity = capacity;
    }

    final int length() {
        return length;
    }

    final byte[] array() {
        return array;
    }

    final void clear() {
        truncated = false;
        length = 0;
    }

    final void reset(final int length) {
        this.truncated = false;
        this.length = length;
    }

    @Override
    public final LogLimitedEntry append(final boolean value) {
        if (!truncated) {
            ensureSpace(MAX_LENGTH_OF_BOOLEAN);
            length = Formatting.formatBoolean(value, array, length);
            verifyLimit();
        }

        return this;
    }

    @Override
    public final LogLimitedEntry append(final char value) {
        if (!truncated) {
            appendChar(value);
        }

        return this;
    }

    @Override
    public final LogLimitedEntry append(final int value) {
        if (!truncated) {
            ensureSpace(MAX_LENGTH_OF_INT);
            length = Formatting.formatInt(value, array, length);
            verifyLimit();
        }

        return this;
    }

    @Override
    public final LogLimitedEntry append(final long value) {
        if (!truncated) {
            ensureSpace(MAX_LENGTH_OF_LONG);
            length = Formatting.formatLong(value, array, length);
            verifyLimit();
        }

        return this;
    }

    @Override
    public final LogLimitedEntry append(final double value) {
        if (!truncated) {
            formatDouble(value);
            verifyLimit();
        }

        return this;
    }

    @Override
    public final LogLimitedEntry append(final double value, @Nonnegative final int precision) {
        if (!truncated) {
            formatDouble(value, precision);
            verifyLimit();
        }

        return this;
    }

    @Override
    public final LogLimitedEntry append(final CharSequence value) {
        if (!truncated) {
            if (value == null) {
                formatNull();
                verifyLimit();
            } else {
                appendCharSequence(value);
            }
        }

        return this;
    }

    @Override
    public final LogLimitedEntry append(final CharSequence value, final int start, final int end) {
        if (!truncated) {
            if (value == null) {
                formatNull();
                verifyLimit();
            } else {
                appendCharSequence(value, start, end);
            }
        }

        return this;
    }

    @Override
    public final LogLimitedEntry append(final String value) {
        if (!truncated) {
            if (value == null) {
                formatNull();
                verifyLimit();
            } else {
                appendString(value);
            }
        }

        return this;
    }

    @Override
    public final LogLimitedEntry append(final String value, final int start, final int end) {
        if (!truncated) {
            if (value == null) {
                formatNull();
                verifyLimit();
            } else {
                appendString(value, start, end);
            }
        }

        return this;
    }

    @Override
    public final LogLimitedEntry append(final Loggable object) {
        if (!truncated) {
            if (object == null) {
                formatNull();
                verifyLimit();
            } else {
                object.appendTo(this);
            }
        }

        return this;
    }

    @Override
    public final LogLimitedEntry append(final Object value) {
        if (!truncated) {
            String text = null;

            if (value != null) {
                text = value.toString();
            }

            if (text == null) {
                formatNull();
                verifyLimit();
            } else {
                appendString(text);
            }
        }

        return this;
    }

    @Override
    public final LogLimitedEntry append(final Throwable exception) {
        if (!truncated) {
            if (exception == null) {
                formatNull();
                verifyLimit();
            } else {
                LogEntryUtil.appendException(exception, this);
            }
        }

        return this;
    }

    @Override
    public final LogLimitedEntry appendDecimal64(final long decimal) {
        if (!truncated) {
            formatDecimal64(decimal);
            verifyLimit();
        }

        return this;
    }

    @Override
    public final LogLimitedEntry appendAlphanumeric(final long value) {
        if (!truncated) {
            if (value == Long.MIN_VALUE) {
                formatNull();
            } else {
                ensureSpace(MAX_LENGTH_OF_ALPHANUMERIC);
                length = Formatting.formatAlphanumeric(value, array, length);
            }

            verifyLimit();
        }

        return this;
    }

    @Override
    public final LogLimitedEntry appendTimestamp(final long timestamp) {
        if (!truncated) {
            if (timestamp == Long.MIN_VALUE) {
                formatNull();
            } else {
                verifyTimestamp(timestamp);
                ensureSpace(LENGTH_OF_TIMESTAMP);
                length = formatTimestamp(timestamp, array, length);
            }

            verifyLimit();
        }

        return this;
    }

    @Override
    public final LogLimitedEntry appendDate(final long timestamp) {
        if (!truncated) {
            if (timestamp == Long.MIN_VALUE) {
                formatNull();
            } else {
                verifyTimestamp(timestamp);
                ensureSpace(LENGTH_OF_DATE);
                length = Formatting.formatDate(timestamp, array, length);
            }

            verifyLimit();
        }

        return this;
    }

    @Override
    public final LogLimitedEntry appendTime(final long timestamp) {
        if (!truncated) {
            if (timestamp == Long.MIN_VALUE) {
                formatNull();
            } else {
                verifyTimestamp(timestamp);
                ensureSpace(LENGTH_OF_TIME);
                length = Formatting.formatTime(timestamp, array, length);
            }

            verifyLimit();
        }

        return this;
    }

    abstract void appendChar(final char value);


    abstract void appendString(final String value);

    abstract void appendString(final String value, final int start, final int end);


    abstract void appendCharSequence(final CharSequence value);

    abstract void appendCharSequence(final CharSequence value, final int start, final int end);


    abstract String substring(final int start, final int end);

    abstract String substring();


    final void appendAsciiChar(final char value) {
        ensureSpace();
        length = formatByte((byte) (value & 0x7F), array, length);
        verifyLimit();
    }

    final void appendUtf8Char(final char value) {
        ensureSpace(4);

        final int before = length;
        length = Formatting.formatUtf8Char(value, array, before);

        if (length > limit) {
            truncate(before);
        }
    }

    final void appendAsciiString(final String value) {
        final int size = value.length();
        final int remaining = limit - length;

        if (size <= remaining) {
            ensureSpace(size);
            length = formatAsciiString(value, array, length);
        } else {
            ensureSpace(remaining + truncationSuffix.length());
            length = formatAsciiString(value, 0, remaining, array, length);
            length = formatAsciiString(truncationSuffix, array, length);
            truncated = true;
        }
    }

    final void appendAsciiString(final String value, final int start, final int end) {
        if (start < 0 || end < start || end > value.length()) {
            throw new IndexOutOfBoundsException();
        }

        final int size = end - start;
        final int remaining = limit - length;

        if (size <= remaining) {
            ensureSpace(size);
            length = formatAsciiString(value, start, end, array, length);
        } else {
            ensureSpace(remaining + truncationSuffix.length());
            length = formatAsciiString(value, start, start + remaining, array, length);
            length = formatAsciiString(truncationSuffix, array, length);
            truncated = true;
        }
    }

    final void appendAsciiCharSequence(final CharSequence value) {
        final int size = value.length();
        final int remaining = limit - length;

        if (size <= remaining) {
            ensureSpace(size);
            length = formatAsciiCharSequence(value, array, length);
        } else {
            ensureSpace(remaining + truncationSuffix.length());
            length = formatAsciiCharSequence(value, 0, remaining, array, length);
            length = formatAsciiString(truncationSuffix, array, length);
            truncated = true;
        }
    }

    final void appendAsciiCharSequence(final CharSequence value, final int start, final int end) {
        if (start < 0 || end < start || end > value.length()) {
            throw new IndexOutOfBoundsException();
        }

        final int size = end - start;
        final int remaining = limit - length;

        if (size <= remaining) {
            ensureSpace(size);
            length = formatAsciiCharSequence(value, start, end, array, length);
        } else {
            ensureSpace(remaining + truncationSuffix.length());
            length = formatAsciiCharSequence(value, start, start + remaining, array, length);
            length = formatAsciiString(truncationSuffix, array, length);
            truncated = true;
        }
    }

    final void appendUtf8String(final String value) {
        final int size = value.length();
        final int remaining = limit - length;

        if (size <= (remaining >> 2)) {
            ensureSpace(size << 2);
            length = Formatting.formatUtf8String(value, array, length);
        } else {
            ensureSpace(remaining + truncationSuffix.length());

            final int bound = Util.findUtf8Bound(value, 0, size, remaining);
            length = Formatting.formatUtf8String(value, 0, bound, array, length);

            if (bound < size) {
                length = Formatting.formatAsciiString(truncationSuffix, array, length);
                truncated = true;
            }
        }
    }

    final void appendUtf8String(final String value, final int start, final int end) {
        if (start < 0 || end < start || end > value.length()) {
            throw new IndexOutOfBoundsException();
        }

        final int size = end - start;
        final int remaining = limit - length;

        if (size <= (remaining >> 2)) {
            ensureSpace(size << 2);
            length = Formatting.formatUtf8String(value, start, end, array, length);
        } else {
            ensureSpace(remaining + truncationSuffix.length());

            final int bound = Util.findUtf8Bound(value, start, end, remaining);
            length = Formatting.formatUtf8String(value, start, bound, array, length);

            if (bound < end) {
                length = Formatting.formatAsciiString(truncationSuffix, array, length);
                truncated = true;
            }
        }
    }

    final void appendUtf8CharSequence(final CharSequence value) {
        final int size = value.length();
        final int remaining = limit - length;

        if (size <= (remaining >> 2)) {
            ensureSpace(size << 2);
            length = Formatting.formatUtf8CharSequence(value, array, length);
        } else {
            ensureSpace(remaining + truncationSuffix.length());

            final int bound = Util.findUtf8Bound(value, 0, size, remaining);
            length = Formatting.formatUtf8CharSequence(value, 0, bound, array, length);

            if (bound < size) {
                length = Formatting.formatAsciiString(truncationSuffix, array, length);
                truncated = true;
            }
        }
    }

    final void appendUtf8CharSequence(final CharSequence value, final int start, final int end) {
        if (start < 0 || end < start || end > value.length()) {
            throw new IndexOutOfBoundsException();
        }

        final int size = end - start;
        final int remaining = limit - length;

        if (size <= (remaining >> 2)) {
            ensureSpace(size << 2);
            length = Formatting.formatUtf8CharSequence(value, start, end, array, length);
        } else {
            ensureSpace(remaining + truncationSuffix.length());

            final int bound = Util.findUtf8Bound(value, start, end, remaining);
            length = Formatting.formatUtf8CharSequence(value, start, bound, array, length);

            if (bound < end) {
                length = Formatting.formatAsciiString(truncationSuffix, array, length);
                truncated = true;
            }
        }
    }

    final void formatDouble(final double value) {
        if (Double.isNaN(value)) {
            formatNaN();
            return;
        }

        if (Math.abs(value) > MAX_VALUE_OF_DOUBLE) {
            formatBigDouble(value);
            return;
        }

        ensureSpace(MAX_LENGTH_OF_DOUBLE);
        length = Formatting.formatDouble(value, array, length);
    }

    final void formatDouble(final double value, @Nonnegative final int precision) {
        verifyDoublePrecision(precision);

        if (Double.isNaN(value)) {
            formatNaN();
            return;
        }

        if (Math.abs(value) > MAX_VALUE_OF_DOUBLE) {
            formatBigDouble(value);
            return;
        }

        ensureSpace(MAX_LENGTH_OF_DOUBLE);
        length = Formatting.formatDouble(value, precision, array, length);
    }

    final void formatBigDouble(@Nonnegative double value) {
        if (Double.isInfinite(value)) {
            formatInfinity(value < 0);
            return;
        }

        final String string = new BigDecimal(value).stripTrailingZeros().toPlainString();
        final int size = Math.min(string.length(), limit - length + 1);

        ensureSpace(size);
        length = Formatting.formatAsciiString(string, 0, size, array, length);
    }

    final void formatDecimal64(final long value) {
        if ((value & DECIMAL_64_SPECIAL_MASK) == DECIMAL_64_SPECIAL_MASK) { // special case: s 11xxx
            formatSpecialDecimal64(value);
            return;
        }

        final long significand = (value & DECIMAL_64_SIGNIFICAND_MASK);

        if (significand == 0) {
            formatZero();
            return;
        }

        final boolean sign = (value < 0);
        final int exponent = (int) ((value >>> DECIMAL_64_EXPONENT_SHIFT) & DECIMAL_64_EXPONENT_MASK) - DECIMAL_64_EXPONENT_BIAS;

        formatDecimal64(sign, exponent, significand);
    }

    final void formatSpecialDecimal64(long value) {
        if ((value & DECIMAL_64_INFINITY_MASK) == DECIMAL_64_INFINITY_MASK) { // infinities or NaN: s 1111x
            if ((value & DECIMAL_64_NAN_MASK) == DECIMAL_64_NAN_MASK) { // NaN: s 11111
                formatNaN();
                return;
            }

            // infinities: s 11110
            formatInfinity(value < 0);
            return;
        }

        // special representation
        final long significand = (value & DECIMAL_64_SIGNIFICAND_MASK_SPECIAL) | DECIMAL_64_SIGNIFICAND_BIT_SPECIAL;

        if (significand > DECIMAL_64_SIGNIFICAND_MAX_VALUE) {
            formatZero();
            return;
        }

        final boolean sign = (value < 0);
        final int exponent = (int) ((value >>> DECIMAL_64_EXPONENT_SHIFT_SPECIAL) & DECIMAL_64_EXPONENT_MASK) - DECIMAL_64_EXPONENT_BIAS;

        formatDecimal64(sign, exponent, significand);
    }

    final void formatDecimal64(final boolean sign, final int exponent, final @Nonnegative long significand) {
        ensureSpace(maxLengthOfDecimal64(exponent));
        length = Formatting.formatDecimal64(sign, exponent, significand, array, length);
    }

    final void formatZero() {
        ensureSpace();
        length = Formatting.formatByte(ZERO, array, length);
    }

    final void formatNull() {
        ensureSpace(LENGTH_OF_NULL);
        length = Formatting.formatNull(array, length);
    }

    final void formatNaN() {
        ensureSpace(LENGTH_OF_NAN);
        length = Formatting.formatNaN(array, length);
    }

    final void formatInfinity(final boolean sign) {
        ensureSpace(LENGTH_OF_INFINITY + 1);

        if (sign) {
            length = Formatting.formatByte(MINUS, array, length);
        }

        length = Formatting.formatInfinity(array, length);
    }

    final void ensureSpace(final int size) {
        final int required = length + size;

        if (required > capacity) {
            grow(required);
        }
    }

    final void ensureSpace() {
        if (length >= capacity) {
            grow(length + 1);
        }
    }

    final void grow(final int requiredCapacity) {
        final int required = Math.min(requiredCapacity << 1, limit + MAX_DIFFERENCE_BETWEEN_LIMIT_AND_CAPACITY);
        final int aligned = Util.align(required, Util.SIZE_OF_LONG);

        array = Arrays.copyOf(array, aligned);
        capacity = aligned;
    }

    final void verifyLimit() {
        if (length > limit) {
            truncate();
        }
    }

    final void truncate() {
        length = limit;

        ensureSpace(truncationSuffix.length());
        length = Formatting.formatAsciiString(truncationSuffix, array, length);

        truncated = true;
    }

    final void truncate(final int limit) {
        length = limit;

        ensureSpace(truncationSuffix.length());
        length = Formatting.formatAsciiString(truncationSuffix, array, length);

        truncated = true;
    }

}
