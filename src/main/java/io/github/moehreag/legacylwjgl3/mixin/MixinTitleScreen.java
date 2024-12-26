package io.github.moehreag.legacylwjgl3.mixin;

import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/lang/String;length()I", ordinal = 1))
    private int preventUpdateCheck(String instance) {
        return 1;
    }
}
