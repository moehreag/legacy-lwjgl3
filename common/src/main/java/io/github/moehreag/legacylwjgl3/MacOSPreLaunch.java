package io.github.moehreag.legacylwjgl3;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class MacOSPreLaunch implements PreLaunchEntrypoint {
	@Override
	public void onPreLaunch() {
		if (System.getProperty("os.name").startsWith("Mac")) {
			// prevent AWT from taking over the AppKit event loop from GLFW/SDL
			System.setProperty("java.awt.headless", "true");
		}
	}
}
