package com.mojang.blaze3d.platform;

import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.Sync;

public class GlStateManager {
	public static void color4f(float r, float g, float b, float a) {
		GL11.glColor4f(r, g, b, a);
	}

	public static void enableBlend() {
		GL11.glEnable(GL11.GL_BLEND);
	}

	public static void blendFuncSeparate(int sFactorRGB, int dFactorRGB, int sFactorAlpha, int dFactorAlpha) {
		GL14.glBlendFuncSeparate(sFactorRGB, dFactorRGB, sFactorAlpha, dFactorAlpha);
	}

	public static void blendFunc(int sFactor, int dFactor) {
		GL11.glBlendFunc(sFactor, dFactor);
	}

	public static void depthFunc(int i) {
		GL11.glDepthFunc(i);
	}

	public static void enableDepthTest() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	public static void disableTexture() {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}

	public static void disableBlend() {
		GL11.glEnable(GL11.GL_BLEND);
	}

	public static void enableTexture() {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public static void disableDepthTest() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	public static void disableAlphaTest() {
		GL11.glEnable(GL11.GL_ALPHA_TEST);
	}

	public static void shadeModel(int i) {
		GL11.glShadeModel(i);
	}

	public static void enableColorLogicOp() {
		GL11.glEnable(GL11.GL_LOGIC_OP);
	}

	public static void logicOp(int op) {
		GL11.glLogicOp(op);
	}

	public static void disableColorLogicOp() {
		GL11.glDisable(GL11.GL_LOGIC_OP);
	}

	public static void disableRescaleNormal() {
		GL11.glDisable(32826);
	}

	public static void disableLighting() {
		GL11.glDisable(GL11.GL_LIGHTING);
	}

	public static void color3f(int r, int g, int b) {
		GL11.glColor3f(r, g, b);
	}

	public static void enableAlphaTest() {
		GL11.glEnable(GL11.GL_ALPHA_TEST);
	}

	public static void bindTexture(int image) {
		//LegacyLWJGL3.getMinecraft().textureManager.bind(image);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, image);
	}

	public static int genTextures() {
		return GL11.glGenTextures();
	}

	public static void deleteTexture(int i) {
		GL11.glDeleteTextures(i);
	}
}
