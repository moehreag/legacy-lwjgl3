package io.github.moehreag.legacylwjgl3.mixin;

import java.util.concurrent.Callable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.util.crash.CrashReportCategory;
import org.lwjgl.opengl.GL;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public abstract class MixinMinecraftFixEarlyCrashNoReports {

	@WrapOperation(method = "populateCrashReport", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/crash/CrashReportCategory;add(Ljava/lang/String;Ljava/util/concurrent/Callable;)V", ordinal = 2))
	private void fixOGLCrashReportInfo(CrashReportCategory instance, String key, Callable<String> value, Operation<Void> original) {
		try {
			GL.getCapabilities();
			original.call(instance, key, value);
		} catch (IllegalStateException ignored) {
		}
	}
}
