package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.implementation.LegacyLWJGL3RenderHelper;
import net.minecraft.resource.Identifier;
import net.ornithemc.osl.lifecycle.impl.client.MinecraftAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LegacyLWJGL3RenderHelper.class)
public abstract class LegacyLWJGL3RenderHelperBindMixin {
	@Unique
	private static final Identifier BACKGROUND = new Identifier("legacy-lwjgl3", "textures/preedit.png");

	/**
	 * @author moehreag
	 * @reason implement cross-version access
	 */
	@Overwrite
	public static void blitPreeditBackground(int texWidth, int texHeight, int u, int v, int x, int y, int width, int height) {
		MinecraftAccess.getInstance().getTextureManager().bind(BACKGROUND);
		LegacyLWJGL3RenderHelper.blit(texWidth, texHeight, u, v, x, y, width, height);
	}
}
