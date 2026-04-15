package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.util.OS;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraftFixResize {

	@Shadow
	private boolean fullscreen;

	@Shadow
	public int width;

	@Shadow
	public int height;

	@Shadow
	protected abstract void resize(int width, int height);

	@Redirect(method = "updateWindow", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;" +
			"fullscreen:Z", opcode = Opcodes.GETFIELD))
	private boolean noFullscreenCheckForResize(Minecraft instance) {
		return OS.current() == OS.WINDOWS && fullscreen;
	}

	// this makes optifine happy
	@Inject(method = "init", at = @At("TAIL"))
	private void forceUpdateScreenSize(CallbackInfo ci) {
		this.width = Display.getWidth();
		this.height = Display.getHeight();
		if (this.width <= 0) {
			this.width = 1;
		}

		if (this.height <= 0) {
			this.height = 1;
		}

		this.resize(this.width, this.height);
	}
}
