package com.epam.deltix.gflog.core.appender;

import com.epam.deltix.gflog.core.util.PropertyUtil;

import java.time.ZoneId;


public class DailyRollingFileAppenderFactory extends FileAppenderFactory {

    protected static final String FILE_SUFFIX_TEMPLATE = PropertyUtil.getString("gflog.daily.rolling.appender.file.suffix.template", "-yyyy-MM-dd");
    protected static final int MAX_FILES = PropertyUtil.getInteger("gflog.daily.rolling.appender.max.files", 0);
    protected static final long MAX_FILE_SIZE = PropertyUtil.getMemory("gflog.daily.rolling.appender.max.file.size", 0L);

    protected String fileSuffixTemplate;
    protected ZoneId zoneId;

    protected int maxFiles = MAX_FILES;
    protected long maxFileSize = MAX_FILE_SIZE;

    public String getFileSuffixTemplate() {
        return fileSuffixTemplate;
    }

    public void setFileSuffixTemplate(final String fileSuffixTemplate) {
        this.fileSuffixTemplate = fileSuffixTemplate;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public void setZoneId(final ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public void setMaxFiles(final int maxFiles) {
        this.maxFiles = maxFiles;
    }

    public void setMaxFileSize(final long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    @Override
    protected void conclude() {
        super.conclude();

        if (fileSuffixTemplate == null) {
            fileSuffixTemplate = FILE_SUFFIX_TEMPLATE;
        }

        if (zoneId == null) {
            zoneId = ZoneId.systemDefault();
        }
    }

    @Override
    protected DailyRollingFileAppender createAppender() {
        return new DailyRollingFileAppender(
                name,
                level,
                bufferCapacity,
                flushCapacity,
                layout,
                append,
                file,
                fileSuffixTemplate,
                zoneId,
                maxFiles,
                maxFileSize
        );
    }

}
