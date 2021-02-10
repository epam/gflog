package com.epam.deltix.gflog.core.service;

import com.epam.deltix.gflog.core.util.Buffer;
import com.epam.deltix.gflog.core.util.Util;

import java.util.Arrays;


final class LogIndex {

    private Buffer[] map = new Buffer[64];

    void put(final String name, final int index) {
        Buffer[] map = this.map;

        if (index >= map.length) {
            map = Arrays.copyOf(this.map, index << 1);
            Util.UNSAFE.storeFence();
            this.map = map;
        }

        map[index] = Util.fromUtf8String(name, Byte.MAX_VALUE);
    }

    Buffer get(final int index) {
        return map[index];
    }

}
