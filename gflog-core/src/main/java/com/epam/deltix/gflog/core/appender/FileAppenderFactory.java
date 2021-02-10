package com.epam.deltix.gflog.core.appender;

import com.epam.deltix.gflog.core.util.PropertyUtil;

import static java.util.Objects.requireNonNull;


public class FileAppenderFactory extends NioAppenderFactory {

    protected String file;
    protected boolean append = PropertyUtil.getBoolean("gflog.file.appender.append", true);

    public FileAppenderFactory() {
        super("file");
    }

    public void setAppend(final boolean append) {
        this.append = append;
    }

    public String getFile() {
        return file;
    }

    public void setFile(final String file) {
        this.file = file;
    }

    @Override
    protected void conclude() {
        super.conclude();
        requireNonNull(file, "file is null");
    }

    @Override
    protected Appender createAppender() {
        return new FileAppender(name, level, bufferCapacity, flushCapacity, layout, append, file);
    }

}
