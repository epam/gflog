package com.epam.deltix.gflog.core.appender;

import com.epam.deltix.gflog.api.LogDebug;
import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.LogRecord;
import com.epam.deltix.gflog.core.layout.Layout;
import com.epam.deltix.gflog.core.util.UnsafeBuffer;
import com.epam.deltix.gflog.core.util.Util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;


public abstract class NioAppender<T extends WritableByteChannel> extends Appender {

    protected final ByteBuffer byteBuffer;
    protected final UnsafeBuffer buffer;
    protected final Layout layout;

    protected final int capacity;
    protected final int flushCapacity;

    protected int offset;
    protected T channel;

    protected NioAppender(final String name,
                          final LogLevel level,
                          final int bufferCapacity,
                          final int flushCapacity,
                          final Layout layout) {
        super(name, level);

        this.buffer = UnsafeBuffer.allocateDirectedAlignedPadded(bufferCapacity, Util.DOUBLE_CACHE_LINE_SIZE);
        this.byteBuffer = buffer.byteBuffer();
        this.layout = layout;
        this.capacity = bufferCapacity;
        this.flushCapacity = flushCapacity;
    }

    @Override
    public void open() throws Exception {
        channel = openChannel();
    }

    @Override
    public void close() throws Exception {
        try {
            flush(true);
            closeChannel(channel);
        } finally {
            channel = null;
        }
    }

    @Override
    public int append(final LogRecord record) throws Exception {
        doLog(record);
        return 1;
    }

    @Override
    public int flush() throws Exception {
        return flush(false);
    }

    protected int doLog(final LogRecord record) throws Exception {
        final int size = layout.size(record);
        final int remaining = capacity - offset;

        if (remaining < size) {
            verifyRecordSize(size);
            flush(true);
        }

        final int before = offset;
        offset = layout.format(record, buffer, offset);

        return offset - before;
    }

    protected int flush(final boolean force) throws IOException {
        if (offset > 0) {
            return doFlush(force);
        }

        return 0;
    }

    protected int doFlush(final boolean force) throws IOException {
        int bytesWritten = 0;

        try {
            final int size = offset;
            byteBuffer.position(0);

            do {
                final int limit = Math.min(byteBuffer.position() + flushCapacity, size);
                byteBuffer.limit(limit);

                bytesWritten += channel.write(byteBuffer);
            } while (byteBuffer.position() < size);
        } finally {
            offset = 0;
        }

        return bytesWritten;
    }

    protected abstract T openChannel() throws Exception;

    protected void closeChannel(final T channel) {
        if (channel != null) {
            try {
                channel.close();
            } catch (final Throwable e) {
                LogDebug.warn("can't close channel. Error: " + e.getMessage());
            }
        }
    }

    protected void verifyRecordSize(final int size) {
        if (size > capacity) {
            throw new IllegalArgumentException(("record size " + size + " more buffer capacity " + capacity));
        }
    }

}
