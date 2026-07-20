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
 * Uses custom annotations and the post-apply mixin phase to handle some transformations.
 * Also uses version predicates to selectively apply mixins in order for broader compatibility.
 */
public class Lwjgl3MixinPostProcessor implements IMixinConfigPlugin {
	private static final Version MINECRAFT_VERSION = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow().getMetadata().getVersion();

	private static final VersionPredicate SCREEN_UNAVAILABLE, HAS_APPLET, HAS_APPLET_132, NEW_CLIPBOARD, OLD_CLIPBOARD, AWT_LINK, MOUSE_COMPONENT_FIX,
			OLD_GAMERENDERER, OLD_WINDOW_SCALE, OLD_EDITBOX, OLD_TEXTURE_MANGER, TEXTURE_MANAGER_1_5, PRE_BUFFERBUILDER, TESSELATOR_END_RETURN, NORETURN_TEXTRENDERER,
			NONSTATIC_FILL, MULTIPLAYER_SCREEN_FIX_OLD, MULTIPLAYER_SCREEN_FIX, BOOK_EDIT_SCREEN_HANDLING, SIGN_EDIT_SCREEN_HANDLING;

	static {
		try {
			SCREEN_UNAVAILABLE = VersionPredicate.parse("<0.13.3+launcher"); // c0.0.13a-launcher
			HAS_APPLET = VersionPredicate.parse(">=0.22.5+a <1.6-alpha.13.16.a");
			HAS_APPLET_132 = VersionPredicate.parse(">=1.3-alpha.12.18.a"); // 12w18a
			NEW_CLIPBOARD = VersionPredicate.parse(">=1.2.4");
			OLD_CLIPBOARD = VersionPredicate.parse(">=1.0.0-alpha.0.15"); // a1.0.15
			AWT_LINK = VersionPredicate.parse(">=1.8-alpha.14.25.a"); // 14w28a
			MOUSE_COMPONENT_FIX = VersionPredicate.parse("<1.6-alpha.13.16.a"); // 13w16a
			OLD_GAMERENDERER = VersionPredicate.parse("<=1.8.2-pre.2");
			OLD_WINDOW_SCALE = VersionPredicate.parse("<1.3-alpha.12.21.a"); // 12w21a
			OLD_EDITBOX = VersionPredicate.parse("<1.2.4");
			OLD_TEXTURE_MANGER = VersionPredicate.parse("<1.6-alpha.13.24.a"); // 13w24a
			TEXTURE_MANAGER_1_5 = VersionPredicate.parse(">=1.5-alpha.13.9.a"); // 13w09a
			PRE_BUFFERBUILDER = VersionPredicate.parse("<1.8-alpha.14.25.a"); // 14w28a
			TESSELATOR_END_RETURN = VersionPredicate.parse(">=1.0.0-beta.9.0.5"); // b1.9-pre5
			NORETURN_TEXTRENDERER = VersionPredicate.parse("<1.4.3");
			NONSTATIC_FILL = VersionPredicate.parse("<1.2.4");
			MULTIPLAYER_SCREEN_FIX_OLD = VersionPredicate.parse("<1.0.0-beta.8.0.1+081459"); // b1.8-pre1-081459
			MULTIPLAYER_SCREEN_FIX = VersionPredicate.parse("<1.2.4");
			BOOK_EDIT_SCREEN_HANDLING = VersionPredicate.parse(">=1.3-alpha.12.17.a"); // 12w17a
			SIGN_EDIT_SCREEN_HANDLING = VersionPredicate.parse(">=0.31.20100607"); // inf-20100607
		} catch (VersionParsingException e) {
			throw new IllegalStateException("Failed to parse version:", e);
		}
	}

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

	@SuppressWarnings("RedundantIfStatement")
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (SCREEN_UNAVAILABLE.test(MINECRAFT_VERSION) && (List.of(
				"net.minecraft.client.gui.screen.Screen", "net.minecraft.unmapped.C_85740840" // calamus name for Screen
		).contains(targetClassName) || mixinClassName.endsWith("IMEMinecraftMixin") || mixinClassName.endsWith("ScreenMixin"))) {
			return false;
		}
		if (OLD_GAMERENDERER.test(MINECRAFT_VERSION) && mixinClassName.endsWith("IMEGameRendererMixin")) {
			return false;
		}
		if (OLD_EDITBOX.test(MINECRAFT_VERSION) && mixinClassName.endsWith("IMETextFieldWidgetMixin")) {
			return false;
		}
		if (OLD_TEXTURE_MANGER.test(MINECRAFT_VERSION) && mixinClassName.endsWith("LegacyLWJGL3RenderHelperBindMixin")) {
			return false;
		}
		if (PRE_BUFFERBUILDER.test(MINECRAFT_VERSION) && (mixinClassName.endsWith("LegacyLWJGL3RenderHelperBlitMixin") || mixinClassName.endsWith("LegacyLWJGL3RenderHelperMatrixMixin"))) {
			return false;
		}
		if (NORETURN_TEXTRENDERER.test(MINECRAFT_VERSION) && mixinClassName.endsWith("LegacyLWJGL3RenderHelperDrawStringMixin")) {
			return false;
		}
		if (NONSTATIC_FILL.test(MINECRAFT_VERSION) && mixinClassName.endsWith("LegacyLWJGL3RenderHelperFillMixin")) {
			return false;
		}
		if (!BOOK_EDIT_SCREEN_HANDLING.test(MINECRAFT_VERSION) && mixinClassName.endsWith("IMEBookEditScreenMixin")) {
			return false;
		}
		if (!SIGN_EDIT_SCREEN_HANDLING.test(MINECRAFT_VERSION) && mixinClassName.endsWith("IMESignEditScreenMixin")) {
			return false;
		}
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		var additionalMixins = new ArrayList<String>();
		if (HAS_APPLET.test(MINECRAFT_VERSION)) {
			LOGGER.info("Applying Applet Mixins!");
			if (HAS_APPLET_132.test(MINECRAFT_VERSION)) {
				additionalMixins.add("MinecraftApplet132Mixin");
			} else {
				additionalMixins.add("MinecraftAppletMixin");
			}
			additionalMixins.add("MixinResourceDownloadThread");
		}
		if (NEW_CLIPBOARD.test(MINECRAFT_VERSION)) {
			additionalMixins.add("MixinScreenFixClipboard");
		} else if (OLD_CLIPBOARD.test(MINECRAFT_VERSION)) {
			additionalMixins.add("MixinScreenFixClipboardOld");
		}
		if (AWT_LINK.test(MINECRAFT_VERSION)) {
			additionalMixins.add("MixinScreenFixOpenLink");
		}
		if (MOUSE_COMPONENT_FIX.test(MINECRAFT_VERSION)) {
			additionalMixins.add("MouseMixin");
		}
		if (OLD_GAMERENDERER.test(MINECRAFT_VERSION)) {
			additionalMixins.add(OLD_WINDOW_SCALE.test(MINECRAFT_VERSION) ? "IMEGameRendererOldMixin" : "IMEGameRenderer13Mixin");
		}
		if (OLD_EDITBOX.test(MINECRAFT_VERSION)) {
			additionalMixins.add("IMETextFieldWidgetOldMixin");
		}
		if (OLD_TEXTURE_MANGER.test(MINECRAFT_VERSION)) {
			additionalMixins.add(TEXTURE_MANAGER_1_5.test(MINECRAFT_VERSION) ? "LegacyLWJGL3RenderHelperBind15Mixin" : "LegacyLWJGL3RenderHelperBindOldMixin");
		}
		if (PRE_BUFFERBUILDER.test(MINECRAFT_VERSION)) {
			additionalMixins.add(TESSELATOR_END_RETURN.test(MINECRAFT_VERSION) ? "LegacyLWJGL3RenderHelperBlit13Mixin" : "LegacyLWJGL3RenderHelperBlitOldMixin");
			additionalMixins.add("LegacyLWJGL3RenderHelperMatrixOldMixin");
		}
		if (NORETURN_TEXTRENDERER.test(MINECRAFT_VERSION)) {
			additionalMixins.add("LegacyLWJGL3RenderHelperDrawStringOldMixin");
		}
		if (NONSTATIC_FILL.test(MINECRAFT_VERSION)) {
			additionalMixins.add("LegacyLWJGL3RenderHelperFillOldMixin");
		}
		if (!SCREEN_UNAVAILABLE.test(MINECRAFT_VERSION)) {
			if (MULTIPLAYER_SCREEN_FIX_OLD.test(MINECRAFT_VERSION)) {
				additionalMixins.add("JoinMultiplayerScreenMixin");
			} else if (MULTIPLAYER_SCREEN_FIX.test(MINECRAFT_VERSION)) {
				additionalMixins.add("MultiplayerScreensMixin");
			}
		}
		return additionalMixins;
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
