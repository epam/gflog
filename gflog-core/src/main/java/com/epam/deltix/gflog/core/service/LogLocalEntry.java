package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.api.LogDebug;
import com.epam.deltix.gflog.api.LogEntry;
import com.epam.deltix.gflog.api.LogEntryTemplate;
import com.epam.deltix.gflog.api.Loggable;
import com.epam.deltix.gflog.core.util.Formatting;
import com.epam.deltix.gflog.core.util.MutableBuffer;
import com.epam.deltix.gflog.core.util.Util;


final class LogLocalEntry implements LogEntry, LogEntryTemplate {

    private static final String PLACEHOLDER = "%s";

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

    private String template;
    private int templateIndex;

    private boolean committed = true;

    LogLocalEntry(final LogService service,
                  final Thread thread,
                  final String truncationSuffix,
                  final int initialCapacity,
                  final int maxCapacity,
                  final boolean utf8) {

        this.service = service;
        this.entry = createEntry(thread, truncationSuffix, initialCapacity, maxCapacity, utf8);
        this.offset = entry.length();
    }

    public int length() {
        return entry.length() - (int) (LENGTH_OFFSET - Util.ARRAY_BYTE_BASE_OFFSET);
    }

    // region Append

    @Override
    public LogLocalEntry append(final char value) {
        if (verifyNotCommitted()) {
            try {
                entry.append(value);
            } catch (final Throwable e) {
                warnAppendError(e);
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final CharSequence value) {
        if (verifyNotCommitted()) {
            try {
                entry.append(value);
            } catch (final Throwable e) {
                warnAppendError(e);
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final CharSequence value, final int start, final int end) {
        if (verifyNotCommitted()) {
            try {
                entry.append(value, start, end);
            } catch (final Throwable e) {
                warnAppendError(e);
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final String value) {
        if (verifyNotCommitted()) {
            try {
                entry.append(value);
            } catch (final Throwable e) {
                warnAppendError(e);
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final String value, final int start, final int end) {
        if (verifyNotCommitted()) {
            try {
                entry.append(value, start, end);
            } catch (final Throwable e) {
                warnAppendError(e);
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final boolean value) {
        if (verifyNotCommitted()) {
            try {
                entry.append(value);
            } catch (final Throwable e) {
                warnAppendError(e);
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final int value) {
        if (verifyNotCommitted()) {
            try {
                entry.append(value);
            } catch (final Throwable e) {
                warnAppendError(e);
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final long value) {
        if (verifyNotCommitted()) {
            try {
                entry.append(value);
            } catch (final Throwable e) {
                warnAppendError(e);
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final double value) {
        if (verifyNotCommitted()) {
            try {
                entry.append(value);
            } catch (final Throwable e) {
                warnAppendError(e);
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final double value, final int precision) {
        if (verifyNotCommitted()) {
            try {
                entry.append(value, precision);
            } catch (final Throwable e) {
                warnAppendError(e);
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final Loggable object) {
        if (verifyNotCommitted()) {
            try {
                entry.append(object);
            } catch (final Throwable e) {
                warnAppendError(e);
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final Object object) {
        if (verifyNotCommitted()) {
            try {
                entry.append(object);
            } catch (final Throwable e) {
                warnAppendError(e);
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry append(final Throwable exception) {
        if (verifyNotCommitted()) {
            try {
                entry.append(exception);
            } catch (final Throwable e) {
                warnAppendError(e);
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry appendDecimal64(final long decimal) {
        if (verifyNotCommitted()) {
            try {
                entry.appendDecimal64(decimal);
            } catch (final Throwable e) {
                warnAppendError(e);
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry appendTimestamp(final long timestamp) {
        if (verifyNotCommitted()) {
            try {
                entry.appendTimestamp(timestamp);
            } catch (final Throwable e) {
                warnAppendError(e);
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry appendDate(final long timestamp) {
        if (verifyNotCommitted()) {
            try {
                entry.appendDate(timestamp);
            } catch (final Throwable e) {
                warnAppendError(e);
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry appendTime(final long timestamp) {
        if (verifyNotCommitted()) {
            try {
                entry.appendTime(timestamp);
            } catch (final Throwable e) {
                warnAppendError(e);
            }
        }

        return this;
    }

    @Override
    public LogLocalEntry appendAlphanumeric(final long alphanumeric) {
        if (verifyNotCommitted()) {
            try {
                entry.appendAlphanumeric(alphanumeric);
            } catch (final Throwable e) {
                warnAppendError(e);
            }
        }

        return this;
    }

    // endregion

    // region AppendLast

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void appendLast(final char value) {
        append(value);
        commit();
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void appendLast(final CharSequence value) {
        append(value);
        commit();
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void appendLast(final CharSequence value, final int start, final int end) {
        append(value, start, end);
        commit();
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void appendLast(final String value) {
        append(value);
        commit();
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void appendLast(final String value, final int start, final int end) {
        append(value, start, end);
        commit();
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void appendLast(final boolean value) {
        append(value);
        commit();
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void appendLast(final int value) {
        append(value);
        commit();
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void appendLast(final long value) {
        append(value);
        commit();
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void appendLast(final double value) {
        append(value);
        commit();
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void appendLast(final double value, final int precision) {
        append(value, precision);
        commit();
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void appendLast(final Loggable object) {
        append(object);
        commit();
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void appendLast(final Object object) {
        append(object);
        commit();
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void appendLast(final Throwable exception) {
        append(exception);
        commit();
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void appendDecimal64Last(final long decimal) {
        appendDecimal64(decimal);
        commit();
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void appendTimestampLast(final long timestamp) {
        appendTimestamp(timestamp);
        commit();
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void appendDateLast(final long timestamp) {
        appendDate(timestamp);
        commit();
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void appendTimeLast(final long timestamp) {
        appendTime(timestamp);
        commit();
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void appendAlphanumericLast(final long alphanumeric) {
        appendAlphanumeric(alphanumeric);
        commit();
    }

    // endregion

    // region With

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LogLocalEntry with(final char value) {
        if (verifyNotCommitted()) {
            append(value);
            appendTemplateChunk();
        }

        return this;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LogLocalEntry with(final CharSequence value) {
        if (verifyNotCommitted()) {
            append(value);
            appendTemplateChunk();
        }

        return this;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LogLocalEntry with(final CharSequence value, final int start, final int end) {
        if (verifyNotCommitted()) {
            append(value, start, end);
            appendTemplateChunk();
        }

        return this;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LogLocalEntry with(final String value) {
        if (verifyNotCommitted()) {
            append(value);
            appendTemplateChunk();
        }

        return this;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LogLocalEntry with(final String value, final int start, final int end) {
        if (verifyNotCommitted()) {
            append(value, start, end);
            appendTemplateChunk();
        }

        return this;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LogLocalEntry with(final boolean value) {
        if (verifyNotCommitted()) {
            append(value);
            appendTemplateChunk();
        }

        return this;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LogLocalEntry with(final int value) {
        if (verifyNotCommitted()) {
            append(value);
            appendTemplateChunk();
        }

        return this;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LogLocalEntry with(final long value) {
        if (verifyNotCommitted()) {
            append(value);
            appendTemplateChunk();
        }

        return this;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LogLocalEntry with(final double value) {
        if (verifyNotCommitted()) {
            append(value);
            appendTemplateChunk();
        }

        return this;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LogLocalEntry with(final double value, final int precision) {
        if (verifyNotCommitted()) {
            append(value, precision);
            appendTemplateChunk();
        }

        return this;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LogLocalEntry with(final Loggable object) {
        if (verifyNotCommitted()) {
            append(object);
            appendTemplateChunk();
        }

        return this;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LogLocalEntry with(final Object object) {
        if (verifyNotCommitted()) {
            append(object);
            appendTemplateChunk();
        }

        return this;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LogLocalEntry with(final Throwable exception) {
        if (verifyNotCommitted()) {
            append(exception);
            appendTemplateChunk();
        }

        return this;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LogLocalEntry withDecimal64(final long decimal) {
        if (verifyNotCommitted()) {
            appendDecimal64(decimal);
            appendTemplateChunk();
        }

        return this;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LogLocalEntry withTimestamp(final long timestamp) {
        if (verifyNotCommitted()) {
            appendTimestamp(timestamp);
            appendTemplateChunk();
        }

        return this;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LogLocalEntry withDate(final long timestamp) {
        if (verifyNotCommitted()) {
            appendDate(timestamp);
            appendTemplateChunk();
        }

        return this;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LogLocalEntry withTime(final long timestamp) {
        if (verifyNotCommitted()) {
            appendTime(timestamp);
            appendTemplateChunk();
        }

        return this;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LogLocalEntry withAlphanumeric(final long alphanumeric) {
        if (verifyNotCommitted()) {
            appendAlphanumeric(alphanumeric);
            appendTemplateChunk();
        }

        return this;
    }

    // endregion With

    @Override
    public void abort() {
        if (verifyNotCommitted()) {
            committed = true;
        }
    }

    @Override
    public void commit() {
        if (verifyNotCommitted()) {
            service.commit(this);
            committed = true;
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

        appendTemplateChunk();
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
            service.commit(this);
        }

        entry.reset(offset);
        committed = false;
    }

    private void appendTemplateChunk() {
        final int i = template.indexOf(PLACEHOLDER, templateIndex);

        if (i >= 0) {
            entry.append(template, templateIndex, i);
            templateIndex = i + 2;
        } else {
            entry.append(template, templateIndex, template.length());
            commit();
        }
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
            final IllegalStateException e = new IllegalStateException("log entry was not committed. Message: " + entry.substring(offset, entry.length()));
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
        final byte threadNameLength = (byte) Util.findUtf8Bound(threadName, 0, threadName.length(), Byte.MAX_VALUE);
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

}
