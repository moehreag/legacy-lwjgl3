package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.implementation.LegacyLWJGL3RenderHelper;
import net.minecraft.client.gui.GuiElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LegacyLWJGL3RenderHelper.class)
public abstract class LegacyLWJGL3RenderHelperFillMixin {
	/**
	 * @author moehreag
	 * @reason implement cross-version access
	 */
	@Overwrite
	public static void fill(int x1, int y1, int x2, int y2, int color) {
		GuiElement.fill(x1, y1, x2, y2, color);
	}
}
