package io.github.moehreag.legacylwjgl3.util;

import java.lang.invoke.MethodHandles;

import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.opengl.DisplayMode;

public class OptifineReflector {

    public static void setOFConfigDisplayMode(DisplayMode mode) {
        if (!FabricLoader.getInstance().isModLoaded("optifabric")) {
            return;
        }
        try {
            var clazz = Class.forName("Config");
            var handle = MethodHandles.lookup().findStaticSetter(clazz, "desktopDisplayMode", DisplayMode.class);
            handle.invoke(mode);
        } catch (Throwable ignored) {

        }
    }
}
