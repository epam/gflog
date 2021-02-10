package com.epam.deltix.gflog.core.layout.template;

import com.epam.deltix.gflog.core.LogRecord;
import com.epam.deltix.gflog.core.util.MutableBuffer;


public abstract class Template {

    public abstract int size(final LogRecord record);

    public abstract int format(final LogRecord record, final MutableBuffer buffer, final int offset);

}
