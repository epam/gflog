package com.epam.deltix.gflog.core.appender;

import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.LogRecord;
import com.epam.deltix.gflog.core.LogRecordBean;
import com.epam.deltix.gflog.core.util.Formatting;
import com.epam.deltix.gflog.core.util.MutableBuffer;
import com.epam.deltix.gflog.core.util.UnsafeBuffer;
import com.epam.deltix.gflog.core.util.Util;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;


public class SafeAppender extends CompositeAppender {

    private final Filter filter;

    private final LogRecordBean warnRecord;
    private final MutableBuffer warnMessage;

    private final MutableBuffer warnBuffer;
    private final int warnBufferOffset;

    private int skipped;

    protected SafeAppender(final String name, final LogLevel level, final int maxEntriesPerSecond, final Appender[] appenders) {
        super(name, level, appenders);

        final String messagePrefix = ("Max entries per second: " + maxEntriesPerSecond + ". Skipped log entries: ");
        final String logName = SafeAppender.class.getName();
        final String threadName = "gflog";

        filter = new Filter(maxEntriesPerSecond);

        warnMessage = new UnsafeBuffer();
        warnBuffer = UnsafeBuffer.allocateHeap(256);
        warnBufferOffset = Formatting.formatAsciiCharSequence(messagePrefix, warnBuffer, 0);

        warnRecord = new LogRecordBean();
        warnRecord.setLogName(Util.fromUtf8String(logName));
        warnRecord.setLogLevel(LogLevel.WARN);
        warnRecord.setThreadName(Util.fromUtf8String(threadName));
        warnRecord.setMessage(warnMessage);
    }

    @Override
    public int append(final LogRecord record) {
        final boolean accept = filter.accept(record);

        if (accept) {
            if (skipped != 0) {
                warnAboutSkippedRecords(record);
                skipped = 0;
            }

            return super.append(record);
        }

        skipped++;
        return 0;
    }

    protected void warnAboutSkippedRecords(final LogRecord newRecord) {
        final int offset = Formatting.formatUInt(skipped, warnBuffer, warnBufferOffset);

        warnMessage.wrap(warnBuffer, 0, offset);
        warnRecord.setTimestamp(newRecord.getTimestamp());

        super.append(warnRecord);
    }

    protected static class Filter {

        protected static final long SECOND = TimeUnit.SECONDS.toNanos(1);

        protected final long[] timestamps;

        protected int index;

        public Filter(final int maxEntriesPerSecond) {
            timestamps = new long[maxEntriesPerSecond];
            Arrays.fill(timestamps, Long.MIN_VALUE);
        }

        public boolean accept(final LogRecord record) {
            final long timestamp = record.getTimestamp();
            final boolean accept = (timestamp - SECOND >= timestamps[index]);

            if (accept) {
                timestamps[index++] = timestamp;

                if (index == timestamps.length) {
                    index = 0;
                }
            }

            return accept;
        }

    }

}
