package io.github.moehreag.legacylwjgl3;

import java.io.*;
import java.util.stream.Collectors;

import io.github.moehreag.legacylwjgl3.mixin.MinecraftAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LegacyLWJGL3 implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void onInitializeClient() {
		LOGGER.info("This is definitely a 100% legit legacy LWJGL2 mod!");
	}

	public static Minecraft getMinecraft() {
		return MinecraftAccessor.getInstance();
	}

	public static String toString(InputStream in) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
			return reader.lines().collect(Collectors.joining("\n"));
		} catch (IOException e) {
			return "";
		}
	}

	public static byte[] toByteArray(InputStream in) {


		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[8192];
			while (in.read(buf) != -1) {
				out.write(buf);
			}
			return out.toByteArray();
		} catch (IOException e) {
			return new byte[0];
		}
	}

	public static void closeQuietly(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException ignored) {
			}
		}
	}
}
