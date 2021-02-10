package com.epam.deltix.gflog.core.layout;

import com.epam.deltix.gflog.api.LogDebug;
import com.epam.deltix.gflog.core.util.Factory;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;


public class GelfLayoutFactory implements Factory<GelfLayout> {

    protected Map<String, String> additionalFields;

    protected String host;

    public void setHost(final String host) {
        this.host = host;
    }

    public void setAdditionalFields(final Map<String, String> additionalFields) {
        this.additionalFields = additionalFields;
    }

    @Override
    public GelfLayout create() {
        if (additionalFields == null) {
            additionalFields = Collections.emptyMap();
        }

        return new GelfLayout(host == null ? getDefaultHost() : host, additionalFields, System.currentTimeMillis() * 1000);
    }

    protected static String getDefaultHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (final Throwable e) {
            LogDebug.warn("can't resolve local host");
            return "unknown";
        }
    }

}
