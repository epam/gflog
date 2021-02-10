package com.epam.deltix.gflog.core.service;

import java.nio.charset.StandardCharsets;


final class LogAsciiEntry extends LogLimitedEntry {

    LogAsciiEntry(final String truncationSuffix, final int initialCapacity, final int maxCapacity) {
        super(truncationSuffix, initialCapacity, maxCapacity);
    }

    @Override
    String substring() {
        return new String(array, 0, length, StandardCharsets.US_ASCII);
    }

    @Override
    String substring(final int start, final int end) {
        return new String(array, start, end - start, StandardCharsets.US_ASCII);
    }

    @Override
    void appendChar(final char value) {
        appendAsciiChar(value);
    }

    @Override
    void appendString(final String value) {
        appendAsciiString(value);
    }

    @Override
    void appendString(final String value, final int start, final int end) {
        appendAsciiString(value, start, end);
    }

    @Override
    void appendCharSequence(final CharSequence value) {
        appendAsciiCharSequence(value);
    }

    @Override
    void appendCharSequence(final CharSequence value, final int start, final int end) {
        appendAsciiCharSequence(value, start, end);
    }

}
