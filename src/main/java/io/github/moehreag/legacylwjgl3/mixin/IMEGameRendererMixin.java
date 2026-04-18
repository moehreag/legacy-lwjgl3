package io.github.moehreag.legacylwjgl3.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.moehreag.legacylwjgl3.implementation.LegacyLWJGL3Internal;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class IMEGameRendererMixin {
	@Inject(method = "render(FJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(IIF)V", shift = At.Shift.AFTER))
	private void renderIME(float tickDelta, long startTime, CallbackInfo ci, @Local Window window) {
		LegacyLWJGL3Internal.renderIMEOverlay(window.getScale(), window.getWidth(), window.getHeight());
	}
}
