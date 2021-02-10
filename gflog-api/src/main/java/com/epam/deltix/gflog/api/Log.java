package com.epam.deltix.gflog.api;


public interface Log {

    /**
     * Returns the log name.
     */
    String getName();

    /**
     * Returns the log level.
     */
    LogLevel getLevel();

    /**
     * Sets the log level.
     */
    void setLevel(final LogLevel level);


    /**
     * Checks if the log can log at the specific log level.
     */
    boolean isEnabled(final LogLevel level);

    /**
     * Checks if the log can log at the trace log level.
     */
    boolean isTraceEnabled();

    /**
     * Checks if the log can log at the debug log level.
     */
    boolean isDebugEnabled();

    /**
     * Checks if the log can log at the info log level.
     */
    boolean isInfoEnabled();

    /**
     * Checks if the log can log at the warn log level.
     */
    boolean isWarnEnabled();

    /**
     * Checks if the log can log at the error log level.
     */
    boolean isErrorEnabled();

    /**
     * Checks if the log can log at the fatal log level.
     */
    boolean isFatalEnabled();


    /**
     * Returns a log entry at the specific log level that can be filled and committed/aborted.
     */
    LogEntry log(final LogLevel level);

    /**
     * Returns a log entry at the trace log level that can be filled and committed/aborted.
     */
    LogEntry trace();

    /**
     * Returns a log entry at the debug log level that can be filled and committed/aborted.
     */
    LogEntry debug();

    /**
     * Returns a log entry at the info log level that can be filled and committed/aborted.
     */
    LogEntry info();

    /**
     * Returns a log entry at the warn log level that can be filled and committed/aborted.
     */
    LogEntry warn();

    /**
     * Returns a log entry at the error log level that can be filled and committed/aborted.
     */
    LogEntry error();

    /**
     * Returns a log entry at the fatal log level that can be filled and committed/aborted.
     */
    LogEntry fatal();


    /**
     * Returns a log template at the specific log level for the specific template.
     * The template is committed automatically as soon as filled.
     */
    LogEntryTemplate log(final LogLevel level, final String template);

    /**
     * Returns a log template at the trace log level for the specific template.
     * The template is committed automatically as soon as filled.
     */
    LogEntryTemplate trace(final String template);

    /**
     * Returns a log template at the debug log level for the specific template.
     * The template is committed automatically as soon as filled.
     */
    LogEntryTemplate debug(final String template);

    /**
     * Returns a log template at the info log level for the specific template.
     * The template is committed automatically as soon as filled.
     */
    LogEntryTemplate info(final String template);

    /**
     * Returns a log template at the warn log level for the specific template.
     * The template is committed automatically as soon as filled.
     */
    LogEntryTemplate warn(final String template);

    /**
     * Returns a log template at the error log level for the specific template.
     * The template is committed automatically as soon as filled.
     */
    LogEntryTemplate error(final String template);

    /**
     * Returns a log template at the fatal log level for the specific template.
     * The template is committed automatically as soon as filled.
     */
    LogEntryTemplate fatal(final String template);

}
