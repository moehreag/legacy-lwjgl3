package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.implementation.LegacyLWJGL3RenderHelper;
import net.minecraft.client.gui.GuiElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings({"unused", "UnusedMixin"})
@Mixin(LegacyLWJGL3RenderHelper.class)
public class LegacyLWJGL3RenderHelperFillOldMixin {
	@Unique
	private static final Element ELEMENT = new Element();

	private static class Element extends GuiElement {
		@Override
		public void fill(int x1, int y1, int x2, int y2, int color) {
			super.fill(x1, y1, x2, y2, color);
		}
	}

	/**
	 * @author moehreag
	 * @reason implement cross-version access
	 */
	@Overwrite
	public static void fill(int x1, int y1, int x2, int y2, int color) {
		ELEMENT.fill(x1, y1, x2, y2, color);
	}
}
