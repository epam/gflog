package com.epam.deltix.gflog.jul;

import com.epam.deltix.gflog.api.LogDebug;

import java.util.logging.LogManager;


public final class JulBridge {

    private JulBridge() {
    }

    public static void install() {
        System.setProperty("java.util.logging.manager", "deltix.gflog.jul.JulBridgeManager");

        if (!isInstalled()) {
            LogDebug.warn("can't install Java Util Logging -> GF Log bridge because LogManager is already initialized");
        }
    }

    public static boolean isInstalled() {
        final LogManager manager = LogManager.getLogManager();
        return manager instanceof JulBridgeManager;
    }

    @Deprecated
    public static void uninstall() {
    }

}
