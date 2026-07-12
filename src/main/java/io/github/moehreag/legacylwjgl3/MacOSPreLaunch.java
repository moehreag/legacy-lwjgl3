package io.github.moehreag.legacylwjgl3;

import io.github.moehreag.legacylwjgl3.util.OS;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class MacOSPreLaunch implements PreLaunchEntrypoint {
	@Override
	public void onPreLaunch() {
		if (OS.current() == OS.OSX) {
			// prevent AWT from taking over the AppKit event loop from GLFW/SDL
			System.setProperty("java.awt.headless", "true");
		}
	}
}
