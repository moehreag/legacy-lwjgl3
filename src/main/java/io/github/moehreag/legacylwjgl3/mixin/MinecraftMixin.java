package io.github.moehreag.legacylwjgl3.mixin;

import java.awt.*;

import io.github.moehreag.legacylwjgl3.CrashReport;
import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Session;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

	@Shadow
	public Canvas canvas;

	@Shadow private boolean fullscreen;

	@Shadow public int width;

	@Shadow public int height;

	@Shadow protected abstract void onResolutionChanged(int width, int height);

	@Shadow private String serverAddress;

	@Inject(method = "main", at = @At("HEAD"), cancellable = true)
	private static void m_6868991(String[] args, CallbackInfo ci) {
		ci.cancel();
		String userName = "Player" + Minecraft.getTime() % 1000L;
		if (args.length > 0) {
			userName = args[0];
		}
		String sessionId = "-";
		if (args.length > 1) {
			sessionId = args[1];
		}
		String server = null;
		String port = null;
		for (int i = 2;i<args.length-1;i++){
			if ("server".equals(args[i])){
				server = args[i+1];
			}
			if ("port".equals(args[i])){
				port = args[i+1];
			}
		}
		String serverAddress = null;
		if (server != null){
			if (port != null){
				serverAddress = server+":"+port;
			} else {
				serverAddress = server+":25565";
			}
		}
		LegacyLWJGL3.LOGGER.info("Creating GLFW window!");
		Minecraft minecraft = new Minecraft(null, null, Display.getWidth(), Display.getHeight(), false) {
			@Override
			public void printCrashReport(net.minecraft.util.crash.CrashReport crashSummary) {
				CrashReport.report(crashSummary);
			}
		};
		if (userName != null && sessionId != null) {
			minecraft.session = new Session(userName, sessionId);
		} else {
			minecraft.session = new Session("Player" + System.currentTimeMillis() % 1000L, "");
		}
		if (serverAddress != null) {
			String[] address = serverAddress.split(":");
			minecraft.setServerAddressAndPort(address[0], Integer.parseInt(address[1]));
		}
		Thread.currentThread().setName("Minecraft Main Thread");
		minecraft.run();
		Runtime.getRuntime().addShutdownHook(new Thread(Minecraft::shutdown));
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

	@ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;setTitle(Ljava/lang/String;)V", remap = false))
	private @NotNull String correctTitle(@NotNull String title){
		return title.substring("Minecraft ".length());
	}

	@Inject(method = "runGame", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;canvas:Ljava/awt/Canvas;", remap = false, ordinal = 1))
	private void resizeCallback(CallbackInfo ci){
		if ((Display.getWidth() != this.width || Display.getHeight() != this.height)) {
			this.width = Display.getWidth();
			this.height = Display.getHeight();
			if (this.width <= 0) {
				this.width = 1;
			}

			if (this.height <= 0) {
				this.height = 1;
			}

			this.onResolutionChanged(this.width, this.height);
			Display.update();
		}
	}
}
