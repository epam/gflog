package com.epam.deltix.gflog.jul;

import com.epam.deltix.gflog.api.LogEntry;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


public class JulMessageAppenderTest {

    @Test
    public void formatsMessageFormatPlaceholders() {
        Assert.assertEquals("Error: dead process, dead socket",
                formatParams("Error: {0} {1}, {0} socket", "dead", "process"));
        Assert.assertEquals("Warning: dead socket", formatParam("Warning: {0}", "dead socket"));
        Assert.assertEquals("Warning: {1}", formatParam("Warning: {1}", "dead socket"));
        Assert.assertEquals("see {hello} x", formatParams("see {hello} {0}", "x"));
        Assert.assertEquals("n {0,number}", formatParams("n {0,number}", 1));
        Assert.assertEquals("Value null", formatParams("Value {0}", new Object[]{null}));
    }

    @Test
    public void formatsPrintfPlaceholders() {
        Assert.assertEquals(String.format("%1$s %1$s", "a", "b"), formatParams("%1$s %1$s", "a", "b"));
        Assert.assertEquals(String.format("%s %s", "a", "b"), formatParams("%s %s", "a", "b"));
        Assert.assertEquals(String.format("%s %2$s %s", "a", "b", "c"), formatParams("%s %2$s %s", "a", "b", "c"));
        Assert.assertEquals(String.format("%1$s%2$s", "a", "b"), formatParams("%1$s%2$s", "a", "b"));
        Assert.assertEquals(String.format("100%%"), formatParams("100%%", "ignored"));
        Assert.assertEquals(String.format("%%1$s", "a"), formatParams("%%1$s", "a"));
        Assert.assertEquals(String.format("%%%s", "a"), formatParams("%%%s", "a"));
        Assert.assertEquals("a %s", formatParam("%s %s", "a"));
        Assert.assertEquals("a %2$s", formatParams("%1$s %2$s", "a"));
        Assert.assertEquals("%0$s", formatParams("%0$s", "a"));
        Assert.assertEquals("null", formatParams("%1$s", new Object[]{null}));
        Assert.assertEquals("n 7", formatParams("n %s", 7));
        Assert.assertEquals(String.format("n %d", 7), formatParams("n %d", 7));
        Assert.assertEquals(String.format("%d", -3), formatParams("%d", -3));
        Assert.assertEquals(String.format("%d", 7L), formatParams("%d", 7L));
        Assert.assertEquals(String.format("%1$d %1$d", 4), formatParams("%1$d %1$d", 4));
        Assert.assertEquals(String.format("%b %b %b %b", true, false, null, "x"),
                formatParams("%b %b %b %b", true, false, null, "x"));
        Assert.assertEquals("5 is 6", formatParams("{0} is %2$d", 5, 6));
        Assert.assertEquals("%d y", formatParams("%d %s", "x", "y"));
    }

    @Test
    public void formatsIndexedStringPlaceholders() {
        final Object[] args = {"alpha", "beta", "gamma"};
        final String[] patterns = {
                "Feature String sent to peer:%1$s%2$s",
                "Feature string returned from peer:%1$s%2$s",
                "Session established with: %1$s, compression used: %2$s",
                "Connection %1$s is ready.",
                "AuthorizationManager connectionUp (%1$s)",
                "Setting primary to %1$s",
                "Connection {%1$s, %2$s} up, new state of the routing domain manager: %3$s",
                "Using %1$s as a source IP in TokenRequest",
                "Cleaned up dir: %1$s",
                "Stop called with StopOption=%1$s",
                "Closing CbSocketChannel for: %1$s",
                "Publishing last event: %1$s"
        };

        for (final String pattern : patterns) {
            Assert.assertEquals(pattern, String.format(pattern, args), formatParams(pattern, args));
        }

        Assert.assertEquals("Connection {alpha} and beta",
                formatParams("Connection {%1$s} and {1}", "alpha", "beta"));
        Assert.assertEquals("Warning: dead socket", formatParam("Warning: %1$s", "dead socket"));
        Assert.assertEquals("Warning: dead socket", formatParam("Warning: %s", "dead socket"));
        Assert.assertEquals("Warning: %2$s", formatParam("Warning: %2$s", "dead socket"));
    }

    @Test
    public void leavesUnsupportedPatterns() {
        Assert.assertEquals("value %f", formatParams("value %f", 1.5d));
        Assert.assertEquals("value %10s", formatParams("value %10s", "x"));
        Assert.assertEquals("value %i", formatParams("value %i", 1));
        Assert.assertEquals("value %1$x", formatParams("value %1$x", 1));
        Assert.assertEquals("value %d", formatParams("value %d", "x"));
        Assert.assertEquals("100% done", formatParams("100% done", "x"));
        Assert.assertEquals("100%", formatParams("100%", "x"));
        Assert.assertEquals("Session started", formatParams("Session started", "x"));
        Assert.assertEquals("", formatParams("", "x"));
    }

    @Test
    public void appendsIntegralsAsNumbers() {
        final Capture capture = new Capture();
        JulMessageAppender.append("n %d", Integer.valueOf(7), capture.entry());

        Assert.assertEquals("n 7", capture.text());
        Assert.assertTrue(capture.parameterTypes().contains("[int]"));
        Assert.assertFalse(capture.parameterTypes().contains("[Object]"));
    }

    @Test
    public void doesNotFormatWithoutParameters() {
        Assert.assertEquals("Session established with: %1$s", formatParams("Session established with: %1$s"));
        Assert.assertEquals("Warning: {0}", formatParams("Warning: {0}"));
        Assert.assertEquals("100%%", formatParams("100%%"));
        Assert.assertEquals("null", formatParams(null, "x"));
        Assert.assertEquals("null", formatParam(null, "x"));
    }

    private static String formatParam(final String message, final Object param) {
        final Capture capture = new Capture();
        JulMessageAppender.append(message, param, capture.entry());
        return capture.text();
    }

    private static String formatParams(final String message, final Object... params) {
        final Capture capture = new Capture();
        JulMessageAppender.append(message, params, capture.entry());
        return capture.text();
    }

    private static final class Capture implements InvocationHandler {

        private final StringBuilder builder = new StringBuilder();
        private final StringBuilder parameterTypes = new StringBuilder();

        private LogEntry entry() {
            return (LogEntry) Proxy.newProxyInstance(
                    LogEntry.class.getClassLoader(),
                    new Class<?>[]{LogEntry.class},
                    this);
        }

        private String text() {
            return builder.toString();
        }

        private String parameterTypes() {
            return parameterTypes.toString();
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) {
            if ("toString".equals(method.getName())) {
                return builder.toString();
            }

            if (!"append".equals(method.getName()) || args == null) {
                throw new UnsupportedOperationException(method.toString());
            }

            if (args.length == 1) {
                parameterTypes.append('[').append(method.getParameterTypes()[0].getSimpleName()).append(']');
            }

            if (args.length == 3) {
                final String value = (String) args[0];
                final int start = (Integer) args[1];
                final int end = (Integer) args[2];
                builder.append(value, start, end);
            } else if (args.length == 1) {
                builder.append(args[0] == null ? "null" : args[0].toString());
            } else {
                throw new UnsupportedOperationException(method.toString());
            }

            return proxy;
        }

    }

}
