package com.epam.deltix.gflog.core.appender;

import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.layout.Layout;

import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


public class FileAppender extends NioAppender<FileChannel> {

    protected final boolean append;
    protected final Path file;

    protected FileAppender(final String name,
                           final LogLevel level,
                           final int bufferCapacity,
                           final int flushCapacity,
                           final Layout layout,
                           final boolean append,
                           final String file) {

        super(name, level, bufferCapacity, flushCapacity, layout);

        this.file = Paths.get(file);
        this.append = append;
    }

    @Override
    protected FileChannel openChannel() throws Exception {
        return openChannel(file);
    }

    protected FileChannel openChannel(final Path file) throws Exception {
        final Path directory = file.getParent();

        if (directory != null) {
            Files.createDirectories(directory);
        }

        return FileChannel.open(
                file,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING
        );
    }

}
