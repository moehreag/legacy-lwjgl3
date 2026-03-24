package io.github.moehreag.legacylwjgl3.mixin;

import java.awt.*;

import io.github.moehreag.legacylwjgl3.CrashReport;
import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Session;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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

    /*@Redirect(method = "updateWindow", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;" +
        "fullscreen:Z"))
    private boolean noFullscreenCheckForResize(Minecraft instance) {
        return GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WIN32 && fullscreen;
    }*/

	@Shadow
	public Canvas canvas;

	@Shadow
	protected abstract void resize(int width, int height);

	// this makes optifine happy
	@Inject(method = "init", at = @At("TAIL"))
	private void forceUpdateScreenSize(CallbackInfo ci) {
		GLFW.glfwPollEvents();
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

	/*@Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;logGlError(Ljava/lang/String;)V", ordinal = 1))
	private void checkResize(CallbackInfo ci) {
		if (Display.wasResized() || width != Display.getWidth() || height != Display.getHeight()) {
			this.onResolutionChanged(Display.getWidth(), Display.getHeight());
		}
	}*/

	@Inject(method = "run", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;canvas:Ljava/awt/Canvas;", remap = false, ordinal = 1))
	private void resizeCallback(CallbackInfo ci) {
		if ((Display.getWidth() != this.width || Display.getHeight() != this.height)) {
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

	@ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;setTitle(Ljava/lang/String;)V", remap = false))
	private @NotNull String correctTitle(@NotNull String title) {
		return title.substring("Minecraft ".length());
	}

	@Inject(method = "startAndConnect", at = @At("HEAD"), cancellable = true)
	private static void m_6868991(String userName, String sessionId, String serverAddress, CallbackInfo ci) {
		ci.cancel();
		LegacyLWJGL3.LOGGER.info("Creating GLFW window!");
		Minecraft minecraft = new Minecraft(null, null, null, Display.getWidth(), Display.getHeight(), false) {
			@Override
			public void handleCrash(net.minecraft.util.crash.CrashReport crashSummary) {
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
			minecraft.setStartupServer(address[0], Integer.parseInt(address[1]));
		}
		new Thread(minecraft, "Minecraft Main Thread").start();
	}

	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;canvas:Ljava/awt/Canvas;"))
	private void noCanvas(Minecraft instance, Canvas value) {
	}

	@Inject(method = "init", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;canvas:Ljava/awt/Canvas;", ordinal = 0))
	private void setCanvasNull(CallbackInfo ci) {
		if (canvas != null) {
			canvas.setVisible(false);
		}
		canvas = null;
	}

	@Redirect(method = "run", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;isActive()Z", remap = false))
	private boolean noUnFullscreenOnUnfocus() {
		return true;
	}

	@Inject(method = "initLicenseCheckThread", at = @At("HEAD"), cancellable = true)
	private void killHttpRequestToDeadUrl(CallbackInfo ci) {
		ci.cancel();
	}
}
