package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.api.LogDebug;
import com.epam.deltix.gflog.api.LogEntry;
import com.epam.deltix.gflog.api.LogEntryTemplate;
import com.epam.deltix.gflog.api.Loggable;
import com.epam.deltix.gflog.core.util.Formatting;
import com.epam.deltix.gflog.core.util.MutableBuffer;
import com.epam.deltix.gflog.core.util.Util;


final class LogLocalEntry implements LogEntry, LogEntryTemplate {

    private static final long LENGTH_OFFSET;
    private static final long LOG_NAME_OFFSET;
    private static final long LOG_LEVEL_OFFSET;
    private static final long TIMESTAMP_OFFSET;
    private static final long APPENDER_MASK_OFFSET;
    private static final long THREAD_NAME_LENGTH_OFFSET;
    private static final long THREAD_NAME_DATA_OFFSET;

    static {
        LENGTH_OFFSET = Util.align(Util.ARRAY_BYTE_BASE_OFFSET, Util.SIZE_OF_LONG) + LogRecordEncoder.LENGTH_OFFSET;
        LOG_NAME_OFFSET = LENGTH_OFFSET + LogRecordEncoder.LOG_NAME_OFFSET;
        TIMESTAMP_OFFSET = LENGTH_OFFSET + LogRecordEncoder.TIMESTAMP_OFFSET;
        APPENDER_MASK_OFFSET = LENGTH_OFFSET + LogRecordEncoder.APPENDER_MASK_OFFSET;
        LOG_LEVEL_OFFSET = LENGTH_OFFSET + LogRecordEncoder.LOG_LEVEL_OFFSET;
        THREAD_NAME_LENGTH_OFFSET = LENGTH_OFFSET + LogRecordEncoder.THREAD_NAME_LENGTH_OFFSET;
        THREAD_NAME_DATA_OFFSET = LENGTH_OFFSET + LogRecordEncoder.ROOT_BLOCK_SIZE;
    }

    private final LogService service;
    private final LogLimitedEntry entry;
    private final int offset;
    private final boolean exceptional;

    private String template;
    private int templateIndex;

    private Throwable exception;
    private int exceptionPosition;

    private boolean committed = true;

    LogLocalEntry(final LogService service,
                  final Thread thread,
                  final String truncationSuffix,
                  final int initialCapacity,
                  final int maxCapacity,
                  final boolean utf8,
                  final boolean exceptional) {

        this.service = service;
        this.entry = createEntry(thread, truncationSuffix, initialCapacity, maxCapacity, utf8);
        this.offset = entry.length();
        this.exceptional = exceptional;
    }

    int length() {
        return entry.length() - (int) (LENGTH_OFFSET - Util.ARRAY_BYTE_BASE_OFFSET);
    }

    // region Append

    @Override
    public LogLocalEntry append(final char value) {
        if (verifyNotCommitted()) {
            doAppend(value);
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final CharSequence value) {
        if (verifyNotCommitted()) {
            doAppend(value);
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final CharSequence value, final int start, final int end) {
        if (verifyNotCommitted()) {
            doAppend(value, start, end);
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final String value) {
        if (verifyNotCommitted()) {
            doAppend(value);
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final String value, final int start, final int end) {
        if (verifyNotCommitted()) {
            doAppend(value, start, end);
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final boolean value) {
        if (verifyNotCommitted()) {
            doAppend(value);
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final int value) {
        if (verifyNotCommitted()) {
            doAppend(value);
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final long value) {
        if (verifyNotCommitted()) {
            doAppend(value);
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final double value) {
        if (verifyNotCommitted()) {
            doAppend(value);
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final double value, final int precision) {
        if (verifyNotCommitted()) {
            doAppend(value, precision);
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final Loggable object) {
        if (verifyNotCommitted()) {
            doAppend(object);
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final Object object) {
        if (verifyNotCommitted()) {
            doAppend(object);
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final Throwable exception) {
        if (verifyNotCommitted()) {
            doAppend(exception);
        }

        return this;
    }

    @Override
    public LogLocalEntry appendDecimal64(final long decimal) {
        if (verifyNotCommitted()) {
            doAppendDecimal64(decimal);
        }

        return this;
    }

    @Override
    public LogLocalEntry appendTimestamp(final long timestamp) {
        if (verifyNotCommitted()) {
            doAppendTimestamp(timestamp);
        }

        return this;
    }

    @Override
    public LogEntry appendTimestampNs(long timestampNs) {
        if (verifyNotCommitted()) {
            doAppendTimestampNs(timestampNs);
        }

        return this;
    }

    @Override
    public LogLocalEntry appendDate(final long timestamp) {
        if (verifyNotCommitted()) {
            doAppendDate(timestamp);
        }

        return this;
    }

    @Override
    public LogEntry appendDateNs(long timestampNs) {
        if (verifyNotCommitted()) {
            doAppendDateNs(timestampNs);
        }

        return this;
    }

    @Override
    public LogLocalEntry appendTime(final long timestamp) {
        if (verifyNotCommitted()) {
            doAppendTime(timestamp);
        }

        return this;
    }

    @Override
    public LogEntry appendTimeNs(long timestampNs) {
        if (verifyNotCommitted()) {
            doAppendTimeNs(timestampNs);
        }

        return this;
    }

    @Override
    public LogLocalEntry appendAlphanumeric(final long alphanumeric) {
        if (verifyNotCommitted()) {
            doAppendAlphanumeric(alphanumeric);
        }

        return this;
    }

    private void doAppend(final char value) {
        try {
            entry.append(value);
        } catch (final Throwable e) {
            warnAppendError(e);
        }
    }

    private void doAppend(final CharSequence value) {
        try {
            entry.append(value);
        } catch (final Throwable e) {
            warnAppendError(e);
        }
    }

    private void doAppend(final CharSequence value, final int start, final int end) {
        try {
            entry.append(value, start, end);
        } catch (final Throwable e) {
            warnAppendError(e);
        }
    }

    private void doAppend(final String value) {
        try {
            entry.append(value);
        } catch (final Throwable e) {
            warnAppendError(e);
        }
    }

    private void doAppend(final String value, final int start, final int end) {
        try {
            entry.append(value, start, end);
        } catch (final Throwable e) {
            warnAppendError(e);
        }
    }

    private void doAppend(final boolean value) {
        try {
            entry.append(value);
        } catch (final Throwable e) {
            warnAppendError(e);
        }
    }

    private void doAppend(final int value) {
        try {
            entry.append(value);
        } catch (final Throwable e) {
            warnAppendError(e);
        }
    }

    private void doAppend(final long value) {
        try {
            entry.append(value);
        } catch (final Throwable e) {
            warnAppendError(e);
        }
    }

    private void doAppend(final double value) {
        try {
            entry.append(value);
        } catch (final Throwable e) {
            warnAppendError(e);
        }
    }

    private void doAppend(final double value, final int precision) {
        try {
            entry.append(value, precision);
        } catch (final Throwable e) {
            warnAppendError(e);
        }
    }

    private void doAppend(final Loggable object) {
        try {
            entry.append(object);
        } catch (final Throwable e) {
            warnAppendError(e);
        }
    }

    private void doAppend(final Object object) {
        try {
            entry.append(object);
        } catch (final Throwable e) {
            warnAppendError(e);
        }
    }

    private void doAppend(final Throwable throwable) {
        try {
            if (!exceptional || throwable == null || exception != null) {
                entry.append(throwable);
            } else {
                exception = throwable;
                exceptionPosition = entry.length() - offset;
            }
        } catch (final Throwable e) {
            warnAppendError(e);
        }
    }

    private void doAppendDecimal64(final long decimal) {
        try {
            entry.appendDecimal64(decimal);
        } catch (final Throwable e) {
            warnAppendError(e);
        }
    }

    private void doAppendTimestamp(final long timestamp) {
        try {
            entry.appendTimestamp(timestamp);
        } catch (final Throwable e) {
            warnAppendError(e);
        }
    }

    private void doAppendTimestampNs(final long timestampNs) {
        try {
            entry.appendTimestampNs(timestampNs);
        } catch (final Throwable e) {
            warnAppendError(e);
        }
    }

    private void doAppendDate(final long timestamp) {
        try {
            entry.appendDate(timestamp);
        } catch (final Throwable e) {
            warnAppendError(e);
        }
    }

    private void doAppendDateNs(final long timestampNs) {
        try {
            entry.appendDateNs(timestampNs);
        } catch (final Throwable e) {
            warnAppendError(e);
        }
    }

    private void doAppendTime(final long timestamp) {
        try {
            entry.appendTime(timestamp);
        } catch (final Throwable e) {
            warnAppendError(e);
        }
    }

    private void doAppendTimeNs(final long timestampNs) {
        try {
            entry.appendTimeNs(timestampNs);
        } catch (final Throwable e) {
            warnAppendError(e);
        }
    }

    private void doAppendAlphanumeric(final long alphanumeric) {
        try {
            entry.appendAlphanumeric(alphanumeric);
        } catch (final Throwable e) {
            warnAppendError(e);
        }
    }

    // endregion

    // region AppendLast

    @Override
    public void appendLast(final char value) {
        if (verifyNotCommitted()) {
            doAppend(value);
            doCommit();
        }
    }

    @Override
    public void appendLast(final CharSequence value) {
        if (verifyNotCommitted()) {
            doAppend(value);
            doCommit();
        }
    }

    @Override
    public void appendLast(final CharSequence value, final int start, final int end) {
        if (verifyNotCommitted()) {
            doAppend(value, start, end);
            doCommit();
        }
    }

    @Override
    public void appendLast(final String value) {
        if (verifyNotCommitted()) {
            doAppend(value);
            doCommit();
        }
    }

    @Override
    public void appendLast(final String value, final int start, final int end) {
        if (verifyNotCommitted()) {
            doAppend(value, start, end);
            doCommit();
        }
    }

    @Override
    public void appendLast(final boolean value) {
        if (verifyNotCommitted()) {
            doAppend(value);
            doCommit();
        }
    }

    @Override
    public void appendLast(final int value) {
        if (verifyNotCommitted()) {
            doAppend(value);
            doCommit();
        }
    }

    @Override
    public void appendLast(final long value) {
        if (verifyNotCommitted()) {
            doAppend(value);
            doCommit();
        }
    }

    @Override
    public void appendLast(final double value) {
        if (verifyNotCommitted()) {
            doAppend(value);
            doCommit();
        }
    }

    @Override
    public void appendLast(final double value, final int precision) {
        if (verifyNotCommitted()) {
            doAppend(value, precision);
            doCommit();
        }
    }

    @Override
    public void appendLast(final Loggable object) {
        if (verifyNotCommitted()) {
            doAppend(object);
            doCommit();
        }
    }

    @Override
    public void appendLast(final Object object) {
        if (verifyNotCommitted()) {
            doAppend(object);
            doCommit();
        }
    }

    @Override
    public void appendLast(final Throwable exception) {
        if (verifyNotCommitted()) {
            doAppend(exception);
            doCommit();
        }
    }

    @Override
    public void appendDecimal64Last(final long decimal) {
        if (verifyNotCommitted()) {
            doAppendDecimal64(decimal);
            doCommit();
        }
    }

    @Override
    public void appendTimestampLast(final long timestamp) {
        if (verifyNotCommitted()) {
            doAppendTimestamp(timestamp);
            doCommit();
        }
    }

    @Override
    public void appendTimestampNsLast(long timestampNs) {
        if (verifyNotCommitted()) {
            doAppendTimestampNs(timestampNs);
            doCommit();
        }
    }

    @Override
    public void appendDateLast(final long timestamp) {
        if (verifyNotCommitted()) {
            doAppendDate(timestamp);
            doCommit();
        }
    }

    @Override
    public void appendDateNsLast(long timestampNs) {
        if (verifyNotCommitted()) {
            doAppendDateNs(timestampNs);
            doCommit();
        }
    }

    @Override
    public void appendTimeLast(final long timestamp) {
        if (verifyNotCommitted()) {
            doAppendTime(timestamp);
            doCommit();
        }
    }

    @Override
    public void appendTimeNsLast(long timestampNs) {
        if (verifyNotCommitted()) {
            doAppendTimeNs(timestampNs);
            doCommit();
        }
    }

    @Override
    public void appendAlphanumericLast(final long alphanumeric) {
        if (verifyNotCommitted()) {
            doAppendAlphanumeric(alphanumeric);
            doCommit();
        }
    }

    // endregion

    // region With

    @Override
    public LogLocalEntry with(final char value) {
        if (verifyNotCommitted()) {
            doAppend(value);

            if (doAppendTemplate()) {
                doCommit();
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry with(final CharSequence value) {
        if (verifyNotCommitted()) {
            doAppend(value);

            if (doAppendTemplate()) {
                doCommit();
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry with(final CharSequence value, final int start, final int end) {
        if (verifyNotCommitted()) {
            doAppend(value, start, end);

            if (doAppendTemplate()) {
                doCommit();
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry with(final String value) {
        if (verifyNotCommitted()) {
            doAppend(value);

            if (doAppendTemplate()) {
                doCommit();
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry with(final String value, final int start, final int end) {
        if (verifyNotCommitted()) {
            doAppend(value, start, end);

            if (doAppendTemplate()) {
                doCommit();
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry with(final boolean value) {
        if (verifyNotCommitted()) {
            doAppend(value);

            if (doAppendTemplate()) {
                doCommit();
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry with(final int value) {
        if (verifyNotCommitted()) {
            doAppend(value);

            if (doAppendTemplate()) {
                doCommit();
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry with(final long value) {
        if (verifyNotCommitted()) {
            doAppend(value);

            if (doAppendTemplate()) {
                doCommit();
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry with(final double value) {
        if (verifyNotCommitted()) {
            doAppend(value);

            if (doAppendTemplate()) {
                doCommit();
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry with(final double value, final int precision) {
        if (verifyNotCommitted()) {
            doAppend(value, precision);

            if (doAppendTemplate()) {
                doCommit();
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry with(final Loggable object) {
        if (verifyNotCommitted()) {
            doAppend(object);

            if (doAppendTemplate()) {
                doCommit();
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry with(final Object object) {
        if (verifyNotCommitted()) {
            doAppend(object);

            if (doAppendTemplate()) {
                doCommit();
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry with(final Throwable exception) {
        if (verifyNotCommitted()) {
            doAppend(exception);

            if (doAppendTemplate()) {
                doCommit();
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry withDecimal64(final long decimal) {
        if (verifyNotCommitted()) {
            doAppendDecimal64(decimal);

            if (doAppendTemplate()) {
                doCommit();
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry withTimestamp(final long timestamp) {
        if (verifyNotCommitted()) {
            doAppendTimestamp(timestamp);

            if (doAppendTemplate()) {
                doCommit();
            }
        }

        return this;
    }

    @Override
    public LogEntryTemplate withTimestampNs(long timestampNs) {
        if (verifyNotCommitted()) {
            doAppendTimestampNs(timestampNs);

            if (doAppendTemplate()) {
                doCommit();
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry withDate(final long timestamp) {
        if (verifyNotCommitted()) {
            doAppendDate(timestamp);

            if (doAppendTemplate()) {
                doCommit();
            }
        }

        return this;
    }

    @Override
    public LogEntryTemplate withDateNs(long timestampNs) {
        if (verifyNotCommitted()) {
            doAppendDateNs(timestampNs);

            if (doAppendTemplate()) {
                doCommit();
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry withTime(final long timestamp) {
        if (verifyNotCommitted()) {
            doAppendTime(timestamp);

            if (doAppendTemplate()) {
                doCommit();
            }
        }

        return this;
    }

    @Override
    public LogEntryTemplate withTimeNs(long timestampNs) {
        if (verifyNotCommitted()) {
            doAppendTimeNs(timestampNs);

            if (doAppendTemplate()) {
                doCommit();
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry withAlphanumeric(final long alphanumeric) {
        if (verifyNotCommitted()) {
            doAppendAlphanumeric(alphanumeric);

            if (doAppendTemplate()) {
                doCommit();
            }
        }

        return this;
    }

    private boolean doAppendTemplate() {
        final int length = template.length();
        final int index = findPlaceholder(template, templateIndex, length);

        entry.append(template, templateIndex, index);
        templateIndex = index + 2;

        return index == length;
    }

    // endregion With

    @Override
    public void abort() {
        if (verifyNotCommitted()) {
            committed = true;
            exception = null;
        }
    }

    @Override
    public void commit() {
        if (verifyNotCommitted()) {
            doCommit();
        }
    }

    void onClaim(final int logName, final int logLevel, final long appenderMask) {
        reuse();

        final byte[] array = entry.array();
        Util.UNSAFE.putInt(array, LOG_NAME_OFFSET, logName);
        Util.UNSAFE.putLong(array, APPENDER_MASK_OFFSET, appenderMask);
        Util.UNSAFE.putByte(array, LOG_LEVEL_OFFSET, (byte) logLevel);
    }

    void onClaim(final int logName, final int logLevel, final long appenderMask, final String template) {
        onClaim(logName, logLevel, appenderMask);

        this.template = template;
        this.templateIndex = 0;

        if (doAppendTemplate()) {
            doCommit();
        }
    }

    void onCommit(final long timestamp) {
        Util.UNSAFE.putLong(entry.array(), TIMESTAMP_OFFSET, timestamp);
    }

    void copyTo(final long address) {
        Util.UNSAFE.copyMemory(
                entry.array(),
                LENGTH_OFFSET,
                null,
                address,
                Util.align(length(), Util.SIZE_OF_LONG)
        );
    }

    void wrapTo(final MutableBuffer wrapper) {
        final int offset = (int) (LENGTH_OFFSET - Util.ARRAY_BYTE_BASE_OFFSET);
        wrapper.wrap(entry.array(), offset, entry.length() - offset);
    }

    private void reuse() {
        if (!committed) {
            warnNotCommitted();
            doCommit();
        }

        entry.reset(offset);
        committed = false;
    }

    private void doCommit() {
        final Throwable throwable = exception;

        if (throwable == null) {
            service.commit(this);
        } else {
            exception = null;
            service.commit(this, throwable, exceptionPosition);
        }

        committed = true;
    }

    private boolean verifyNotCommitted() {
        if (committed) {
            warnCommitted();
            return false;
        }

        return true;
    }

    private void warnNotCommitted() {
        if (LogDebug.isWarnEnabled()) {
            final String message = entry.substring(offset, entry.length());
            final IllegalStateException e = new IllegalStateException("log entry was not committed. Message: " + message);
            LogDebug.warn(e);
        }
    }

    private static void warnCommitted() {
        if (LogDebug.isWarnEnabled()) {
            final IllegalStateException e = new IllegalStateException("log entry is committed");
            LogDebug.warn(e);
        }
    }

    private static void warnAppendError(final Throwable e) {
        if (LogDebug.isWarnEnabled()) {
            LogDebug.warn("append error: " + e.getMessage(), e);
        }
    }

    private static LogLimitedEntry createEntry(final Thread thread,
                                               final String truncationSuffix,
                                               final int initialCapacity,
                                               final int maxCapacity,
                                               final boolean utf8) {

        final String threadName = thread.getName();
        final byte threadNameLength = (byte) Util.limitUtf8Index(threadName, 0, threadName.length(), Byte.MAX_VALUE);
        final int offset = (int) (THREAD_NAME_DATA_OFFSET + threadNameLength - Util.ARRAY_BYTE_BASE_OFFSET);

        final LogLimitedEntry entry = utf8 ?
                new LogUtf8Entry(truncationSuffix, initialCapacity + offset, maxCapacity + offset) :
                new LogAsciiEntry(truncationSuffix, initialCapacity + offset, maxCapacity + offset);

        Util.UNSAFE.putInt(entry.array, LENGTH_OFFSET, 0);
        Util.UNSAFE.putByte(entry.array, THREAD_NAME_LENGTH_OFFSET, threadNameLength);

        Formatting.formatUtf8String(threadName, 0, threadNameLength, entry.array, (int) (THREAD_NAME_DATA_OFFSET - Util.ARRAY_BYTE_BASE_OFFSET));
        entry.reset(offset);

        return entry;
    }

    private static int findPlaceholder(final String template, final int start, final int end) {
        for (int index = start, limit = end - 1; index < limit; index++) {
            if (template.charAt(index) == '%' && template.charAt(index + 1) == 's') {
                return index;
            }
        }

        return end;
    }

}
