package com.epam.deltix.gflog.core.layout;

import com.epam.deltix.gflog.api.LogLevel;
import org.junit.Test;

import java.time.ZoneId;


public class TemplateLayoutTest extends AbstractLayoutTest {

    public TemplateLayoutTest() {
        super(new TemplateLayout("%d %m", ZoneId.of("UTC")));
    }

    @Test
    public void testSimple() {
        verify(LogLevel.INFO, 0, "Hey there!", "1970-01-01 00:00:00.000 Hey there!");
        verify(LogLevel.INFO, timestamp("2020-04-25 12:34:56.789123456"), "Hey there!", "2020-04-25 12:34:56.789 Hey there!");
    }

}
