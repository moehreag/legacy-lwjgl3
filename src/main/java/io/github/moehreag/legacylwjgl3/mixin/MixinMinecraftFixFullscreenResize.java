package io.github.moehreag.legacylwjgl3.mixin;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public class MixinMinecraftFixFullscreenResize {

    @Shadow private boolean fullscreen;

    @Redirect(method = "updateWindow", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;fullscreen:Z"))
    private boolean noFullscreenCheckForResize(Minecraft instance) {
        return GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WIN32 && fullscreen;
    }
}
