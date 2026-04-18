package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.implementation.LegacyLWJGL3RenderHelper;
import net.ornithemc.osl.lifecycle.impl.client.MinecraftAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LegacyLWJGL3RenderHelper.class)
public abstract class LegacyLWJGL3RenderHelperDrawStringMixin {

	/**
	 * @author moehreag
	 * @reason implement cross-version access
	 */
	@Overwrite
	public static void drawString(String s, int x, int y, int color) {
		MinecraftAccess.getInstance().textRenderer.draw(s, x, y, color);
	}
}
