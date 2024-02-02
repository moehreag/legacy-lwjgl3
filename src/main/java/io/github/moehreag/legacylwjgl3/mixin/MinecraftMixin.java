package io.github.moehreag.legacylwjgl3.mixin;

import java.awt.*;

import io.github.moehreag.legacylwjgl3.CrashReport;
import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.crash.CrashSummary;
import net.minecraft.client.resource.ResourceDownloadThread;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

	@Shadow
	public Canvas canvas;

	/**
	 * @author moehreag
	 * @reason no applet thingies
	 */
	@Overwrite
	public static void m_6868991(String userName, String sessionId, String serverAddress) {
		LegacyLWJGL3.LOGGER.info("Creating GLFW window!");
		Minecraft minecraft = new Minecraft(null, null, null, Display.getWidth(), Display.getHeight(), false) {
			@Override
			public void printCrashReport(CrashSummary crashSummary) {
				CrashReport.report(crashSummary);
			}
		};
		new Thread(minecraft, "Minecraft Main Thread").start();
	}

	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;canvas:Ljava/awt/Canvas;"))
	private void noCanvas(Minecraft instance, Canvas value){
	}

	@Inject(method = "init", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;canvas:Ljava/awt/Canvas;", ordinal = 0))
	private void setCanvasNull(CallbackInfo ci){
		if (canvas != null){
			canvas.setVisible(false);
		}
		canvas = null;
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void canvas(CallbackInfo ci) {
	}

	// Because the url is dead anyway...
	@Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resource/ResourceDownloadThread;start()V"))
	private void noResourceLoading(ResourceDownloadThread instance){

	}

	@ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;setTitle(Ljava/lang/String;)V"))
	private @NotNull String correctTitle(@NotNull String title){
		return title.substring("Minecraft ".length());
	}
}
