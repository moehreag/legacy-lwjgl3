package io.github.moehreag.legacylwjgl3;

import io.github.moehreag.legacylwjgl3.implementation.LegacyLWJGL3Internal;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegacyLWJGL3 implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("LegacyLWJGL3");
	public static final boolean USE_SDL = Boolean.getBoolean("legacy_lwjgl3.use_sdl") || System.getenv("LEGACY_LWJGL3_USE_SDL") != null;

	@Override
	public void onInitializeClient() {
		if (USE_SDL) {
			LOGGER.info("Using SDL3 for window & input handling instead of GLFW!");
		}
	}

	public static String getClipboard() {
		return LegacyLWJGL3Internal.getClipboard();
	}

	public static void setClipboard(String s) {
		LegacyLWJGL3Internal.setClipboard(s);
	}
}
