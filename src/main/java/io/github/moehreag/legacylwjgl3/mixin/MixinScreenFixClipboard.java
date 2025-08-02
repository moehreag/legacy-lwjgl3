package io.github.moehreag.legacylwjgl3.mixin;

import java.util.Objects;

import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.sdl.SDLClipboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Screen.class)
public class MixinScreenFixClipboard {

	/**
	 * @author moehreag
	 * @reason Fix clipboard access with GLFW
	 */
	@Overwrite
	public static String getClipboard() {
		return Objects.requireNonNullElse(SDLClipboard.SDL_GetClipboardText(), "");
	}

	/**
	 * @author moehreag
	 * @reason Fix clipboard access with GLFW
	 */
	@Overwrite
	public static void setClipboard(String string) {
		SDLClipboard.SDL_SetClipboardText(string);
	}
}
