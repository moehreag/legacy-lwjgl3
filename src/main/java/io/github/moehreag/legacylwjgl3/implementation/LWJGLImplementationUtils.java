package io.github.moehreag.legacylwjgl3.implementation;

import io.github.moehreag.legacylwjgl3.implementation.glfw.GLFWKeyboardImplementation;
import io.github.moehreag.legacylwjgl3.implementation.glfw.GLFWMouseImplementation;
import io.github.moehreag.legacylwjgl3.implementation.glfw.VirtualGLFWMouseImplementation;
import io.github.moehreag.legacylwjgl3.implementation.input.CombinedInputImplementation;
import io.github.moehreag.legacylwjgl3.implementation.input.InputImplementation;
import io.github.moehreag.legacylwjgl3.implementation.input.MouseImplementation;
import org.lwjgl.glfw.GLFW;

/**
 * @author Zarzelcow
 * @created 28/09/2022 - 3:12 PM
 */
public class LWJGLImplementationUtils {
    private static InputImplementation _inputImplementation;

    public static InputImplementation getOrCreateInputImplementation() {
        if (_inputImplementation == null) {
            _inputImplementation = createImplementation();
        }
        return _inputImplementation;
    }

    private static InputImplementation createImplementation() {
        MouseImplementation mouse = GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND ?
                VirtualGLFWMouseImplementation.getInstance() :
                new GLFWMouseImplementation();
        return new CombinedInputImplementation(new GLFWKeyboardImplementation(), mouse);
    }

}
