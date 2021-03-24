package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.api.AppendableEntry;
import com.epam.deltix.gflog.api.Loggable;
import com.epam.deltix.gflog.core.util.Buffer;
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
            doAppendChar(value);
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
                doAppendCharSequence(value);
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
                doAppendCharSequence(value, start, end);
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
                doAppendString(value);
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
                doAppendString(value, start, end);
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
                doAppendString(text);
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
                Formatting.verifyTimestamp(timestamp);
                ensureSpace(LENGTH_OF_TIMESTAMP);
                length = Formatting.formatTimestamp(timestamp, array, length);
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
                Formatting.verifyTimestamp(timestamp);
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
                Formatting.verifyTimestamp(timestamp);
                ensureSpace(LENGTH_OF_TIME);
                length = Formatting.formatTime(timestamp, array, length);
            }

            verifyLimit();
        }

        return this;
    }

    void appendUtf8Bytes(final Buffer bytes, final int bytesOffset, final int bytesLength) {
        if (!truncated) {
            if (bytes == null) {
                formatNull();
                verifyLimit();
            } else {
                doAppendUtf8Bytes(bytes, bytesOffset, bytesLength);
            }
        }
    }

    abstract void doAppendChar(final char value);


    abstract void doAppendString(final String value);

    abstract void doAppendString(final String value, final int start, final int end);


    abstract void doAppendCharSequence(final CharSequence value);

    abstract void doAppendCharSequence(final CharSequence value, final int start, final int end);


    abstract String substring(final int start, final int end);

    abstract String substring();


    final void doAppendAsciiChar(final char value) {
        ensureSpace();
        length = Formatting.formatByte((byte) (value & 0x7F), array, length);
        verifyLimit();
    }

    final void doAppendUtf8Char(final char value) {
        ensureSpace(4);

        final int before = length;
        length = Formatting.formatUtf8Char(value, array, before);

        if (length > limit) {
            truncate(before);
        }
    }

    final void doAppendAsciiString(final String value) {
        final int size = value.length();
        final int remaining = limit - length;

        if (size <= remaining) {
            ensureSpace(size);
            length = Formatting.formatAsciiString(value, array, length);
        } else {
            ensureSpace(remaining + truncationSuffix.length());
            length = Formatting.formatAsciiString(value, 0, remaining, array, length);
            length = Formatting.formatAsciiString(truncationSuffix, array, length);
            truncated = true;
        }
    }

    final void doAppendAsciiString(final String value, final int start, final int end) {
        if (start < 0 || end < start || end > value.length()) {
            throw new IndexOutOfBoundsException();
        }

        final int size = end - start;
        final int remaining = limit - length;

        if (size <= remaining) {
            ensureSpace(size);
            length = Formatting.formatAsciiString(value, start, end, array, length);
        } else {
            ensureSpace(remaining + truncationSuffix.length());
            length = Formatting.formatAsciiString(value, start, start + remaining, array, length);
            length = Formatting.formatAsciiString(truncationSuffix, array, length);
            truncated = true;
        }
    }

    final void doAppendAsciiCharSequence(final CharSequence value) {
        final int size = value.length();
        final int remaining = limit - length;

        if (size <= remaining) {
            ensureSpace(size);
            length = Formatting.formatAsciiCharSequence(value, array, length);
        } else {
            ensureSpace(remaining + truncationSuffix.length());
            length = Formatting.formatAsciiCharSequence(value, 0, remaining, array, length);
            length = Formatting.formatAsciiString(truncationSuffix, array, length);
            truncated = true;
        }
    }

    final void doAppendAsciiCharSequence(final CharSequence value, final int start, final int end) {
        if (start < 0 || end < start || end > value.length()) {
            throw new IndexOutOfBoundsException();
        }

        final int size = end - start;
        final int remaining = limit - length;

        if (size <= remaining) {
            ensureSpace(size);
            length = Formatting.formatAsciiCharSequence(value, start, end, array, length);
        } else {
            ensureSpace(remaining + truncationSuffix.length());
            length = Formatting.formatAsciiCharSequence(value, start, start + remaining, array, length);
            length = Formatting.formatAsciiString(truncationSuffix, array, length);
            truncated = true;
        }
    }

    final void doAppendUtf8String(final String value) {
        final int size = value.length();
        final int remaining = limit - length;

        if (size <= (remaining >> 2)) {
            ensureSpace(size << 2);
            length = Formatting.formatUtf8String(value, array, length);
        } else {
            ensureSpace(remaining + truncationSuffix.length());

            final int bound = Util.limitUtf8Index(value, 0, size, remaining);
            length = Formatting.formatUtf8String(value, 0, bound, array, length);

            if (bound < size) {
                length = Formatting.formatAsciiString(truncationSuffix, array, length);
                truncated = true;
            }
        }
    }

    final void doAppendUtf8String(final String value, final int start, final int end) {
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

            final int bound = Util.limitUtf8Index(value, start, end, remaining);
            length = Formatting.formatUtf8String(value, start, bound, array, length);

            if (bound < end) {
                length = Formatting.formatAsciiString(truncationSuffix, array, length);
                truncated = true;
            }
        }
    }

    final void doAppendUtf8CharSequence(final CharSequence value) {
        final int size = value.length();
        final int remaining = limit - length;

        if (size <= (remaining >> 2)) {
            ensureSpace(size << 2);
            length = Formatting.formatUtf8CharSequence(value, array, length);
        } else {
            ensureSpace(remaining + truncationSuffix.length());

            final int bound = Util.limitUtf8Index(value, 0, size, remaining);
            length = Formatting.formatUtf8CharSequence(value, 0, bound, array, length);

            if (bound < size) {
                length = Formatting.formatAsciiString(truncationSuffix, array, length);
                truncated = true;
            }
        }
    }

    final void doAppendUtf8CharSequence(final CharSequence value, final int start, final int end) {
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

            final int bound = Util.limitUtf8Index(value, start, end, remaining);
            length = Formatting.formatUtf8CharSequence(value, start, bound, array, length);

            if (bound < end) {
                length = Formatting.formatAsciiString(truncationSuffix, array, length);
                truncated = true;
            }
        }
    }

    final void doAppendUtf8Bytes(final Buffer bytes, final int bytesOffset, final int bytesLength) {
        final int remaining = limit - length;

        if (bytesLength <= remaining) {
            ensureSpace(bytesLength);
            length = Formatting.formatBytes(bytes, bytesOffset, bytesLength, array, length);
        } else {
            final int bytesLimit = Util.limitUtf8Length(bytes, bytesOffset, bytesLength, remaining);
            ensureSpace(remaining + truncationSuffix.length());

            length = Formatting.formatBytes(bytes, bytesOffset, bytesLimit, array, length);
            truncated = true;
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
        Formatting.verifyDoublePrecision(precision);

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

        ensureSpace(Formatting.maxLengthOfDecimal64(exponent));
        length = Formatting.formatDecimal64(sign, exponent, significand, array, length);
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

        ensureSpace(Formatting.maxLengthOfDecimal64(exponent));
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
