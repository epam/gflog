package com.epam.deltix.gflog.core.appender;

import com.epam.deltix.gflog.core.util.PropertyUtil;


public class ConsoleAppenderFactory extends NioAppenderFactory {

    // Some versions of Windows have a limit how many bytes can be written at once in the underlying channel
    protected static final int FLUSH_CAPACITY_WINDOWS_DEFAULT = 16 * 1024;

    protected boolean wrap = PropertyUtil.getBoolean("gflog.console.appender.wrap", false);
    protected boolean stderr = PropertyUtil.getBoolean("gflog.console.appender.stderr", false);

    public ConsoleAppenderFactory() {
        super("console");
    }

    public void setWrap(final boolean wrap) {
        this.wrap = wrap;
    }

    public boolean isWrap() {
        return wrap;
    }

    public boolean isStderr() {
        return stderr;
    }

    public void setStderr(final boolean stderr) {
        this.stderr = stderr;
    }

    @Override
    protected void conclude() {
        if (flushCapacity == 0 && isWindows()) {
            flushCapacity = FLUSH_CAPACITY_WINDOWS_DEFAULT;
        }

        super.conclude();
    }

    @Override
    protected ConsoleAppender createAppender() {
        return new ConsoleAppender(name, level, bufferCapacity, flushCapacity, layout, wrap, stderr);
    }

    protected static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

}
