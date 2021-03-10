package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.api.LogDebug;
import com.epam.deltix.gflog.core.LogRecord;
import com.epam.deltix.gflog.core.appender.Appender;
import com.epam.deltix.gflog.core.util.Util;


class LogProcessor implements AutoCloseable {

    protected final Appender[] appenders;

    protected long enabled = ~0;

    LogProcessor(final Appender[] appenders) {
        this.appenders = appenders;
    }

    void open() {
        Throwable error = null;
        int index;

        for (index = 0; index < appenders.length; index++) {
            final Appender appender = appenders[index];

            try {
                appender.open();
            } catch (final Throwable e) {
                error = e;
                LogDebug.warn("appender: " + appender.getName() + " threw exception during opening", e);
                break;
            }
        }

        if (error != null) {
            for (int i = 0; i < index; i++) {
                final Appender appender = appenders[i];

                try {
                    appender.close();
                } catch (final Throwable e) {
                    LogDebug.warn("appender: " + appender.getName() + " threw exception during closing", e);
                }
            }

            Util.rethrow(error);
        }
    }

    @Override
    public void close() {
        for (final Appender appender : appenders) {
            try {
                appender.close();
            } catch (final Throwable e) {
                LogDebug.warn("appender: " + appender.getName() + " threw exception during closing", e);
            }
        }
    }

    final void process(final LogRecord record) {
        final Appender[] appenders = this.appenders;
        final long mask = record.getAppenderMask() & enabled;

        for (int i = 0; i < appenders.length; i++) {
            if (((mask >>> i) & 1) == 1) {
                final Appender appender = appenders[i];

                try {
                    appender.append(record);
                } catch (final Throwable e) {
                    enabled &= ~(1L << i);
                    LogDebug.warn("appender: " + appender.getName() + " threw exception during processing record", e);
                }
            }
        }
    }

    final int flush() {
        final Appender[] appenders = this.appenders;
        final long mask = enabled;

        int work = 0;

        for (int i = 0; i < appenders.length; i++) {
            if (((mask >>> i) & 1) == 1) {
                final Appender appender = appenders[i];

                try {
                    work += appender.flush();
                } catch (final Throwable e) {
                    enabled &= ~(1L << i);
                    LogDebug.warn("appender: " + appender.getName() + " threw exception during flush", e);
                }
            }
        }

        return work;
    }

}
