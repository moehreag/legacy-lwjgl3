package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.LegacyLWJGL3ScreenEx;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Screen.class)
public class MixinScreenExtension implements LegacyLWJGL3ScreenEx {

}
