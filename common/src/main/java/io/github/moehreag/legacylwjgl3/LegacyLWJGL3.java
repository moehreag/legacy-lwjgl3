package io.github.moehreag.legacylwjgl3;

import io.github.moehreag.legacylwjgl3.implementation.LegacyLWJGL3Internal;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegacyLWJGL3 implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("LegacyLWJGL3");
	public static final boolean USE_SDL = readBooleanOption("legacy_lwjgl3.use_sdl", "LEGACY_LWJGL3_USE_SDL", true);
	public static final boolean SCALE_FRAMEBUFFER = readBooleanOption("legacy_lwjgl3.scale_framebuffer", "LEGACY_LWJGL3_SCALE_FRAMEBUFFER", true);

	@Override
	public void onInitializeClient() {
		if (USE_SDL) {
			LOGGER.info("Using SDL3 for window & input handling!");
		} else {
			LOGGER.info("Using GLFW for window & input handling!");
		}
	}

	private static boolean readBooleanOption(String propertyKey, String envVarName, boolean defaultValue) {
		String property = System.getProperty(propertyKey);
		if (property != null) {
			return Boolean.parseBoolean(property);
		}
		if (System.getenv().containsKey(envVarName)) {
			String envVar = System.getenv(envVarName);
			return Boolean.parseBoolean(envVar) || "1".equals(envVar);
		}
		return defaultValue;
	}

	public static String getClipboard() {
		return LegacyLWJGL3Internal.getClipboard();
	}

	public static void setClipboard(String s) {
		LegacyLWJGL3Internal.setClipboard(s);
	}

	public static void openLink(String s) {
		LegacyLWJGL3Internal.openLink(s);
	}
}
