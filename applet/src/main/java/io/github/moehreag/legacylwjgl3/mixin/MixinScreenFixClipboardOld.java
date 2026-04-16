package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import io.github.moehreag.legacylwjgl3.annotations.Public;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@SuppressWarnings({"unused", "UnusedMixin", "MissingUnique"})
@Mixin(Screen.class)
public class MixinScreenFixClipboardOld {

	/**
	 * @author moehreag
	 * @reason Fix clipboard access with GLFW
	 */
	@Overwrite
	public static String getClipboard() {
		return LegacyLWJGL3.getClipboard();
	}

	@Public
	private static void setClipboard(String string) {
		LegacyLWJGL3.setClipboard(string);
	}
}
