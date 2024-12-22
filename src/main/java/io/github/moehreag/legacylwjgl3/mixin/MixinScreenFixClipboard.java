package io.github.moehreag.legacylwjgl3.mixin;

import java.util.Objects;

import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Screen.class)
public abstract class MixinScreenFixClipboard {

	/**
	 * @author moehreag
	 * @reason Fix clipboard access with GLFW
	 */
	@Overwrite
	public static String getClipboard(){
        return Objects.requireNonNullElse(GLFW.glfwGetClipboardString(Display.getHandle()), "");
	}

	/**
	 * @author moehreag
	 * @reason Fix clipboard access with GLFW
	 *
	@Overwrite
	public static void setClipboard(String string){
		GLFW.glfwSetClipboardString(Display.getHandle(), string);
	}*/
}
