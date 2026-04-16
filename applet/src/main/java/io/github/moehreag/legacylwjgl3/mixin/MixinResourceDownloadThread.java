package io.github.moehreag.legacylwjgl3.mixin;

import net.minecraft.client.resource.ResourceDownloader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"unused", "UnusedMixin"})
@Mixin(ResourceDownloader.class)
public abstract class MixinResourceDownloadThread {

	@Shadow
	public abstract void forceReload();

	@Inject(method = "run", at = @At("HEAD"), cancellable = true)
	private void noResourceLoading(CallbackInfo ci){
		ci.cancel();
		forceReload();
	}
}