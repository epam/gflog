package com.epam.deltix.gflog.core.layout;

import com.epam.deltix.gflog.core.util.Factory;
import com.epam.deltix.gflog.core.util.PropertyUtil;

import java.time.ZoneId;


public class TemplateLayoutFactory implements Factory<Layout> {

    protected static final String TEMPLATE = PropertyUtil.getString("gflog.layout.template", "%d %p [%t] %m%n");

    protected String template;
    protected ZoneId zoneId;

    @Override
    public Layout create() {
        if (template == null) {
            template = TEMPLATE;
        }

        if (zoneId == null) {
            zoneId = ZoneId.systemDefault();
        }

        return new TemplateLayout(template, zoneId);
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(final String template) {
        this.template = template;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public void setZoneId(final ZoneId zoneId) {
        this.zoneId = zoneId;
    }

}
