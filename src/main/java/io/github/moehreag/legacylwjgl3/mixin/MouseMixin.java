package io.github.moehreag.legacylwjgl3.mixin;

import java.awt.*;

import net.minecraft.client.Mouse;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mouse.class)
public abstract class MouseMixin {

	@Redirect(method = "release", at = @At(value = "INVOKE", target = "Ljava/awt/Component;getHeight()I"))
	private int getHeight(Component instance){
		return Display.getHeight();
	}

	@Redirect(method = "release", at = @At(value = "INVOKE", target = "Ljava/awt/Component;getWidth()I"))
	private int getWidth(Component instance){
		return Display.getWidth();
	}
}
