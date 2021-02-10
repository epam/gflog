package com.epam.deltix.gflog.core.service;

import java.nio.charset.StandardCharsets;


final class LogUtf8Entry extends LogLimitedEntry {

    LogUtf8Entry(final String truncationSuffix, final int initialCapacity, final int maxCapacity) {
        super(truncationSuffix, initialCapacity, maxCapacity);
    }

    @Override
    String substring() {
        return new String(array, 0, length, StandardCharsets.UTF_8);
    }

    @Override
    String substring(final int start, final int end) {
        return new String(array, start, end - start, StandardCharsets.UTF_8);
    }

    @Override
    void appendChar(final char value) {
        appendUtf8Char(value);
    }

    @Override
    void appendString(final String value) {
        appendUtf8String(value);
    }

    @Override
    void appendString(final String value, final int start, final int end) {
        appendUtf8String(value, start, end);
    }

    @Override
    void appendCharSequence(final CharSequence value) {
        appendUtf8CharSequence(value);
    }

    @Override
    void appendCharSequence(final CharSequence value, final int start, final int end) {
        appendUtf8CharSequence(value, start, end);
    }

}
