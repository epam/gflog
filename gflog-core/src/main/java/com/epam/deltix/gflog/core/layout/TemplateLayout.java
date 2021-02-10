package com.epam.deltix.gflog.core.layout;

import com.epam.deltix.gflog.core.LogRecord;
import com.epam.deltix.gflog.core.layout.template.Template;
import com.epam.deltix.gflog.core.layout.template.TemplateParser;
import com.epam.deltix.gflog.core.util.MutableBuffer;

import java.time.ZoneId;


public class TemplateLayout extends Layout {

    private final Template[] templates;

    public TemplateLayout(final String template, final ZoneId zoneId) {
        this.templates = TemplateParser.parse(template, zoneId);
    }

    @Override
    public int size(final LogRecord record) {
        int size = 0;

        for (final Template template : templates) {
            size += template.size(record);
        }

        return size;
    }

    @Override
    public int format(final LogRecord record, final MutableBuffer buffer, int offset) {
        for (final Template template : templates) {
            offset = template.format(record, buffer, offset);
        }

        return offset;
    }

}
