package io.github.moehreag.legacylwjgl3.util;

import java.nio.IntBuffer;

import io.github.moehreag.legacylwjgl3.mixin.MinecraftAccessor;
import net.minecraft.client.render.platform.MemoryTracker;
import org.lwjgl.opengl.GL11;

public class TextureUtil {
	private static final IntBuffer BUFFER = MemoryTracker.createIntBuffer(4194304);

	public static int genTextures() {
		return GlStateManager.genTextures();
	}

	public static void deleteTextures(int i) {
		GlStateManager.deleteTexture(i);
	}

	public static void uploadTexture(int i, int[] is, int j, int k) {
		bind(i);
		upload(0, is, j, k, 0, 0, false, false, false);
	}

	private static void upload(int i, int[] is, int j, int k, int l, int m, boolean bl, boolean bl2, boolean bl3) {
		int n = 4194304 / j;
		setTextureFilter(bl, bl3);
		setTextureClamp(bl2);

		int q;
		for (int o = 0; o < j * k; o += j * q) {
			int p = o / j;
			q = Math.min(n, k - p);
			int r = j * q;
			putInBufferAt(is, o, r);
			GL11.glTexSubImage2D(3553, i, l, m + p, j, q, 32993, 33639, BUFFER);
		}
	}

	public static void prepare(int i, int j, int k) {
		prepareImage(i, 0, j, k);
	}

	public static void prepareImage(int i, int j, int k, int l) {
		deleteTextures(i);
		bind(i);
		if (j >= 0) {
			GL11.glTexParameteri(3553, 33085, j);
			GL11.glTexParameterf(3553, 33082, 0.0F);
			GL11.glTexParameterf(3553, 33083, (float) j);
			GL11.glTexParameterf(3553, 34049, 0.0F);
		}

		for (int m = 0; m <= j; ++m) {
			GL11.glTexImage2D(3553, m, 6408, k >> m, l >> m, 0, 32993, 33639, (IntBuffer) null);
		}
	}

	private static void setTextureClamp(boolean bl) {
		if (bl) {
			GL11.glTexParameteri(3553, 10242, 10496);
			GL11.glTexParameteri(3553, 10243, 10496);
		} else {
			GL11.glTexParameteri(3553, 10242, 10497);
			GL11.glTexParameteri(3553, 10243, 10497);
		}
	}

	private static void setTextureFilter(boolean bl, boolean bl2) {
		if (bl) {
			GL11.glTexParameteri(3553, 10241, bl2 ? 9987 : 9729);
			GL11.glTexParameteri(3553, 10240, 9729);
		} else {
			GL11.glTexParameteri(3553, 10241, bl2 ? 9986 : 9728);
			GL11.glTexParameteri(3553, 10240, 9728);
		}
	}

	private static void putInBufferAt(int[] is, int i, int j) {
		int[] js = is;
		if (MinecraftAccessor.getInstance().options.anaglyph) {
			js = getAnaglyphColors(is);
		}

		BUFFER.clear();
		BUFFER.put(js, i, j);
		BUFFER.position(0).limit(j);
	}

	static void bind(int i) {
		GlStateManager.bindTexture(i);
	}

	public static int[] getAnaglyphColors(int[] is) {
		int[] js = new int[is.length];

		for (int i = 0; i < is.length; ++i) {
			js[i] = getAnaglyphColor(is[i]);
		}

		return js;
	}

	public static int getAnaglyphColor(int i) {
		int j = i >> 24 & 0xFF;
		int k = i >> 16 & 0xFF;
		int l = i >> 8 & 0xFF;
		int m = i & 0xFF;
		int n = (k * 30 + l * 59 + m * 11) / 100;
		int o = (k * 30 + l * 70) / 100;
		int p = (k * 30 + m * 70) / 100;
		return j << 24 | n << 16 | o << 8 | p;
	}
}