package io.github.moehreag.legacylwjgl3.mixin;

import java.awt.*;

import net.minecraft.client.Mouse;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings({"unused", "UnusedMixin"})
@Mixin(Mouse.class)
public class MouseMixin {

	@Redirect(method = "unlock", at = @At(value = "INVOKE", target = "Ljava/awt/Component;getWidth()I"))
	private int useDisplayWidth(Component instance) {
		return Display.getWidth();
	}

	@Redirect(method = "unlock", at = @At(value = "INVOKE", target = "Ljava/awt/Component;getHeight()I"))
	private int useDisplayHeight(Component instance) {
		return Display.getHeight();
	}
}
