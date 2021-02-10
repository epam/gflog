package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.api.LogLevel;
import com.epam.deltix.gflog.core.LogRecordBean;
import com.epam.deltix.gflog.core.appender.Appender;
import com.epam.deltix.gflog.core.util.Util;
import org.junit.Test;
import org.mockito.Mockito;


public class LogProcessorTest {

    private final Appender appender1 = Mockito.mock(Appender.class);
    private final Appender appender2 = Mockito.mock(Appender.class);
    private final Appender appender3 = Mockito.mock(Appender.class);

    private final LogProcessor processor = new LogProcessor(
            new Appender[]{appender1, appender2, appender3}
    );

    private final LogRecordBean record = new LogRecordBean();

    public LogProcessorTest() {
        record.setTimestamp(0);
        record.setAppenderMask(~0);
        record.setThreadName(Util.fromUtf8String("main"));
        record.setMessage(Util.fromUtf8String("message"));
        record.setLogName(Util.fromUtf8String("logger"));
        record.setLogLevel(LogLevel.INFO);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldCloseOnlyOpenAppendersWhenFailedToOpenOneOfAppendersAndRethrowException() throws Exception {
        Mockito.doThrow(IllegalStateException.class).when(appender2).open();

        try {
            processor.open();
        } catch (final Exception e) {
            Mockito.verify(appender1, Mockito.times(1)).open();
            Mockito.verify(appender1, Mockito.times(1)).close();

            Mockito.verify(appender2, Mockito.times(1)).open();
            Mockito.verify(appender2, Mockito.times(0)).close();

            Mockito.verifyNoInteractions(appender3);
            throw e;
        }
    }

    @Test
    public void shouldCallAllAppendersWhenProcessing() throws Exception {
        processor.process(record);

        Mockito.verify(appender1).append(Mockito.eq(record));
        Mockito.verify(appender2).append(Mockito.eq(record));
        Mockito.verify(appender3).append(Mockito.eq(record));
    }

    @Test
    public void shouldCallFirstAndThirdWhenProcessing() throws Exception {
        Mockito.doThrow(IllegalStateException.class).when(appender2).append(Mockito.any());

        processor.process(record);
        processor.process(record);

        Mockito.verify(appender1, Mockito.times(2)).append(Mockito.eq(record));
        Mockito.verify(appender2, Mockito.times(1)).append(Mockito.eq(record));
        Mockito.verify(appender3, Mockito.times(2)).append(Mockito.eq(record));
    }

    @Test
    public void shouldCallAllAppendersWhenFlushing() throws Exception {
        processor.flush();

        Mockito.verify(appender1).flush();
        Mockito.verify(appender2).flush();
        Mockito.verify(appender3).flush();
    }

    @Test
    public void shouldCallFirstAndThirdWhenFlushing() throws Exception {
        Mockito.doThrow(IllegalStateException.class).when(appender2).flush();

        processor.flush();
        processor.flush();

        Mockito.verify(appender1, Mockito.times(2)).flush();
        Mockito.verify(appender2, Mockito.times(1)).flush();
        Mockito.verify(appender3, Mockito.times(2)).flush();
    }

}
