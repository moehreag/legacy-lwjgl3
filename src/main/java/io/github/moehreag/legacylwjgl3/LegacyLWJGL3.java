package io.github.moehreag.legacylwjgl3;

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
}
