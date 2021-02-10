package com.epam.deltix.gflog.core.appender;

import com.epam.deltix.gflog.api.LogDebug;
import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.LogRecord;

import java.util.Arrays;


public final class ObservableAppender extends Appender {

    public static final ObservableAppender INSTANCE = new ObservableAppender();

    private volatile AppenderListener[] listeners = new AppenderListener[0];

    private ObservableAppender() {
        super("observable", LogLevel.TRACE);
    }

    @Override
    public void open() {
        //skip
    }

    @Override
    public void close() {
        //skip
    }

    @Override
    public int append(final LogRecord record) {
        int workDone = 0;

        for (final AppenderListener listener : listeners) {
            try {
                workDone += listener.onLogRecord(record);
            } catch (final Throwable e) {
                LogDebug.warn("error while process log record by appender listener", e);
            }
        }

        return workDone;
    }

    @Override
    public int flush() {
        int workDone = 0;

        for (final AppenderListener listener : listeners) {
            try {
                workDone += listener.onFlush();
            } catch (final Throwable e) {
                LogDebug.warn("error while flushing appender listener", e);
            }
        }

        return workDone;
    }

    public synchronized void addListener(final AppenderListener listener) {
        final AppenderListener[] listeners = this.listeners;
        final int length = listeners.length;

        final AppenderListener[] newListeners = Arrays.copyOf(listeners, length + 1);
        newListeners[length] = listener;

        this.listeners = newListeners;
    }

    public synchronized void removeListener(final AppenderListener listener) {
        final AppenderListener[] listeners = this.listeners;
        final int length = listeners.length;
        int index = -1;

        for (int i = 0; i < length; i++) {
            if (listeners[i] == listener) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            final AppenderListener[] newListeners = new AppenderListener[length - 1];
            System.arraycopy(listeners, 0, newListeners, 0, index);
            System.arraycopy(listeners, index + 1, newListeners, index, length - index - 1);
            this.listeners = newListeners;
        }
    }

}
