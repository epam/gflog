package com.epam.deltix.gflog.core.appender;

import com.epam.deltix.gflog.core.layout.Layout;
import com.epam.deltix.gflog.core.layout.TemplateLayoutFactory;
import com.epam.deltix.gflog.core.util.PropertyUtil;


public abstract class NioAppenderFactory extends AbstactAppenderFactory {

    protected static final int BUFFER_CAPACITY = PropertyUtil.getMemory("gflog.appender.buffer.capacity", 2 * 1024 * 1024);

    protected int bufferCapacity;
    protected int flushCapacity;
    protected Layout layout;

    public NioAppenderFactory(final String defaultName) {
        super(defaultName);
    }

    public int getBufferCapacity() {
        return bufferCapacity;
    }

    public void setBufferCapacity(final int bufferCapacity) {
        this.bufferCapacity = bufferCapacity;
    }

    public int getFlushCapacity() {
        return flushCapacity;
    }

    public void setFlushCapacity(final int flushCapacity) {
        this.flushCapacity = flushCapacity;
    }

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(final Layout layout) {
        this.layout = layout;
    }

    @Override
    protected void conclude() {
        super.conclude();

        if (bufferCapacity == 0) {
            bufferCapacity = BUFFER_CAPACITY;
        }

        if (flushCapacity == 0) {
            flushCapacity = bufferCapacity;
        }

        if (flushCapacity > bufferCapacity) {
            flushCapacity = bufferCapacity;
        }

        if (layout == null) {
            layout = new TemplateLayoutFactory().create();
        }
    }

}
