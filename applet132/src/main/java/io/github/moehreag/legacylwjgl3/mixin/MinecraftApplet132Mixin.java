package io.github.moehreag.legacylwjgl3.mixin;

import java.applet.Applet;

import io.github.moehreag.legacylwjgl3.CrashReport;
import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MinecraftApplet;
import net.minecraft.client.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"removal", "UnusedMixin"})
@Mixin(value = MinecraftApplet.class, priority = 1100)
public abstract class MinecraftApplet132Mixin extends Applet {

	@Inject(method = "init", remap = false, at = @At("HEAD"), cancellable = true)
	private void onAppletInit(CallbackInfo ci) {
		LegacyLWJGL3.LOGGER.info("Creating window from Applet!");
		ci.cancel();
		Applet launcher = ((Applet) this.getParent());

		boolean var1 = false;
		if (this.getParameter("fullscreen") != null) {
			var1 = this.getParameter("fullscreen").equalsIgnoreCase("true");
		}

		Minecraft minecraft = new Minecraft(null, null, 854, 480, var1) {
			public void handleCrash(net.minecraft.util.crash.CrashReport crashSummary) {
				CrashReport.report(crashSummary);
			}
		};

		if (this.getParameter("username") != null && this.getParameter("sessionid") != null) {
			minecraft.session = new Session(this.getParameter("username"), this.getParameter("sessionid"));
			System.out.println("Setting user: " + minecraft.session.username + ", " + minecraft.session.id);
		} else {
			minecraft.session = new Session("Player", "");
		}

		if (this.getParameter("server") != null && this.getParameter("port") != null) {
			minecraft.setStartupServer(this.getParameter("server"), Integer.parseInt(this.getParameter("port")));
		}
		minecraft.appletMode = !"true".equals(this.getParameter("stand-alone"));
		launcher.setVisible(false);
		launcher.stop();
		launcher.destroy();
		setStub(null);
		launcher.removeAll();
		launcher.setSize(0, 0);

		Thread.currentThread().setName("Minecraft Main Thread");
		@SuppressWarnings("UnnecessaryLocalVariable") Runnable r = minecraft; // ensures that the `run` call doesn't get obfuscated later
		r.run();
		System.exit(0);
	}

}