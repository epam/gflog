package com.epam.deltix.gflog.core.layout.template;

import com.epam.deltix.gflog.core.util.Util;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;


public final class TemplateParser {

    public static final int HEX_RADIX = 16;

    public static final char TEMPLATE_START = '%';
    public static final char OPTION_START = '{';
    public static final char OPTION_END = '}';

    public static final String ESCAPE_START = "\\0x";
    public static final int ESCAPE_SIZE = ESCAPE_START.length() + 2; // \0x1F

    public static final char LOG_NAME = 'c';
    public static final char LOG_LEVEL = 'p';
    public static final char THREAD_NAME = 't';
    public static final char DATE = 'd';
    public static final char MESSAGE = 'm';
    public static final char LINE_BREAK = 'n';

    public static Template[] parse(String pattern, final ZoneId zoneId) {
        pattern = unescape(pattern);
        return split(pattern, zoneId);
    }

    private static String unescape(String pattern) {
        if (pattern.contains(ESCAPE_START)) {
            final StringBuilder builder = new StringBuilder();

            final int length = pattern.length();
            int i = 0;

            while (true) {
                final int j = pattern.indexOf(ESCAPE_START, i);
                if (j == -1) {
                    break;
                }

                if (i < j) {
                    builder.append(pattern, i, j);
                }

                final int m = j + ESCAPE_START.length();
                final int k = j + ESCAPE_SIZE;

                final String value = pattern.substring(m, k);
                final char escaped = (char) Byte.parseByte(value, HEX_RADIX);

                builder.append(escaped);
                i = k;
            }

            if (i < length) {
                builder.append(pattern, i, length);
            }

            pattern = builder.toString();
        }

        return pattern;
    }

    private static Template[] split(final String pattern, final ZoneId zoneId) {
        final List<Template> templates = new ArrayList<>();

        final int length = pattern.length();
        final int end = length - 1;

        int i = 0;
        int j = 0;

        while (i < end) {
            char c = pattern.charAt(i);

            if (c == TEMPLATE_START) {
                String option = null;
                Template template = null;
                c = pattern.charAt(i + 1);

                switch (c) {
                    case LOG_LEVEL:
                        template = new LogLevelTemplate();
                        break;

                    case THREAD_NAME:
                        template = new ThreadNameTemplate();
                        break;

                    case MESSAGE:
                        template = new MessageTemplate();
                        break;

                    case LINE_BREAK:
                        template = new LiteralTemplate(Util.LINE_SEPARATOR);
                        break;

                    case LOG_NAME:
                        option = getOption(pattern, i, end);
                        template = (option == null) ?
                                new LogNameTemplate() :
                                new ShortLogNameTemplate(Integer.parseInt(option));
                        break;

                    case DATE:
                        option = getOption(pattern, i, end);
                        template = DefaultDateTimeTemplate.matches(option) ?
                                new DefaultDateTimeTemplate(option, zoneId) :
                                new CustomDateTimeTemplate(option, zoneId);
                        break;
                }

                if (template != null) {
                    if (j < i) {
                        final String literal = pattern.substring(j, i);
                        templates.add(new LiteralTemplate(literal));
                    }

                    templates.add(template);

                    i += 1 + ((option == null) ? 0 : (2 + option.length()));
                    j = i + 1;
                }
            }

            i++;
        }

        if (j < length) {
            final String literal = pattern.substring(j, length);
            templates.add(new LiteralTemplate(literal));
        }

        return templates.toArray(new Template[0]);
    }

    private static String getOption(final String pattern, int index, final int end) {
        index += 2;

        if (index < end) {
            final char c = pattern.charAt(index);

            if (c == OPTION_START) {
                final int beginIndex = index + 1;
                final int endIndex = pattern.indexOf(OPTION_END, beginIndex);

                if (endIndex > 0) {
                    return pattern.substring(beginIndex, endIndex);
                }
            }
        }

        return null;
    }

}