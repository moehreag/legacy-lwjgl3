package io.github.moehreag.legacylwjgl3;

import java.util.Objects;

import net.fabricmc.api.ClientModInitializer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.sdl.SDLClipboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegacyLWJGL3 implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("LegacyLWJGL3");
	public static final boolean USE_SDL = Boolean.getBoolean("legacy_lwjgl3.use_sdl") || System.getenv("LEGACY_LWJGL3_USE_SDL") != null;
	public static long GLFW_WINDOW = -1L;

	@Override
	public void onInitializeClient() {
		if (USE_SDL) {
			LOGGER.info("Using SDL3 for window & input handling instead of GLFW!");
		}
	}

	public static String getClipboard() {
		return Objects.requireNonNullElse(USE_SDL ? SDLClipboard.SDL_GetClipboardText() : GLFW.glfwGetClipboardString(GLFW_WINDOW), "");
	}

	public static void setClipboard(String s) {
		if (USE_SDL) {
			SDLClipboard.SDL_SetClipboardText(s);
		} else {
			GLFW.glfwSetClipboardString(GLFW_WINDOW, s);
		}
	}
}
