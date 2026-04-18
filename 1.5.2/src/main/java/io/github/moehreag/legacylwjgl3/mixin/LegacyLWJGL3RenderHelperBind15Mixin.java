package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.implementation.LegacyLWJGL3RenderHelper;
import net.ornithemc.osl.lifecycle.impl.client.MinecraftAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@SuppressWarnings({"unused", "UnusedMixin"})
@Mixin(LegacyLWJGL3RenderHelper.class)
public abstract class LegacyLWJGL3RenderHelperBind15Mixin {
	/**
	 * @author moehreag
	 * @reason implement cross-version access
	 */
	@Overwrite
	public static void blitPreeditBackground(int texWidth, int texHeight, int u, int v, int x, int y, int width, int height) {
		MinecraftAccess.getInstance().textureManager.bind("/assets/legacy-lwjgl3/textures/preedit.png");
		LegacyLWJGL3RenderHelper.blit(texWidth, texHeight, u, v, x, y, width, height);
	}
}
