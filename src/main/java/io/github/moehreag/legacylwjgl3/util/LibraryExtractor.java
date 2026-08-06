package io.github.moehreag.legacylwjgl3.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarInputStream;
import org.tukaani.xz.XZInputStream;

@Slf4j
public class LibraryExtractor {

	private static final String MODID = "legacy-lwjgl3";

	public void run(Consumer<Path> classpathAdder) {
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) return;
		try {
			var out = Files.createTempDirectory(MODID+"_dependencies");
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					recursiveDelete(out);
				} catch (Exception e) {
					//log.warn("Failed to delete temp libraries dir!", e);
				}
			}));
			var path = FabricLoader.getInstance().getModContainer(MODID).orElseThrow(IOException::new).findPath("libraries.tar.xz").orElseThrow(IOException::new);
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

	private void recursiveDelete(Path p) throws IOException {
		if (!Files.exists(p)) return;
		Files.walkFileTree(p, new SimpleFileVisitor<>() {
			@Override
			public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
				Files.deleteIfExists(file);
				return super.visitFile(file, attrs);
			}

			@Override
			public @NotNull FileVisitResult postVisitDirectory(@NotNull Path dir, @Nullable IOException exc) throws IOException {
				Files.deleteIfExists(dir);
				return super.postVisitDirectory(dir, exc);
			}
		});
		Files.deleteIfExists(p);
	}
}
