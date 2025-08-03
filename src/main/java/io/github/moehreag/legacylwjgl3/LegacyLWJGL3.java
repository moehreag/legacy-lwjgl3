package io.github.moehreag.legacylwjgl3;

import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LegacyLWJGL3 implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("LegacyLWJGL3");

	@Override
	public void onInitializeClient() {
		LOGGER.info("This is definitely a 100% legit legacy LWJGL2 mod!");
	}
}
