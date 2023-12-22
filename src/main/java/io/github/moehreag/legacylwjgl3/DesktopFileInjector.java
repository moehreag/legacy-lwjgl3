package io.github.moehreag.legacylwjgl3;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.moehreag.legacylwjgl3.util.XDGPathResolver;
import net.fabricmc.loader.api.FabricLoader;
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
					pixels[i] = Integer.rotateRight(buf.getInt(), 8);
				}
				int size = (int) Math.sqrt(pixels.length);
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
		return XDGPathResolver.getUserDataLocation().resolve("icons/hicolor").resolve(width + "x" + height)
				.resolve("apps").resolve(ICON_NAME);
	}

	private static Path getDesktopFileLocation() {
		return XDGPathResolver.getUserDataLocation().resolve("applications").resolve(FILE_NAME);
	}

	private static void updateIconSystem() {
		ProcessBuilder builder = new ProcessBuilder("xdg-icon-resource", "forceupdate");
		try {
			builder.start();
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
