package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.core.Logger;
import com.epam.deltix.gflog.core.appender.Appender;
import com.epam.deltix.gflog.core.clock.Clock;
import com.epam.deltix.gflog.core.clock.ClockFactory;
import com.epam.deltix.gflog.core.util.PropertyUtil;

import java.util.Collection;


public abstract class LogServiceFactory {

    protected static final String ENCODING_ASCII = "ASCII";
    protected static final String ENCODING_UTF_8 = "UTF-8";

    protected static final String ENTRY_TRUNCATION_SUFFIX = PropertyUtil.getString("gflog.entry.truncation.suffix", ">>");
    protected static final int ENTRY_TRUNCATION_SUFFIX_LIMIT = 256;

    protected static final String ENTRY_ENCODING = PropertyUtil.getString("gflog.entry.encoding", ENCODING_ASCII);

    protected static final int ENTRY_INITIAL_CAPACITY = PropertyUtil.getMemory("gflog.entry.initial.capacity", 4 * 1024);
    protected static final int ENTRY_MAX_CAPACITY = PropertyUtil.getMemory("gflog.entry.max.capacity", 64 * 1024);
    protected static final int ENTRY_CAPACITY_LIMIT = 512 * 1024 * 1024;

    protected Clock clock;
    protected String entryTruncationSuffix;
    protected String entryEncoding;
    protected int entryInitialCapacity;
    protected int entryMaxCapacity;

    public void setClock(final Clock clock) {
        this.clock = clock;
    }

    public Clock getClock() {
        return clock;
    }

    public void setEntryTruncationSuffix(final String entryTruncationSuffix) {
        this.entryTruncationSuffix = entryTruncationSuffix;
    }

    public String getEntryTruncationSuffix() {
        return entryTruncationSuffix;
    }

    public void setEntryEncoding(final String entryEncoding) {
        this.entryEncoding = entryEncoding;
    }

    public String getEntryEncoding() {
        return entryEncoding;
    }

    public void setEntryInitialCapacity(final int entryInitialCapacity) {
        this.entryInitialCapacity = entryInitialCapacity;
    }

    public int getEntryInitialCapacity() {
        return entryInitialCapacity;
    }

    public void setEntryMaxCapacity(final int entryMaxCapacity) {
        this.entryMaxCapacity = entryMaxCapacity;
    }

    public int getEntryMaxCapacity() {
        return entryMaxCapacity;
    }

    public LogService create(final Collection<Logger> loggers, final Collection<Appender> appenders) {
        conclude();

        final Logger[] loggersArray = loggers.toArray(new Logger[0]);
        final Appender[] appendersArray = appenders.toArray(new Appender[0]);
        final boolean entryUtf8 = ENCODING_UTF_8.equalsIgnoreCase(entryEncoding);

        return createService(
                loggersArray,
                appendersArray,
                clock,
                entryTruncationSuffix,
                entryInitialCapacity,
                entryMaxCapacity,
                entryUtf8
        );
    }

    protected void conclude() {
        if (clock == null) {
            clock = new ClockFactory().create();
        }

        if (entryTruncationSuffix == null) {
            entryTruncationSuffix = ENTRY_TRUNCATION_SUFFIX;
        }

        verifyTruncationSuffix();

        if (entryEncoding == null) {
            entryEncoding = ENTRY_ENCODING;
        }

        verifyEntryEncoding();

        if (entryInitialCapacity <= 0) {
            entryInitialCapacity = ENTRY_INITIAL_CAPACITY;
        }

        if (entryMaxCapacity <= 0) {
            entryMaxCapacity = ENTRY_MAX_CAPACITY;
        }

        entryMaxCapacity = Math.min(entryMaxCapacity, ENTRY_CAPACITY_LIMIT);
    }

    protected abstract LogService createService(final Logger[] loggers,
                                                final Appender[] appenders,
                                                final Clock clock,
                                                final String entryTruncationSuffix,
                                                final int entryInitialCapacity,
                                                final int entryMaxCapacity,
                                                final boolean entryUtf8);

    protected void verifyTruncationSuffix() {
        final int length = entryTruncationSuffix.length();
        if (length > ENTRY_TRUNCATION_SUFFIX_LIMIT) {
            throw new IllegalArgumentException("truncation suffix is too long: " + length + ". Max size: " + ENTRY_TRUNCATION_SUFFIX_LIMIT);
        }

        for (int i = 0; i < length; i++) {
            if (entryTruncationSuffix.charAt(i) > 0x7F) {
                throw new IllegalArgumentException("truncation suffix must be an ascii encoded string: " + entryTruncationSuffix);
            }
        }
    }

    protected void verifyEntryEncoding() {
        if (!ENCODING_UTF_8.equalsIgnoreCase(entryEncoding) && !ENCODING_ASCII.equalsIgnoreCase(entryEncoding)) {
            throw new IllegalArgumentException("unsupported entry encoding: " + entryEncoding +
                    ". Supported values: " + ENCODING_UTF_8 + " or " + ENCODING_ASCII);
        }
    }

}
