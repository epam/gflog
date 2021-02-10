package com.epam.deltix.gflog.core.appender;

import com.epam.deltix.gflog.api.LogDebug;
import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.LogRecord;
import com.epam.deltix.gflog.core.layout.Layout;

import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;


class DailyRollingFileAppender extends FileAppender {

    protected final String filePrefix;
    protected final String fileSuffix;
    protected final String fileMiddle;

    protected final Path fileDirectory;

    protected final DateTimeFormatter fileMiddleTemplate;
    protected final ZoneId zoneId;

    protected final int maxFiles;
    protected final long maxFileSize;

    protected ArrayDeque<LogFile> files;
    protected LogFile file;

    protected long nextFileSize;
    protected long nextIndex;
    protected long nextDay = Long.MIN_VALUE;

    protected DailyRollingFileAppender(final String name,
                                       final LogLevel level,
                                       final int bufferCapacity,
                                       final int flushCapacity,
                                       final Layout layout,
                                       final boolean append,
                                       final String file,
                                       final String fileMiddleTemplate,
                                       final ZoneId zoneId,
                                       final int maxFiles,
                                       final long maxFileSize) {

        super(name, level, bufferCapacity, flushCapacity, layout, append, file);

        final Path filePath = Paths.get(file).toAbsolutePath();

        final String fileName = filePath.getFileName().toString();
        final int index = fileName.lastIndexOf('.');

        this.filePrefix = (index == -1) ? fileName : fileName.substring(0, index);
        this.fileSuffix = (index == -1) ? "" : fileName.substring(index);
        this.fileMiddle = fileMiddleTemplate;

        this.fileDirectory = filePath.getParent();
        this.fileMiddleTemplate = DateTimeFormatter.ofPattern(fileMiddleTemplate);
        this.zoneId = zoneId;

        this.maxFiles = Math.max(maxFiles, 0);
        this.maxFileSize = (maxFileSize > 0) ? maxFileSize : Long.MAX_VALUE;
    }

    @Override
    public void open() throws Exception {
        Files.createDirectories(fileDirectory);
        files = LogDirectoryVisitor.visit(fileDirectory, filePrefix, fileSuffix, fileMiddle, fileMiddleTemplate, zoneId, maxFiles);

        final LogFile file = files.peekLast();

        if (file == null) {
            openNewFile();
        } else {
            openExistingFile(file);
        }
    }

    @Override
    public int append(final LogRecord record) throws Exception {
        if (needRoll(record)) {
            roll();
        }

        return super.append(record);
    }

    protected boolean needRoll(final LogRecord record) {
        return record.getTimestamp() >= nextDay || nextFileSize >= maxFileSize;
    }

    protected void roll() throws Exception {
        closeFile();
        openNewFile();
        clean();
    }

    @Override
    protected int doLog(final LogRecord record) throws Exception {
        final int bytes = super.doLog(record);
        nextFileSize += bytes;
        return bytes;
    }

    protected void openNewFile() throws Exception {
        final ZonedDateTime now = Instant.now().atZone(zoneId);
        final ZonedDateTime next = now.plusDays(1).truncatedTo(ChronoUnit.DAYS);

        final Path filePath = filePath(now);
        final FileChannel fileChannel = openChannel(filePath);

        channel = fileChannel;
        file = new LogFile(filePath, now, nextIndex);
        files.add(file);

        nextFileSize = fileChannel.size();
        nextDay = TimeUnit.SECONDS.toNanos(next.toInstant().getEpochSecond());
        nextIndex++;
    }

    protected void openExistingFile(final LogFile existing) throws Exception {
        final ZonedDateTime now = existing.timestamp;
        final ZonedDateTime next = now.plusDays(1).truncatedTo(ChronoUnit.DAYS);

        final Path filePath = existing.path;
        final FileChannel fileChannel = openChannel(filePath);

        channel = fileChannel;
        file = existing;

        nextFileSize = fileChannel.size();
        nextDay = TimeUnit.SECONDS.toNanos(next.toInstant().getEpochSecond());
        nextIndex = existing.index + 1;
    }

    protected void closeFile() throws Exception {
        flush(true);
        closeChannel(channel);
    }

    protected void clean() {
        while (files.size() > maxFiles) {
            final LogFile file = files.remove();

            if (maxFiles > 0) {
                deleteFile(file);
            }
        }
    }

    protected Path filePath(final ZonedDateTime currentDay) {
        final String name = filePrefix + currentDay.format(fileMiddleTemplate) + "." + nextIndex + fileSuffix;
        return fileDirectory.resolve(name);
    }

    protected static void deleteFile(final LogFile file) {
        final Path path = file.path;

        try {
            Files.deleteIfExists(path);
        } catch (final Throwable e) {
            LogDebug.warn("can't delete a file: " + path, e);
        }
    }

    protected static class LogFile {

        protected final Path path;
        protected final ZonedDateTime timestamp;
        protected final long index;

        public LogFile(final Path path, final ZonedDateTime timestamp, final long index) {
            this.path = path;
            this.timestamp = timestamp;
            this.index = index;
        }

    }

    protected static class LogDirectoryVisitor extends SimpleFileVisitor<Path> {

        protected final TreeSet<LogFile> files = new TreeSet<>(Comparator.comparingLong(o -> o.index));

        protected final String prefix;
        protected final String suffix;
        protected final String middle;

        protected final DateTimeFormatter template;
        protected final ZoneId zoneId;

        protected final int maxFilesToKeep;
        protected final boolean maxFilesToDelete;

        public LogDirectoryVisitor(final String prefix,
                                   final String suffix,
                                   final String middle,
                                   final DateTimeFormatter template,
                                   final ZoneId zoneId,
                                   final int maxFiles) {
            this.prefix = prefix;
            this.suffix = suffix;
            this.middle = middle;
            this.template = template;
            this.zoneId = zoneId;
            this.maxFilesToKeep = Math.max(1, maxFiles);
            this.maxFilesToDelete = (maxFiles > 0);
        }

        @Override
        public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) {
            try {
                doVisit(path);
            } catch (final Throwable e) {
                LogDebug.warn("can't figure out if a file matches the template: " + path, e);
            }

            return FileVisitResult.CONTINUE;
        }

        protected void doVisit(final Path path) {
            final String name = path.getFileName().toString();

            if ((name.length() > prefix.length() + suffix.length() + middle.length() + 1) && name.startsWith(prefix) && name.endsWith(suffix)) {
                final int indexEnd = name.length() - suffix.length();
                final int indexStart = name.lastIndexOf('.', indexEnd - 1);

                if (indexStart == prefix.length() + middle.length()) {
                    final long index = Long.parseUnsignedLong(name.substring(indexStart + 1, indexEnd));
                    final ZonedDateTime timestamp = LocalDate.parse(name.substring(prefix.length(), indexStart), template).atStartOfDay(zoneId);

                    onLogFile(path, timestamp, index);
                }
            }
        }

        protected void onLogFile(final Path path, final ZonedDateTime timestamp, final long index) {
            final LogFile file = new LogFile(path, timestamp, index);
            files.add(file);

            if (files.size() > maxFilesToKeep) {
                final LogFile lowest = files.first();
                files.remove(lowest);

                if (maxFilesToDelete) {
                    deleteFile(lowest);
                }
            }
        }

        public static ArrayDeque<LogFile> visit(final Path directory,
                                                final String prefix,
                                                final String suffix,
                                                final String middle,
                                                final DateTimeFormatter template,
                                                final ZoneId zoneId,
                                                final int maxFiles) throws Exception {
            final LogDirectoryVisitor visitor = new LogDirectoryVisitor(prefix, suffix, middle, template, zoneId, maxFiles);
            Files.walkFileTree(directory, Collections.emptySet(), 1, visitor);

            return new ArrayDeque<>(visitor.files);
        }

    }

}
