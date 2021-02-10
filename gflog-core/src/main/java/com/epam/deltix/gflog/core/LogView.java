package com.epam.deltix.gflog.core;

import com.epam.deltix.gflog.api.*;
import com.epam.deltix.gflog.core.service.LogService;

import java.util.Objects;


final class LogView implements Log {

    private final String name;
    private final int index;

    private long[] appenderMask;

    private volatile int level = LogLevel.FATAL.ordinal();
    private volatile LogService service;

    LogView(final String name, final int index) {
        this.name = name;
        this.index = index;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public LogLevel getLevel() {
        return LogLevel.valueOf(level);
    }

    @Override
    public void setLevel(final LogLevel level) {
        this.level = Objects.requireNonNull(level).ordinal();
    }

    @Override
    public boolean isTraceEnabled() {
        return (this.level <= 0) && (this.service != null) && (this.appenderMask[0] != 0);
    }

    @Override
    public boolean isDebugEnabled() {
        return (this.level <= 1) && (this.service != null) && (this.appenderMask[1] != 0);
    }

    @Override
    public boolean isInfoEnabled() {
        return (this.level <= 2) && (this.service != null) && (this.appenderMask[2] != 0);
    }

    @Override
    public boolean isWarnEnabled() {
        return (this.level <= 3) && (this.service != null) && (this.appenderMask[3] != 0);
    }

    @Override
    public boolean isErrorEnabled() {
        return (this.level <= 4) && (this.service != null) && (this.appenderMask[4] != 0);
    }

    @Override
    public boolean isFatalEnabled() {
        return (this.level <= 5) && (this.service != null) && (this.appenderMask[5] != 0);
    }

    @Override
    public boolean isEnabled(final LogLevel logLevel) {
        final int level = logLevel.ordinal();
        return (this.level <= level) && (this.service != null) && (this.appenderMask[level] != 0);
    }

    @Override
    public LogEntry trace() {
        if (this.level <= 0) {
            final LogService service = this.service;
            final long appenderMask = this.appenderMask[0];

            if (service != null && appenderMask != 0) {
                return service.claim(index, 0, appenderMask);
            }
        }

        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public LogEntryTemplate trace(final String template) {
        if (this.level <= 0) {
            final LogService service = this.service;
            final long appenderMask = this.appenderMask[0];

            if (service != null && appenderMask != 0) {
                return service.claim(index, 0, appenderMask, template);
            }
        }

        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public LogEntry debug() {
        if (this.level <= 1) {
            final LogService service = this.service;
            final long appenderMask = this.appenderMask[1];

            if (service != null && appenderMask != 0) {
                return service.claim(index, 1, appenderMask);
            }
        }

        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public LogEntryTemplate debug(final String template) {
        if (this.level <= 1) {
            final LogService service = this.service;
            final long appenderMask = this.appenderMask[1];

            if (service != null && appenderMask != 0) {
                return service.claim(index, 1, appenderMask, template);
            }
        }

        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public LogEntry info() {
        if (this.level <= 2) {
            final LogService service = this.service;
            final long appenderMask = this.appenderMask[2];

            if (service != null && appenderMask != 0) {
                return service.claim(index, 2, appenderMask);
            }
        }

        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public LogEntryTemplate info(final String template) {
        if (this.level <= 2) {
            final LogService service = this.service;
            final long appenderMask = this.appenderMask[2];

            if (service != null && appenderMask != 0) {
                return service.claim(index, 2, appenderMask, template);
            }
        }

        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public LogEntry warn() {
        if (this.level <= 3) {
            final LogService service = this.service;
            final long appenderMask = this.appenderMask[3];

            if (service != null && appenderMask != 0) {
                return service.claim(index, 3, appenderMask);
            }
        }

        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public LogEntryTemplate warn(final String template) {
        if (this.level <= 3) {
            final LogService service = this.service;
            final long appenderMask = this.appenderMask[3];

            if (service != null && appenderMask != 0) {
                return service.claim(index, 3, appenderMask, template);
            }
        }

        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public LogEntry error() {
        if (this.level <= 4) {
            final LogService service = this.service;
            final long appenderMask = this.appenderMask[4];

            if (service != null && appenderMask != 0) {
                return service.claim(index, 4, appenderMask);
            }
        }

        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public LogEntryTemplate error(final String template) {
        if (this.level <= 4) {
            final LogService service = this.service;
            final long appenderMask = this.appenderMask[4];

            if (service != null && appenderMask != 0) {
                return service.claim(index, 4, appenderMask, template);
            }
        }

        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public LogEntry fatal() {
        if (this.level <= 5) {
            final LogService service = this.service;
            final long appenderMask = this.appenderMask[5];

            if (service != null && appenderMask != 0) {
                return service.claim(index, 5, appenderMask);
            }
        }

        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public LogEntryTemplate fatal(final String template) {
        if (this.level <= 5) {
            final LogService service = this.service;
            final long appenderMask = this.appenderMask[5];

            if (service != null && appenderMask != 0) {
                return service.claim(index, 5, appenderMask, template);
            }
        }

        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public LogEntry log(final LogLevel logLevel) {
        final int level = logLevel.ordinal();

        if (this.level <= level) {
            final LogService service = this.service;
            final long appenderMask = this.appenderMask[level];

            if (service != null && appenderMask != 0) {
                return service.claim(index, level, appenderMask);
            }
        }

        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public LogEntryTemplate log(final LogLevel logLevel, final String template) {
        final int level = logLevel.ordinal();

        if (this.level <= level) {
            final LogService service = this.service;
            final long appenderMask = this.appenderMask[level];

            if (service != null && appenderMask != 0) {
                return service.claim(index, level, appenderMask, template);
            }
        }

        return NoOpLogEntry.INSTANCE;
    }

    void initialize(final LogService service) {
        final LogInfo info = service.register(name, index);

        this.appenderMask = info.getAppenderMask();
        this.level = info.getLevel().ordinal();
        this.service = service;
    }

    void invalidate() {
        this.level = LogLevel.FATAL.ordinal();
        this.service = null;
    }

}
