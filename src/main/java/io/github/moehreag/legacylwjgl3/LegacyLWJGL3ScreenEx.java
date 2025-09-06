package io.github.moehreag.legacylwjgl3;

import java.nio.file.Path;
import java.util.List;

import net.minecraft.client.Minecraft;

public interface LegacyLWJGL3ScreenEx {
	default void onFileDrop(List<Path> files) {

	}

	static void handleFileDrop(List<Path> files) {
		var screen = Minecraft.getInstance().screen;
		if (screen != null) {
			screen.onFileDrop(files);
		}
	}
}
