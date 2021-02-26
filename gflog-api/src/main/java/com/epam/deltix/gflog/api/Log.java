package com.epam.deltix.gflog.api;


public interface Log {

    /**
     * Returns the log name.
     *
     * @return the log name.
     */
    String getName();

    /**
     * Returns the log level.
     *
     * @return the log level.
     */
    LogLevel getLevel();

    /**
     * Sets the log level.
     *
     * @param level to set.
     */
    void setLevel(final LogLevel level);


    /**
     * Checks if the specific log level is enabled.
     *
     * @param level to check against.
     * @return true if enabled.
     */
    boolean isEnabled(final LogLevel level);

    /**
     * Checks if trace log level is enabled.
     *
     * @return true if enabled.
     */
    boolean isTraceEnabled();

    /**
     * Checks if debug log level is enabled.
     *
     * @return true if enabled.
     */
    boolean isDebugEnabled();

    /**
     * Checks if info log level is enabled.
     *
     * @return true if enabled.
     */
    boolean isInfoEnabled();

    /**
     * Checks if warn log level is enabled.
     *
     * @return true if enabled.
     */
    boolean isWarnEnabled();

    /**
     * Checks if error log level is enabled.
     *
     * @return true if enabled.
     */
    boolean isErrorEnabled();

    /**
     * Checks if fatal log level is enabled.
     *
     * @return true if enabled.
     */
    boolean isFatalEnabled();


    /**
     * Returns a log entry with the specific log level that can be filled and committed/aborted.
     *
     * @param level to log with.
     * @return a log entry with the specified level.
     */
    LogEntry log(final LogLevel level);

    /**
     * Returns a log entry with trace log level that can be filled and committed/aborted.
     *
     * @return a log entry with trace level.
     */
    LogEntry trace();

    /**
     * Returns a log entry with debug log level that can be filled and committed/aborted.
     *
     * @return a log entry with debug level.
     */
    LogEntry debug();

    /**
     * Returns a log entry with info log level that can be filled and committed/aborted.
     *
     * @return a log entry with info level.
     */
    LogEntry info();

    /**
     * Returns a log entry with warn log level that can be filled and committed/aborted.
     *
     * @return a log entry with warn level.
     */
    LogEntry warn();

    /**
     * Returns a log entry with error log level that can be filled and committed/aborted.
     *
     * @return a log entry with error level.
     */
    LogEntry error();

    /**
     * Returns a log entry with fatal log level that can be filled and committed/aborted.
     *
     * @return a log entry with fatal level.
     */
    LogEntry fatal();


    /**
     * Returns a log entry template with the specific log level for the specific template.
     * The template is committed automatically as soon as filled.
     *
     * @param level    to log with.
     * @param template to log with.
     * @return a log entry template with the specified level and template.
     */
    LogEntryTemplate log(final LogLevel level, final String template);

    /**
     * Returns a log entry template with trace log level for the specific template.
     * The template is committed automatically as soon as filled.
     *
     * @param template to log with.
     * @return a log entry template with trace level and the specified template.
     */
    LogEntryTemplate trace(final String template);

    /**
     * Returns a log entry template with debug log level for the specific template.
     * The template is committed automatically as soon as filled.
     *
     * @param template to log with.
     * @return a log entry template with debug level and the specified template.
     */
    LogEntryTemplate debug(final String template);

    /**
     * Returns a log entry template with info log level for the specific template.
     * The template is committed automatically as soon as filled.
     *
     * @param template to log with.
     * @return a log entry template with info level and the specified template.
     */
    LogEntryTemplate info(final String template);

    /**
     * Returns a log entry template with warn log level for the specific template.
     * The template is committed automatically as soon as filled.
     *
     * @param template to log with.
     * @return a log entry template with warn level and the specified template.
     */
    LogEntryTemplate warn(final String template);

    /**
     * Returns a log entry template with error log level for the specific template.
     * The template is committed automatically as soon as filled.
     *
     * @param template to log with.
     * @return a log entry template with error level and the specified template.
     */
    LogEntryTemplate error(final String template);

    /**
     * Returns a log entry template with fatal log level for the specific template.
     * The template is committed automatically as soon as filled.
     *
     * @param template to log with.
     * @return a log entry template with fatal level and the specified template.
     */
    LogEntryTemplate fatal(final String template);

}
