package com.epam.deltix.gflog.core.appender;

import com.epam.deltix.gflog.core.util.Factory;


public interface AppenderFactory extends Factory<Appender> {

    Appender create();

}
