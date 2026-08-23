package io.github.moehreag.legacylwjgl3.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;
import net.fabricmc.loader.api.FabricLoader;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarInputStream;
import org.tukaani.xz.XZInputStream;

@Slf4j
public class LibraryExtractor {

	private static final String MODID = "legacy-lwjgl3";

	public void run(Consumer<Path> classpathAdder) {
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) return;
		try {
			var container = FabricLoader.getInstance().getModContainer(MODID).orElseThrow();
			var version = container.getMetadata().getVersion().getFriendlyString();
			var out = Path.of(System.getProperty("java.io.tmpdir"), MODID+"_"+version+"_dependencies");
			if (Files.exists(out)) {
				try (var libraries = Files.list(out)) {
					libraries.forEach(classpathAdder);
				}
				return;
			}
			Files.createDirectories(out);
			var path = container.findPath("libraries.tar.xz").orElseThrow(IOException::new);
			try (var in = Files.newInputStream(path)) {

				try (var buf = new BufferedInputStream(in);
					 var xz = new XZInputStream(buf);
					 var tar = new TarInputStream(xz)) {
					TarEntry currentEntry;
					while ((currentEntry = tar.getNextEntry()) != null) {
						var entry = tar.readNBytes((int) currentEntry.getSize());
						var res = out.resolve(currentEntry.getName());
						Files.write(res, entry);
						classpathAdder.accept(res);
					}
				}
			}
		} catch (IOException e) {
			log.error("Failed to extract libraries, the game is likely to crash!", e);
		}
	}
}
