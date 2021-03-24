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
    void doAppendChar(final char value) {
        doAppendAsciiChar(value);
    }

    @Override
    void doAppendString(final String value) {
        doAppendAsciiString(value);
    }

    @Override
    void doAppendString(final String value, final int start, final int end) {
        doAppendAsciiString(value, start, end);
    }

    @Override
    void doAppendCharSequence(final CharSequence value) {
        doAppendAsciiCharSequence(value);
    }

    @Override
    void doAppendCharSequence(final CharSequence value, final int start, final int end) {
        doAppendAsciiCharSequence(value, start, end);
    }

}
