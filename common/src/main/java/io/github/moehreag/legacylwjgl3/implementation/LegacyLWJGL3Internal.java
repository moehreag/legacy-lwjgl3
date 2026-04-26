package io.github.moehreag.legacylwjgl3.implementation;

import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("unused")
@ApiStatus.Internal
public final class LegacyLWJGL3Internal {
	public static void renderIMEOverlay(int guiScale, int windowWidth, int windowHeight) {
		throw new UnsupportedOperationException("Implemented in Mixin");
	}

	public static String getClipboard() {
		throw new UnsupportedOperationException("Implemented in Mixin");
	}

	public static void setClipboard(String s) {
		throw new UnsupportedOperationException("Implemented in Mixin");
	}
}
