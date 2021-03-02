package com.epam.deltix.gflog.mail.appender;

final class SmtpSettings {

    private String to;
    private String from;
    private String subject;
    private int maxSubjectLength;
    private String host;
    private int port;
    private String secure;
    private String username;
    private String password;
    private int timeout;
    private boolean debug;

    SmtpSettings(final String to,
                 final String from,
                 final String subject,
                 final int maxSubjectLength,
                 final String host,
                 final int port,
                 final String secure,
                 final String username,
                 final String password,
                 final int timeout,
                 final boolean debug) {
        this.to = to;
        this.from = from;
        this.subject = subject;
        this.maxSubjectLength = maxSubjectLength;
        this.host = host;
        this.port = port;
        this.secure = secure;
        this.username = username;
        this.password = password;
        this.timeout = timeout;
        this.debug = debug;
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
