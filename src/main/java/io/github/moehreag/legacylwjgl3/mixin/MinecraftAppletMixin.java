package io.github.moehreag.legacylwjgl3.mixin;

import java.applet.Applet;

import io.github.moehreag.legacylwjgl3.CrashReport;
import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import net.minecraft.class_447;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MinecraftApplet;
import net.minecraft.client.util.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MinecraftApplet.class, priority = 1100)
public abstract class MinecraftAppletMixin extends Applet {

	@Shadow
	public abstract void destroy();

	@Shadow
	public abstract void method_2155();

	@Inject(method = "init", remap = false, at = @At("HEAD"), cancellable = true)
	private void onAppletInit(CallbackInfo ci){
		LegacyLWJGL3.LOGGER.info("Creating GLFW window from Applet!");
		ci.cancel();
		Applet launcher = ((Applet) this.getParent());

		boolean var1 = false;
		if (this.getParameter("fullscreen") != null) {
			var1 = this.getParameter("fullscreen").equalsIgnoreCase("true");
		}

		Minecraft minecraft = new Minecraft(null, null, null, 854, 480, var1) {
			@Override
			public void method_2102(class_447 crashSummary) {
				CrashReport.report(crashSummary);
			}
		};

		if (this.getParameter("username") != null && this.getParameter("sessionid") != null) {
			minecraft.session = new Session(this.getParameter("username"), this.getParameter("sessionid"));
			System.out.println("Setting user: " + minecraft.session.username + ", " + minecraft.session.sessionId);
			if (this.getParameter("mppass") != null) {
				minecraft.session.mpPass = this.getParameter("mppass");
			}
		} else {
			minecraft.session = new Session("Player", "");
		}

		if (this.getParameter("server") != null && this.getParameter("port") != null) {
			minecraft.method_2117(this.getParameter("server"), Integer.parseInt(this.getParameter("port")));
		}
		minecraft.paused = !"true".equals(this.getParameter("stand-alone"));
		launcher.setVisible(false);
		launcher.stop();
		launcher.destroy();
		method_2155();
		setStub(null);
		launcher.removeAll();
		launcher.setSize(0, 0);

		Thread.currentThread().setName("Minecraft Main Thread");
		minecraft.run();
		System.exit(0);
	}

}
