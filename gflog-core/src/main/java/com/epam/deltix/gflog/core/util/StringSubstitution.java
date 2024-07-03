package com.epam.deltix.gflog.core.util;

import java.util.Map;
import java.util.Properties;


public final class StringSubstitution {

    private static final String SYSTEM_PROPERTY_PREFIX = "sys:";
    private static final String ENVIRONMENT_VARIABLE_PREFIX = "env:";

    private final Properties defaultProperties;
    private final Properties systemProperties;
    private final Map<String, String> environmentVariables;

    public StringSubstitution(final Properties defaultProperties,
                              final Properties systemProperties,
                              final Map<String, String> environmentVariables) {
        this.defaultProperties = defaultProperties;
        this.systemProperties = systemProperties;
        this.environmentVariables = environmentVariables;
    }

    public String substitute(final String text) {
        final StringBuilder builder = new StringBuilder();

        for (int index = 0; index < text.length(); ) {
            final int position = text.indexOf("${", index);

            if (position < 0) {
                builder.append(text, index, text.length());
                break;
            }

            builder.append(text, index, position);
            index = substitute(text, position, builder);
        }

        return builder.toString();
    }

    private int substitute(final String source, int index, final StringBuilder target) {
        final int end = source.length();
        final int start = index;

        index += 2;

        while (index < end) {
            char c = source.charAt(index);

            if (c == '}') {
                final String key = source.substring(start + 2, index);
                final String value = lookup(key);

                if (value == null) {
                    target.append("${").append(key).append(":#UNRESOLVED#").append("}");
                } else {
                    target.append(value);
                }

                return index + 1;
            }

            if (c == ':' && index + 1 < end && source.charAt(index + 1) == '-') {
                final String key = source.substring(start + 2, index);
                final String value = lookup(key);

                final int length = target.length();
                final int mid = index + 2;

                index = mid;

                while (index < end) {
                    c = source.charAt(index);

                    if (c == '}') {
                        if (value != null) {
                            target.setLength(length);
                            target.append(value);
                        }

                        return index + 1;
                    }

                    if (c == '$' && index + 1 < end && source.charAt(index + 1) == '{') {
                        index = substitute(source, index, target);
                        continue;
                    }

                    target.append(c);
                    index++;
                }

                target.insert(length, source, start, mid);
                return end;
            }

            index++;
        }

        target.append(source, start, end);
        return index;
    }

    private String lookup(final String key) {
        if (key.startsWith(SYSTEM_PROPERTY_PREFIX)) {
            final String propertyKey = key.substring(SYSTEM_PROPERTY_PREFIX.length());
            return systemProperties.getProperty(propertyKey);
        }

        if (key.startsWith(ENVIRONMENT_VARIABLE_PREFIX)) {
            final String environmentKey = key.substring(ENVIRONMENT_VARIABLE_PREFIX.length());
            return environmentVariables.get(environmentKey);
        }

        return defaultProperties.getProperty(key);
    }
}