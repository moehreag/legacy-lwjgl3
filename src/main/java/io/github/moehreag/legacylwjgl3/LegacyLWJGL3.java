package io.github.moehreag.legacylwjgl3;

import io.github.moehreag.legacylwjgl3.mixin.MinecraftAccessor;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LegacyLWJGL3 implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger();
	@Getter @Setter
	private static Minecraft minecraft;

	@Override
	public void onInitializeClient() {
		LOGGER.info("This is definitely a 100% legit legacy LWJGL2 mod!");
	}
}
