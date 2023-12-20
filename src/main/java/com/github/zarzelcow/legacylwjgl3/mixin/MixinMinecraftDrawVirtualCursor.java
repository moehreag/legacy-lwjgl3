package com.github.zarzelcow.legacylwjgl3.mixin;

import com.github.zarzelcow.legacylwjgl3.implementation.glfw.VirtualGLFWMouseImplementation;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraftDrawVirtualCursor {

	@Inject(method = "runGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ToastGui;tick()V"))
	private void drawVirtualCursor(CallbackInfo ci) {
		VirtualGLFWMouseImplementation.render();
	}
}
