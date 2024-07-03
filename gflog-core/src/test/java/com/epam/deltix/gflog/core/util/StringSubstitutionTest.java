package com.epam.deltix.gflog.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class StringSubstitutionTest {

    @Test
    public void testDefaults() {
        final Properties defaults = new Properties();
        defaults.put("a", "A");
        defaults.put("b", "B");
        defaults.put("c", "C");

        final Properties systems = new Properties();
        final Map<String, String> environments = new HashMap<>();
        final StringSubstitution substitution = new StringSubstitution(defaults, systems, environments);
        final Map<String, String> mapping = new LinkedHashMap<>();

        mapping.put("12345", "12345");
        mapping.put("${a} ${b} ${c}", "A B C");
        mapping.put("${a:-10} ${b:-${c:-100500}}", "A B");
        mapping.put("${missing:-5} + ${missing:-10}", "5 + 10");
        mapping.put("${missing:-${a}}", "A");
        mapping.put("${missing:-${missing2:-${b}}}", "B");
        mapping.put("${missing:-${missing2:-${missing3:-${c}}}}", "C");
        mapping.put("${a:-5", "${a:-5");
        mapping.put(" ${a:-${b} ", " ${a:-B ");
        mapping.put(" ${a:-${b:-${c ", " ${a:-${b:-${c ");
        mapping.put(" ${a:-${b:-${c} ", " ${a:-${b:-C ");
        mapping.put(" ${a:-${b:-${c}} ", " ${a:-B ");
        mapping.put("${missing}", "${missing:#UNRESOLVED#}");
        mapping.put("${missing:-1 ${a} 2 ${missing2} 3}", "1 A 2 ${missing2:#UNRESOLVED#} 3");
        mapping.put("${missing:-}", "");

        for (final Map.Entry<String, String> entry : mapping.entrySet()) {
            final String text = entry.getKey();
            final String expected = entry.getValue();
            final String actual = substitution.substitute(text);

            Assert.assertEquals(expected, actual);
        }
    }

    @Test
    public void testEnvironmentAndSystemSubstitution() {
        final Properties defaults = new Properties();
        defaults.put("a", "1");
        defaults.put("b", "2");
        defaults.put("c", "3");

        final Properties systems = new Properties();
        systems.put("a", "4");
        systems.put("b", "5");
        systems.put("c", "6");

        final Map<String, String> environments = new HashMap<>();
        environments.put("a", "7");
        environments.put("b", "8");
        environments.put("c", "9");

        final StringSubstitution substitution = new StringSubstitution(defaults, systems, environments);
        final Map<String, String> mapping = new LinkedHashMap<>();

        mapping.put("${a} ${sys:a} ${env:a}", "1 4 7");
        mapping.put("${missing:-${b}} ${missing:-${sys:b}} ${missing:-${env:b}}", "2 5 8");
        mapping.put("${sys:missing:-${b}} ${sys:missing:-${sys:b}} ${sys:missing:-${env:b}}", "2 5 8");
        mapping.put("${env:missing:-${b}} ${env:missing:-${sys:b}} ${env:missing:-${env:b}}", "2 5 8");

        for (final Map.Entry<String, String> entry : mapping.entrySet()) {
            final String text = entry.getKey();
            final String expected = entry.getValue();
            final String actual = substitution.substitute(text);

            Assert.assertEquals(expected, actual);
        }
    }
}
