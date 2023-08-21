package com.epam.deltix.gflog.api;


public final class NoOpLogEntry implements LogEntry, LogEntryTemplate {

    public static final NoOpLogEntry INSTANCE = new NoOpLogEntry();

    private NoOpLogEntry() {
    }

    @Override
    public NoOpLogEntry append(final char value) {
        return this;
    }

    @Override
    public NoOpLogEntry append(final CharSequence value) {
        return this;
    }

    @Override
    public NoOpLogEntry append(final CharSequence value, final int start, final int end) {
        return this;
    }

    @Override
    public NoOpLogEntry append(final String value) {
        return this;
    }

    @Override
    public NoOpLogEntry append(final String value, final int start, final int end) {
        return this;
    }

    @Override
    public NoOpLogEntry append(final boolean value) {
        return this;
    }

    @Override
    public NoOpLogEntry append(final int value) {
        return this;
    }

    @Override
    public NoOpLogEntry append(final long value) {
        return this;
    }

    @Override
    public NoOpLogEntry append(final double value) {
        return this;
    }

    @Override
    public NoOpLogEntry append(final double value, final int precision) {
        return this;
    }

    @Override
    public NoOpLogEntry append(final Loggable object) {
        return this;
    }

    @Override
    public NoOpLogEntry append(final Object object) {
        return this;
    }

    @Override
    public NoOpLogEntry append(final Throwable exception) {
        return this;
    }

    @Override
    public NoOpLogEntry appendDecimal64(final long decimal) {
        return this;
    }

    @Override
    public NoOpLogEntry appendTimestamp(final long timestamp) {
        return this;
    }

    @Override
    public LogEntry appendTimestampNs(long timestampNs) {
        return this;
    }

    @Override
    public NoOpLogEntry appendDate(final long timestamp) {
        return this;
    }

    @Override
    public LogEntry appendDateNs(long timestampNs) {
        return this;
    }

    @Override
    public NoOpLogEntry appendTime(final long timestamp) {
        return this;
    }

    @Override
    public LogEntry appendTimeNs(long timestampNs) {
        return this;
    }

    @Override
    public NoOpLogEntry appendAlphanumeric(final long alphanumeric) {
        return this;
    }

    @Override
    public void appendLast(final char value) {
    }

    @Override
    public void appendLast(final CharSequence value) {
    }

    @Override
    public void appendLast(final CharSequence value, final int start, final int end) {
    }

    @Override
    public void appendLast(final String value) {
    }

    @Override
    public void appendLast(final String value, final int start, final int end) {
    }

    @Override
    public void appendLast(final boolean value) {
    }

    @Override
    public void appendLast(final int value) {
    }

    @Override
    public void appendLast(final long value) {
    }

    @Override
    public void appendLast(final double value) {
    }

    @Override
    public void appendLast(final double value, final int precision) {
    }

    @Override
    public void appendLast(final Loggable object) {
    }

    @Override
    public void appendLast(final Object object) {
    }

    @Override
    public void appendLast(final Throwable exception) {
    }

    @Override
    public void appendDecimal64Last(final long decimal) {
    }

    @Override
    public void appendTimestampLast(final long timestamp) {
    }

    @Override
    public void appendTimestampNsLast(long timestampNs) {
    }

    @Override
    public void appendDateLast(final long timestamp) {
    }

    @Override
    public void appendDateNsLast(long timestampNs) {
    }

    @Override
    public void appendTimeLast(final long timestamp) {
    }

    @Override
    public void appendTimeNsLast(long timestampNs) {
    }

    @Override
    public void appendAlphanumericLast(final long alphanumeric) {
    }

    @Override
    public void commit() {
    }

    @Override
    public void abort() {
    }

    @Override
    public NoOpLogEntry with(final char value) {
        return this;
    }

    @Override
    public NoOpLogEntry with(final CharSequence value) {
        return this;
    }

    @Override
    public NoOpLogEntry with(final CharSequence value, final int start, final int end) {
        return this;
    }

    @Override
    public NoOpLogEntry with(final String value) {
        return this;
    }

    @Override
    public NoOpLogEntry with(final String value, final int start, final int end) {
        return this;
    }

    @Override
    public NoOpLogEntry with(final boolean value) {
        return this;
    }

    @Override
    public NoOpLogEntry with(final int value) {
        return this;
    }

    @Override
    public NoOpLogEntry with(final long value) {
        return this;
    }

    @Override
    public NoOpLogEntry with(final double value) {
        return this;
    }

    @Override
    public NoOpLogEntry with(final double value, final int precision) {
        return this;
    }

    @Override
    public NoOpLogEntry with(final Loggable object) {
        return this;
    }

    @Override
    public NoOpLogEntry with(final Object object) {
        return this;
    }

    @Override
    public NoOpLogEntry with(final Throwable exception) {
        return this;
    }

    @Override
    public NoOpLogEntry withDecimal64(final long decimal) {
        return this;
    }

    @Override
    public NoOpLogEntry withTimestamp(final long timestamp) {
        return this;
    }

    @Override
    public LogEntryTemplate withTimestampNs(long timestampNs) {
        return this;
    }

    @Override
    public NoOpLogEntry withDate(final long timestamp) {
        return this;
    }

    @Override
    public LogEntryTemplate withDateNs(long timestampNs) {
        return this;
    }

    @Override
    public NoOpLogEntry withTime(final long timestamp) {
        return this;
    }

    @Override
    public LogEntryTemplate withTimeNs(long timestampNs) {
        return this;
    }

    @Override
    public NoOpLogEntry withAlphanumeric(final long alphanumeric) {
        return this;
    }

}
