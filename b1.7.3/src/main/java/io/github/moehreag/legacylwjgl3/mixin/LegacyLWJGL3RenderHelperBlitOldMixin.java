package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.implementation.LegacyLWJGL3RenderHelper;
import net.minecraft.client.render.vertex.Tesselator;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@SuppressWarnings({"unused", "UnusedMixin"})
@Mixin(LegacyLWJGL3RenderHelper.class)
public abstract class LegacyLWJGL3RenderHelperBlitOldMixin {

	/**
	 * @author moehreag
	 * @reason implement cross-version access
	 */
	@Overwrite
	public static void blit(int texWidth, int texHeight, int u, int v, int x, int y, int width, int height) {
		GL11.glColor3f(1, 1, 1);
		Tesselator tesselator = Tesselator.INSTANCE;
		tesselator.begin();
		tesselator.texture((float) u / texWidth, (float) (v + height) / texHeight);
		tesselator.vertex(x, y + height, 0.0F);
		tesselator.texture((float) (u + width) / texWidth, (float) (v + height) / texHeight);
		tesselator.vertex(x + width, y + height, 0.0F);
		tesselator.texture((float) (u + width) / texWidth, (float) v / texHeight);
		tesselator.vertex(x + width, y, 0.0F);
		tesselator.texture((float) u / texWidth, (float) v / texHeight);
		tesselator.vertex(x, y, 0.0F);
		tesselator.end();
		GL11.glColor3f(1, 1, 1);
	}
}
