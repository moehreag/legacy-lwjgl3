package io.github.moehreag.legacylwjgl3.mixin;

import net.minecraft.client.input.MouseInput;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MouseInput.class)
public abstract class MouseMixin {

    /**
     * @author moehreag
     * @reason we're using lwjgl/glfw for this, there are no awt components.
     */
    @Overwrite
    public void unlock() {
        int var1 = Display.getWidth();
        int var2 = Display.getHeight();
        Mouse.setCursorPosition(var1 / 2, var2 / 2);
        Mouse.setGrabbed(false);
    }
}
