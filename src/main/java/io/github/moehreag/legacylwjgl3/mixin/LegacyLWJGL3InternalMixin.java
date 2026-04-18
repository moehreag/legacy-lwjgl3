package io.github.moehreag.legacylwjgl3.mixin;

import java.util.Objects;

import io.github.moehreag.legacylwjgl3.implementation.LegacyLWJGL3Internal;
import io.github.moehreag.legacylwjgl3.util.IMEManager;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.Display;
import org.lwjgl.sdl.SDLClipboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import static io.github.moehreag.legacylwjgl3.LegacyLWJGL3.USE_SDL;

@Mixin(LegacyLWJGL3Internal.class)
public abstract class LegacyLWJGL3InternalMixin {

	/**
	 * @author moehreag
	 * @reason implement clipboard access
	 */
	@Overwrite
	public static String getClipboard() {
		return Objects.requireNonNullElse(USE_SDL ? SDLClipboard.SDL_GetClipboardText() : GLFW.glfwGetClipboardString(Display.getHandle()), "");
	}

	/**
	 * @author moehreag
	 * @reason implement clipboard access
	 */
	@Overwrite
	public static void setClipboard(String s) {
		if (USE_SDL) {
			SDLClipboard.SDL_SetClipboardText(s);
		} else {
			GLFW.glfwSetClipboardString(Display.getHandle(), s);
		}
	}

	/**
	 * @author moehreag
	 * @reason implement IME
	 */
	@Overwrite
	public static void renderIMEOverlay(int guiScale, int windowWidth, int windowHeight) {
		IMEManager.getInstance().renderPreeditOverlay(guiScale, windowWidth, windowHeight);
	}
}
