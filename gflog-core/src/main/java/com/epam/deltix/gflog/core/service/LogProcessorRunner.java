package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.api.LogDebug;
import com.epam.deltix.gflog.core.idle.IdleStrategy;
import com.epam.deltix.gflog.core.util.Util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeoutException;


final class LogProcessorRunner implements Runnable, AutoCloseable {

    private static final int CLOSE_TIMEOUT = 5000;
    private static final int CLOSE_ATTEMPTS = 6;

    private final Thread thread;
    private final AsyncLogProcessor processor;
    private final IdleStrategy strategy;

    private final CountDownLatch barrier = new CountDownLatch(1);

    private Throwable exception;

    LogProcessorRunner(final AsyncLogProcessor processor, final ThreadFactory factory, final IdleStrategy strategy) {
        final Thread thread = factory.newThread(this);
        thread.setDaemon(true);

        final ThreadUncaughtExceptionHandler uncaughtHandler = new ThreadUncaughtExceptionHandler(thread);
        thread.setUncaughtExceptionHandler(uncaughtHandler);

        this.thread = thread;
        this.processor = processor;
        this.strategy = strategy;
    }

    public void open() {
        try {
            final Thread.State state = thread.getState();
            if (state != Thread.State.NEW) {
                throw new IllegalStateException("Thread: " + thread + " is not in new state: " + state);
            }

            thread.start();
            barrier.await();

            final Throwable exception = this.exception;
            if (exception != null) {
                throw exception;
            }
        } catch (final Throwable e) {
            Util.rethrow(e);
        }
    }

    @Override
    public void close() {
        try {
            int attempts = 0;
            signalToClose();

            while (!awaitClose()) {
                if (++attempts < CLOSE_ATTEMPTS) {
                    LogDebug.warn("can't stop gflog thread: " + thread + ". Attempt: " + attempts + ". Awaiting...");
                } else {
                    final StackTraceElement[] stackTrace = thread.getStackTrace();

                    final Exception cause = new Exception("Thread: " + thread);
                    cause.setStackTrace(stackTrace);

                    final Throwable exception = new TimeoutException("can't stop thread: " + thread)
                            .initCause(cause);

                    LogDebug.warn("can't stop gflog thread: " + thread + ". Attempt: " + attempts + ". Stack: ", exception);
                }
            }
        } catch (final Throwable e) {
            Util.rethrow(e);
        }
    }

    private void signalToClose() {
        processor.deactivate();
    }

    private boolean awaitClose() {
        try {
            thread.join(LogProcessorRunner.CLOSE_TIMEOUT);
        } catch (final Throwable e) {
            Util.rethrow(e);
        }

        return !thread.isAlive();
    }

    @Override
    public void run() {
        if (doOpen()) {
            try {
                doWork();
            } finally {
                doClose();
            }
        }
    }

    private boolean doOpen() {
        try {
            processor.open();
            return true;
        } catch (final Throwable e) {
            exception = e;
            return false;
        } finally {
            barrier.countDown();
        }
    }

    private void doClose() {
        processor.close();
    }

    private void doWork() {
        while (processor.active()) {
            try {
                final int work = processor.work();
                strategy.idle(work);
            } catch (final Throwable e) {
                LogDebug.warn(e);
            }
        }
    }

    private final class ThreadUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        private final Thread.UncaughtExceptionHandler delegate;

        private ThreadUncaughtExceptionHandler(final Thread thread) {
            delegate = thread.getUncaughtExceptionHandler();
        }

        @Override
        public void uncaughtException(final Thread t, final Throwable e) {
            try {
                if (delegate != null) {
                    delegate.uncaughtException(t, e);
                }
            } finally {
                if (barrier.getCount() > 0) {
                    exception = e;
                    barrier.countDown();
                }
            }
        }
    }

}
