package io.github.moehreag.legacylwjgl3.implementation;

import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("unused")
@ApiStatus.Internal
public class LegacyLWJGL3RenderHelper {
	public static void blitPreeditBackground(int texWidth, int texHeight, int u, int v, int x, int y, int width, int height) {
		throw new UnsupportedOperationException("Implemented in Mixin");
	}

	public static void blit(int texWidth, int texHeight, int u, int v, int x, int y, int width, int height) {
		throw new UnsupportedOperationException("Implemented in Mixin");
	}

	public static void drawString(String s, int x, int y, int color) {
		throw new UnsupportedOperationException("Implemented in Mixin");
	}

	public static void fill(int x1, int y1, int x2, int y2, int color) {
		throw new UnsupportedOperationException("Implemented in Mixin");
	}

	public static void pushMatrix() {
		throw new UnsupportedOperationException("Implemented in Mixin");
	}

	public static void translate(float x, float y, float z) {
		throw new UnsupportedOperationException("Implemented in Mixin");
	}

	public static void popMatrix() {
		throw new UnsupportedOperationException("Implemented in Mixin");
	}
}
