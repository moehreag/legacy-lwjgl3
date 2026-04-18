package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.implementation.LegacyLWJGL3RenderHelper;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LegacyLWJGL3RenderHelper.class)
public abstract class LegacyLWJGL3RenderHelperBlitMixin {
	/**
	 * @author moehreag
	 * @reason implement cross-version access
	 */
	@Overwrite
	public static void blit(int texWidth, int texHeight, int u, int v, int x, int y, int width, int height) {
		GlStateManager.color3f(1, 1, 1);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuffer();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(x, y + height, 0.0F).texture((float) u / texWidth, (float) (v + height) / texHeight).nextVertex();
		bufferBuilder.vertex(x + width, y + height, 0.0F).texture((float) (u + width) / texWidth, (float) (v + height) / texHeight).nextVertex();
		bufferBuilder.vertex(x + width, y, 0.0F).texture((float) (u + width) / texWidth, (float) v / texHeight).nextVertex();
		bufferBuilder.vertex(x, y, 0.0F).texture((float) u / texWidth, (float) v / texHeight).nextVertex();
		tesselator.end();
		GlStateManager.color3f(1, 1, 1);
	}
}
