package io.github.moehreag.legacylwjgl3.mixin;

import net.minecraft.client.resource.ResourceDownloadThread;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResourceDownloadThread.class)
public abstract class MixinResourceDownloadThread {

	@Shadow public abstract void m_5855120();

	@Inject(method = "run", at = @At("HEAD"), cancellable = true)
	private void noResourceLoading(CallbackInfo ci){
		ci.cancel();
		m_5855120();
	}
}
