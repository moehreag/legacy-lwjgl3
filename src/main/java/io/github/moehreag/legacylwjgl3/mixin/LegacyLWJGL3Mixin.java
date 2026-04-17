package io.github.moehreag.legacylwjgl3.mixin;

import java.util.Objects;

import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.Display;
import org.lwjgl.sdl.SDLClipboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import static io.github.moehreag.legacylwjgl3.LegacyLWJGL3.USE_SDL;

@Mixin(LegacyLWJGL3.class)
public abstract class LegacyLWJGL3Mixin {

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
}
