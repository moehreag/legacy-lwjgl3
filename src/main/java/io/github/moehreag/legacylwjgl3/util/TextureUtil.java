package io.github.moehreag.legacylwjgl3.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;

import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import io.github.moehreag.legacylwjgl3.mixin.MinecraftAccessor;
import net.minecraft.client.util.GlAllocationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

public class TextureUtil {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final IntBuffer BUFFER = GlAllocationUtils.allocateIntBuffer(4194304);

	public static int genTextures() {
		return GlStateManager.genTextures();
	}

	public static void deleteTextures(int i) {
		GlStateManager.deleteTexture(i);
	}

	public static int uploadTexture(int i, BufferedImage bufferedImage) {
		return upload(i, bufferedImage, false, false);
	}

	public static void uploadTexture(int i, int[] is, int j, int k) {
		bind(i);
		upload(0, is, j, k, 0, 0, false, false, false);
	}

	private static int blendPixelComponents(int i, int j, int k, int l, int m) {
		float f = (float) Math.pow((double) ((float) (i >> m & 0xFF) / 255.0F), 2.2);
		float g = (float) Math.pow((double) ((float) (j >> m & 0xFF) / 255.0F), 2.2);
		float h = (float) Math.pow((double) ((float) (k >> m & 0xFF) / 255.0F), 2.2);
		float n = (float) Math.pow((double) ((float) (l >> m & 0xFF) / 255.0F), 2.2);
		float o = (float) Math.pow((double) (f + g + h + n) * 0.25, 0.45454545454545453);
		return (int) ((double) o * 255.0);
	}

	public static void upload(int[][] is, int i, int j, int k, int l, boolean bl, boolean bl2) {
		for (int m = 0; m < is.length; ++m) {
			int[] js = is[m];
			upload(m, js, i >> m, j >> m, k >> m, l >> m, bl, bl2, is.length > 1);
		}
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

	public static int upload(int i, BufferedImage bufferedImage, boolean bl, boolean bl2) {
		prepare(i, bufferedImage.getWidth(), bufferedImage.getHeight());
		return upload(i, bufferedImage, 0, 0, bl, bl2);
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

	public static int upload(int i, BufferedImage bufferedImage, int j, int k, boolean bl, boolean bl2) {
		bind(i);
		upload(bufferedImage, j, k, bl, bl2);
		return i;
	}

	private static void upload(BufferedImage bufferedImage, int i, int j, boolean bl, boolean bl2) {
		int k = bufferedImage.getWidth();
		int l = bufferedImage.getHeight();
		int m = 4194304 / k;
		int[] is = new int[m * k];
		setFilterWithBlur(bl);
		setTextureClamp(bl2);

		for (int n = 0; n < k * l; n += k * m) {
			int o = n / k;
			int p = Math.min(m, l - o);
			int q = k * p;
			bufferedImage.getRGB(0, o, k, p, is, 0, k);
			putInBuffer(is, q);
			GL11.glTexSubImage2D(3553, 0, i, j + o, k, p, 32993, 33639, BUFFER);
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

	private static void setFilterWithBlur(boolean bl) {
		setTextureFilter(bl, false);
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

	private static void putInBuffer(int[] is, int i) {
		putInBufferAt(is, 0, i);
	}

	private static void putInBufferAt(int[] is, int i, int j) {
		int[] js = is;
		if (MinecraftAccessor.getInstance().options.anaglyph3d) {
			js = getAnaglyphColors(is);
		}

		BUFFER.clear();
		BUFFER.put(js, i, j);
		BUFFER.position(0).limit(j);
	}

	static void bind(int i) {
		GlStateManager.bindTexture(i);
	}

	public static BufferedImage readImage(InputStream inputStream) throws IOException {
		BufferedImage var1;
		try {
			var1 = ImageIO.read(inputStream);
		} finally {
			LegacyLWJGL3.closeQuietly(inputStream);
		}

		return var1;
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

	public static void copyTextureValues(int[] is, int i, int j) {
		int[] js = new int[i];
		int k = j / 2;

		for (int l = 0; l < k; ++l) {
			System.arraycopy(is, l * i, js, 0, i);
			System.arraycopy(is, (j - 1 - l) * i, is, l * i, i);
			System.arraycopy(js, 0, is, (j - 1 - l) * i, i);
		}
	}
}
