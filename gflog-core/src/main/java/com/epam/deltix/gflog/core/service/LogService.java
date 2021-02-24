package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.LogInfo;
import com.epam.deltix.gflog.core.Logger;
import com.epam.deltix.gflog.core.appender.Appender;
import com.epam.deltix.gflog.core.clock.Clock;

import static java.util.Arrays.sort;
import static java.util.Comparator.comparing;


public abstract class LogService implements AutoCloseable {

    protected static final LogInfo NO_LOG_INFO = new LogInfo("", LogLevel.FATAL, new long[LogLevel.values().length]);

    protected final LogIndex logIndex = new LogIndex();

    protected final String entryTruncationSuffix;
    protected final int entryInitialCapacity;
    protected final int entryMaxCapacity;
    protected final boolean entryUtf8;

    protected final ThreadLocal<LogLocalEntry> logEntry;
    protected final Clock clock;

    protected final Appender[] appenders;
    protected final LogInfo[] logs;

    protected volatile boolean closed;

    public LogService(final Logger[] loggers,
                      final Appender[] appenders,
                      final Clock clock,
                      final String entryTruncationSuffix,
                      final int entryInitialCapacity,
                      final int entryMaxCapacity,
                      final boolean entryUtf8) {
        this.entryTruncationSuffix = entryTruncationSuffix;
        this.entryInitialCapacity = entryInitialCapacity;
        this.entryMaxCapacity = entryMaxCapacity;
        this.entryUtf8 = entryUtf8;
        this.logEntry = ThreadLocal.withInitial(this::newLogLocalEntry);
        this.clock = clock;
        this.logs = createLogInfos(loggers, appenders);
        this.appenders = appenders;
    }

    private LogLocalEntry newLogLocalEntry() {
        return new LogLocalEntry(
                this,
                Thread.currentThread(),
                entryTruncationSuffix,
                entryInitialCapacity,
                entryMaxCapacity,
                entryUtf8
        );
    }

    public abstract void open();

    @Override
    public abstract void close();

    public LogLocalEntry claim(final int logName, final int logLevel, final long appenderMask) {
        final LogLocalEntry entry = logEntry.get();
        entry.onClaim(logName, logLevel, appenderMask);
        return entry;
    }

    public LogLocalEntry claim(final int logName, final int logLevel, final long appenderMask, final String template) {
        final LogLocalEntry entry = logEntry.get();
        entry.onClaim(logName, logLevel, appenderMask, template);
        return entry;
    }

    public abstract void commit(final LogLocalEntry entry);

    public LogInfo register(final String logName, final int index) {
        logIndex.put(logName, index);

        LogInfo info = NO_LOG_INFO;

        for (int i = logs.length - 1; i >= 0; i--) {
            final LogInfo log = logs[i];
            if (logName.startsWith(log.getName())) {
                info = log;
                break;
            }
        }

        return info;
    }

    private static LogInfo[] createLogInfos(final Logger[] loggers, final Appender[] appenders) {
        final LogInfo[] infos = new LogInfo[loggers.length];

        for (int i = 0; i < infos.length; i++) {
            final Logger logger = loggers[i];

            final String name = logger.getName();
            final LogLevel level = logger.getLevel();
            final long[] appenderMask = buildAppenderMask(logger, appenders);

            infos[i] = new LogInfo(name, level, appenderMask);
        }

        sort(infos, comparing(LogInfo::getName));
        return infos;
    }

    private static long[] buildAppenderMask(final Logger logger, final Appender[] appenders) {
        final long[] mask = new long[LogLevel.values().length];

        search:
        for (final Appender appender : logger.getAppenders()) {
            for (int index = 0; index < appenders.length; index++) {
                if (appender != appenders[index]) {
                    continue;
                }

                for (int level = appender.getLevel().ordinal(); level < mask.length; level++) {
                    mask[level] |= (1L << index);
                }

                continue search;
            }

            throw new IllegalArgumentException("can't find appender \"" + appender.getName() + "\" for logger \"" + logger.getName() + "\"");
        }

        return mask;
    }

}
