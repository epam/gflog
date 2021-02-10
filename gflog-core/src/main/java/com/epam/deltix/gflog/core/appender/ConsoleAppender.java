package com.epam.deltix.gflog.core.appender;

import com.epam.deltix.gflog.api.LogDebug;
import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.layout.Layout;
import com.epam.deltix.gflog.core.util.Util;

import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;


public class ConsoleAppender extends NioAppender<WritableByteChannel> {

    protected final boolean wrap;
    protected final boolean stderr;

    protected ConsoleAppender(final String name,
                              final LogLevel level,
                              final int bufferCapacity,
                              final int flushCapacity,
                              final Layout layout,
                              final boolean wrap,
                              final boolean stderr) {
        super(name, level, bufferCapacity, flushCapacity, layout);

        this.wrap = wrap;
        this.stderr = stderr;
    }

    @Override
    protected void closeChannel(final WritableByteChannel channel) {
        // skip
    }

    @Override
    protected WritableByteChannel openChannel() {
        final PrintStream stream = stderr ? System.err : System.out;
        WritableByteChannel channel = null;

        if (!wrap) {
            try {
                channel = unwrap(stream);
            } catch (final Throwable e) {
                LogDebug.warn("can't unwrap system out/err stream. Will use wrapper. Error: " + e.getMessage());
            }
        }

        if (channel == null) {
            channel = Channels.newChannel(stream);
        }

        return channel;
    }

    private static FileChannel unwrap(OutputStream stream) throws Exception {
        final Field field = FilterOutputStream.class.getDeclaredField("out");
        final long fieldOffset = Util.UNSAFE.objectFieldOffset(field);

        if (field.getType() != OutputStream.class) {
            throw new AssertionError("FilterOutputStream.out type is not OutputStream");
        }

        while (stream instanceof FilterOutputStream) {
            stream = (OutputStream) Util.UNSAFE.getObject(stream, fieldOffset);
        }

        if (stream instanceof FileOutputStream) {
            return ((FileOutputStream) stream).getChannel();
        }

        return null;
    }

}
