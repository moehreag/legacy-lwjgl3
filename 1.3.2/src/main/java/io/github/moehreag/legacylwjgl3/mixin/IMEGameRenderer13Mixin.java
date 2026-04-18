package io.github.moehreag.legacylwjgl3.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.moehreag.legacylwjgl3.implementation.LegacyLWJGL3Internal;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"unused", "UnusedMixin"})
@Mixin(GameRenderer.class)
public abstract class IMEGameRenderer13Mixin {
	@Inject(method = "render(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(IIF)V", shift = At.Shift.AFTER))
	private void renderIME(float tickDelta, CallbackInfo ci, @Local Window window) {
		LegacyLWJGL3Internal.renderIMEOverlay(window.getScale(), window.getWidth(), window.getHeight());
	}
}
