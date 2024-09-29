package io.github.moehreag.legacylwjgl3.mixin;

import java.awt.*;

import net.minecraft.class_596;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// class name: Mouse(Input)
@Mixin(class_596.class)
public abstract class MouseMixin {

	// method name: release
	@Redirect(method = "method_1971", at = @At(value = "INVOKE", target = "Ljava/awt/Component;getHeight()I"))
	private int getHeight(Component instance){
		return Display.getHeight();
	}

	// method name: release
	@Redirect(method = "method_1971", at = @At(value = "INVOKE", target = "Ljava/awt/Component;getWidth()I"))
	private int getWidth(Component instance){
		return Display.getWidth();
	}
}
