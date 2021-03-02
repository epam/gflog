package com.epam.deltix.gflog.mail.appender;

import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.appender.Appender;
import com.epam.deltix.gflog.core.appender.NioAppenderFactory;

import static com.epam.deltix.gflog.core.util.PropertyUtil.*;


public final class SmtpAppenderFactory extends NioAppenderFactory {

    protected static final LogLevel PUSH_LEVEL = LogLevel.valueOf(getString("gflog.smtp.appender.push.level", LogLevel.ERROR.name()));
    protected static final int PERIOD = getInteger("gflog.smtp.appender.period", 5 * 60 * 1000);
    protected static final int MAX_ENTRIES = getInteger("gflog.smtp.appender.max.entries", 128);
    protected static final int MAX_EMAILS = getInteger("gflog.smtp.appender.max.emails", 5);

    protected static final String TO = getString("gflog.smtp.appender.to", null);
    protected static final String FROM = getString("gflog.smtp.appender.from", null);
    protected static final String SUBJECT = getString("gflog.smtp.appender.subject", null);
    protected static final int MAX_SUBJECT_LENGTH = getInteger("gflog.smtp.appender.max.subject.length", 64);
    protected static final String HOST = getString("gflog.smtp.appender.host", null);
    protected static final int PORT = getInteger("gflog.smtp.appender.port", 0);
    protected static final String SECURE = getString("gflog.smtp.appender.secure", null);
    protected static final String USERNAME = getString("gflog.smtp.appender.username", null);
    protected static final String PASSWORD = getString("gflog.smtp.appender.password", null);
    protected static final int TIMEOUT = getInteger("gflog.smtp.appender.timeout", 10 * 10000);
    protected static final boolean DEBUG = getBoolean("gflog.smtp.appender.debug", false);

    public SmtpAppenderFactory() {
        super("smtp");
    }

    private LogLevel pushLevel = PUSH_LEVEL;
    private int period = PERIOD;
    private int maxEntries = MAX_ENTRIES;
    private int maxEmails = MAX_EMAILS;
    private String to = TO;
    private String from = FROM;
    private String subject = SUBJECT;
    private int maxSubjectLength = MAX_SUBJECT_LENGTH;
    private String host = HOST;
    private int port = PORT;
    private String secure = SECURE;
    private String username = USERNAME;
    private String password = PASSWORD;
    private int timeout = TIMEOUT;
    private boolean debug = DEBUG;

    @Override
    protected void conclude() {
        super.conclude();

        if (to == null) {
            throw new IllegalArgumentException("Smtp To (mail addresses) is not specified");
        }
    }

    @Override
    protected Appender createAppender() {
        final SmtpSettings settings = new SmtpSettings(to, from, subject, maxSubjectLength, host, port,
                secure, username, password, timeout, debug);

        return new SmtpAppender(name, level, bufferCapacity, flushCapacity, layout, maxEntries, pushLevel, period, maxEmails, settings);
    }

    public LogLevel getPushLevel() {
        return pushLevel;
    }

    public void setPushLevel(final LogLevel pushLevel) {
        this.pushLevel = pushLevel;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(final int period) {
        this.period = period;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public void setMaxEntries(final int maxEntries) {
        this.maxEntries = maxEntries;
    }

    public int getMaxEmails() {
        return maxEmails;
    }

    public void setMaxEmails(final int maxEmails) {
        this.maxEmails = maxEmails;
    }

    public String getTo() {
        return to;
    }

    public void setTo(final String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(final String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public int getMaxSubjectLength() {
        return maxSubjectLength;
    }

    public void setMaxSubjectLength(final int maxSubjectLength) {
        this.maxSubjectLength = maxSubjectLength;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public String getSecure() {
        return secure;
    }

    public void setSecure(final String secure) {
        this.secure = secure;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

}
