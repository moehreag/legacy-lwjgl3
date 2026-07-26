package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.client.gui.screen.Screen;

import java.net.URI;

@SuppressWarnings({"UnusedMixin", "unused"})
@Mixin(Screen.class)
public class MixinScreenFixOpenLink {
	/**
	 * @author rdh
	 * @reason fix link opening with GLFW
	 */
	@Overwrite
	public void openLink(URI uri) {
		LegacyLWJGL3.openLink(uri.toString());
	}
}
