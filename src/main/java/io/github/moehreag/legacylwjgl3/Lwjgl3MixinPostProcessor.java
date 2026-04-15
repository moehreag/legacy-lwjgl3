package io.github.moehreag.legacylwjgl3;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.github.moehreag.legacylwjgl3.annotations.CreateStub;
import io.github.moehreag.legacylwjgl3.annotations.Public;
import io.github.moehreag.legacylwjgl3.util.CodeGen;
import io.github.moehreag.legacylwjgl3.util.LibraryExtractor;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.util.Annotations;

import static io.github.moehreag.legacylwjgl3.LegacyLWJGL3.LOGGER;

/*
 * Uses custom annotations and the post-apply mixin phase to handle some transformations
 */
public class Lwjgl3MixinPostProcessor implements IMixinConfigPlugin {
	private static final Version MINECRAFT_VERSION = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow().getMetadata().getVersion();

	@Override
	public void onLoad(String mixinPackage) {
		// Add paulscode to the knot classpath (specifically the librarylwjglopenal as that load lwjgl classes).
		// by default, paulscode is a part of the system class loader which only has LWJGL 2 in its classpath
		// adding it to the knot classpath means paulscode will instead load classes with the knot class loader
		// meaning it will load LWJGL 3 classes from the knot classpath instead of LWJGL 2 from the system classpath
		//
		// This was such a pain to figure out and caused me so much trouble
		getPaulscodePath().ifPresent(FabricLauncherBase.getLauncher()::addToClassPath);
		new LibraryExtractor().run(FabricLauncherBase.getLauncher()::addToClassPath);
	}

	public static Optional<Path> getPaulscodePath() {
		for (Path path : FabricLauncherBase.getLauncher().getClassPath()) {
			if (path.toString().contains("librarylwjglopenal")) {
				return Optional.of(path);
			}
		}
		return Optional.empty();
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		var apply = true;
		try {
			if (MINECRAFT_VERSION.compareTo(Version.parse("0.13.0+a.launcher")) < 0) {
				apply = !List.of("net.minecraft.client.Minecraft",
						"net.minecraft.client.gui.screen.Screen", "net.minecraft.unmapped.C_85740840"
				).contains(targetClassName);
			}
		} catch (VersionParsingException e) {
			LOGGER.warn("Failed to parse version:", e);
		}
		return apply;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		try {
			var additionalMixins = new ArrayList<String>();
			if (VersionPredicate.parse(">=0.22.5+a <1.6.0-alpha.13.16.a+04192037").test(MINECRAFT_VERSION)) {
				LOGGER.info("Applying Applet Mixins!");
				additionalMixins.add("MinecraftAppletMixin");
				additionalMixins.add("MixinResourceDownloadThread");
			}
			if (VersionPredicate.parse("<1.2.4").test(MINECRAFT_VERSION)) {
				additionalMixins.add("MixinScreenFixClipboardOld");
			} else {
				additionalMixins.add("MixinScreenFixClipboard");
			}
			return additionalMixins;
		} catch (VersionParsingException e) {
			LOGGER.warn("Failed to parse version:", e);
		}
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	private static void processPublic(ClassNode target, MethodNode method) {
		if (Annotations.getInvisible(method, Public.class) == null) {
			return;
		}

		if ((method.access & Opcodes.ACC_STATIC) == 0) {
			LOGGER.warn("[L3MPP] non static method annotated with @Public");
			return;
		}

		LOGGER.debug("[L3MPP] Injected public static method {}::{}{}", target.name, method.name, method.desc);
		method.access &= ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED);
		method.access |= Opcodes.ACC_PUBLIC;
	}

	private static void processStub(ClassNode target, MethodNode method) {
		final var node = Annotations.getInvisible(method, CreateStub.class);
		if (node == null) {
			return;
		}

		final var stub = CodeGen.createDelegatingMethod(target, method, Annotations.getValue(node));
		target.methods.add(stub);
		LOGGER.debug("[L3MPP] Created method {}::[{} -> {}]{}", target.name, stub.name, method.name, method.desc);
	}

	@Override
	public void postApply(String targetClassName, ClassNode target, String mixinClassName, IMixinInfo mixinInfo) {
		for (final var method : new ArrayList<>(target.methods)) {
			processPublic(target, method);
			processStub(target, method);
		}
	}
}
