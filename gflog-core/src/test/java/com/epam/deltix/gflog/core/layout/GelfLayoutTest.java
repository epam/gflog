package com.epam.deltix.gflog.core.layout;

import com.epam.deltix.gflog.api.LogLevel;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;


public class GelfLayoutTest extends AbstractLayoutTest {

    protected static final Map<String, String> ADDITIONAL_FIELDS = new HashMap<>();

    static {
        ADDITIONAL_FIELDS.put("container", "k8.\"k8\"");
        ADDITIONAL_FIELDS.put("healthy", "yep");
    }

    public GelfLayoutTest() {
        super(new GelfLayout("localhost", ADDITIONAL_FIELDS, 0));
    }

    @Test
    public void testSimple() {
        verify(LogLevel.TRACE, 0, "Hey",
                "{" +
                        "\"version\":\"1.1\"," +
                        "\"level\":7," +
                        "\"host\":\"localhost\"," +
                        "\"_container\":\"k8.\\\"k8\\\"\"," +
                        "\"_healthy\":\"yep\"," +
                        "\"short_message\":\"Hey\"," +
                        "\"_logger\":\"java.util.Object\"," +
                        "\"_thread\":\"main\"," +
                        "\"_sequence\":0," +
                        "\"timestamp\":0.000000000" +
                        "}"
        );

        verify(LogLevel.DEBUG, -1, "Deltix",
                "{" +
                        "\"version\":\"1.1\"," +
                        "\"level\":7," +
                        "\"host\":\"localhost\"," +
                        "\"_container\":\"k8.\\\"k8\\\"\"," +
                        "\"_healthy\":\"yep\"," +
                        "\"short_message\":\"Deltix\"," +
                        "\"_logger\":\"java.util.Object\"," +
                        "\"_thread\":\"main\"," +
                        "\"_sequence\":1," +
                        "\"timestamp\":-0.000000001" +
                        "}"
        );

        verify(LogLevel.INFO, 1234567890L, "\"\\/\b\f\n\r\t",
                "{" +
                        "\"version\":\"1.1\"," +
                        "\"level\":6," +
                        "\"host\":\"localhost\"," +
                        "\"_container\":\"k8.\\\"k8\\\"\"," +
                        "\"_healthy\":\"yep\"," +
                        "\"short_message\":\"\\\"\\\\\\/\\b\\f\\n\\r\\t\"," +
                        "\"_logger\":\"java.util.Object\"," +
                        "\"_thread\":\"main\"," +
                        "\"_sequence\":2," +
                        "\"timestamp\":1.234567890" +
                        "}"
        );

        verify(LogLevel.WARN, 1234567890L, "0\"1\\2/3\b4\f5\n6\r7\t8",
                "{" +
                        "\"version\":\"1.1\"," +
                        "\"level\":4," +
                        "\"host\":\"localhost\"," +
                        "\"_container\":\"k8.\\\"k8\\\"\"," +
                        "\"_healthy\":\"yep\"," +
                        "\"short_message\":\"0\\\"1\\\\2\\/3\\b4\\f5\\n6\\r7\\t8\"," +
                        "\"_logger\":\"java.util.Object\"," +
                        "\"_thread\":\"main\"," +
                        "\"_sequence\":3," +
                        "\"timestamp\":1.234567890" +
                        "}"
        );
    }

    @Override
    protected void verify(final LogLevel level, final long timestamp, final String message, final String expected) {
        super.verify(level, timestamp, message, expected + "\u0000");
    }

}
