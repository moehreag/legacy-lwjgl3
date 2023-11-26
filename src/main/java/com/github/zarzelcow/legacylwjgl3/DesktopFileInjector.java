package com.github.zarzelcow.legacylwjgl3;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.io.IOUtils;

public class DesktopFileInjector {
	public static final String APP_ID = "com.mojang.minecraft";
	private static final String ICON_NAME = "minecraft.png";
	private static final String FILE_NAME = APP_ID + ".desktop";
	private static final String RESOURCE_LOCATION = "/assets/legacy-lwjgl3/" + FILE_NAME;
	private static final List<Path> injectedLocations = new ArrayList<>();

	public static void inject() {
		Runtime.getRuntime().addShutdownHook(new Thread(DesktopFileInjector::uninject));

		try (InputStream stream = DesktopFileInjector.class.getResourceAsStream(RESOURCE_LOCATION)) {
			Path location = getDesktopFileLocation();

			String version = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow(IllegalStateException::new)
					.getMetadata().getVersion().getFriendlyString();
			injectFile(location, String.format(IOUtils.toString(Objects.requireNonNull(stream)),
					version, ICON_NAME.substring(0, ICON_NAME.lastIndexOf("."))).getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public static int setIcon(ByteBuffer[] icons) {
		for (ByteBuffer buf : icons) {
			try {
				int[] pixels = new int[buf.remaining() / 4];
				for (int i = 0; i < pixels.length; i++) {
					Color c = new Color(buf.getInt(), true);
					Color transformed = new Color(c.getAlpha(), c.getRed(), c.getGreen(), c.getBlue());
					pixels[i] = transformed.getRGB();
				}
				int size = (int) MathHelper.sqrt(pixels.length);
				BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
				image.setRGB(0, 0, size, size, pixels, 0, size);
				Path target = getIconFileLocation(image.getWidth(), image.getHeight());
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				ImageIO.write(image, "png", outputStream);

				injectFile(target, outputStream.toByteArray());
			} catch (IOException e) {
				return 1;
			}
		}
		updateIconSystem();
		return 0;
	}

	private static void injectFile(Path target, byte[] data) {
		try {
			if (!Files.exists(target)) {
				Files.createFile(target);
			}
			Files.write(target, data);
			injectedLocations.add(target);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	private static Path getIconFileLocation(int width, int height) {
		return getDataLocation().resolve("icons/hicolor").resolve(width + "x" + height)
				.resolve("apps").resolve(ICON_NAME);
	}

	private static Path getDesktopFileLocation() {
		return getDataLocation().resolve("applications").resolve(FILE_NAME);
	}

	private static Path getDataLocation() {
		String xdgDataHome = System.getenv("$XDG_DATA_HOME");
		if (xdgDataHome == null || xdgDataHome.isEmpty()) {
			String home = System.getenv().getOrDefault("$HOME", System.getProperty("user.home"));
			if (home == null || home.isEmpty()) {
				throw new IllegalStateException("could not resolve user home");
			}
			return new File(home).toPath().resolve(".local/share/");
		}
		return new File(xdgDataHome).toPath();
	}

	private static void updateIconSystem() {
		ProcessBuilder builder = new ProcessBuilder("xdg-icon-resource", "forceupdate");
		try {
			Process p = builder.start();
		} catch (IOException ignored) {
		}
	}

	private static void uninject() {
		injectedLocations.forEach(p -> {
			try {
				Files.deleteIfExists(p);
			} catch (IOException ignored) {

			}
		});
		updateIconSystem();
	}
}
