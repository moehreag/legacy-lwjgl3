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
		/*try {
			Field mc = Minecraft.class.getField(FabricLoader.getInstance().getMappingResolver().mapFieldName("intermediary",
				"net.minecraft.client.Minecraft",
				"f_7951283", "Lnet/minecraft/client/Minecraft;"));
			mc.setAccessible(true);
			return (Minecraft) mc.get(null);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new IllegalStateException(e);
		}*/
		return MinecraftAccessor.getInstance();
	}
}
