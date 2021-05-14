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
    void doAppendChar(final char value) {
        doAppendUtf8Char(value);
    }

    @Override
    void doAppendString(final String value) {
        doAppendUtf8String(value);
    }

    @Override
    void doAppendString(final String value, final int start, final int end) {
        doAppendUtf8String(value, start, end);
    }

    @Override
    void doAppendCharSequence(final CharSequence value) {
        doAppendUtf8CharSequence(value);
    }

    @Override
    void doAppendCharSequence(final CharSequence value, final int start, final int end) {
        doAppendUtf8CharSequence(value, start, end);
    }

}
