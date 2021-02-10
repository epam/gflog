package com.epam.deltix.gflog.api;


public final class NoOpLog implements Log {

    public static final NoOpLog INSTANCE = new NoOpLog();

    private NoOpLog() {
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setLevel(final LogLevel level) {
    }

    @Override
    public LogLevel getLevel() {
        return LogLevel.FATAL;
    }

    @Override
    public boolean isEnabled(final LogLevel level) {
        return false;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public boolean isFatalEnabled() {
        return false;
    }

    @Override
    public NoOpLogEntry log(final LogLevel level) {
        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public NoOpLogEntry trace() {
        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public NoOpLogEntry debug() {
        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public NoOpLogEntry info() {
        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public NoOpLogEntry warn() {
        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public NoOpLogEntry error() {
        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public NoOpLogEntry fatal() {
        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public NoOpLogEntry log(final LogLevel level, final String template) {
        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public NoOpLogEntry trace(final String template) {
        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public NoOpLogEntry debug(final String template) {
        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public NoOpLogEntry info(final String template) {
        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public NoOpLogEntry warn(final String template) {
        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public NoOpLogEntry error(final String template) {
        return NoOpLogEntry.INSTANCE;
    }

    @Override
    public NoOpLogEntry fatal(final String template) {
        return NoOpLogEntry.INSTANCE;
    }

}
