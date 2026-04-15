package io.github.moehreag.legacylwjgl3.implementation;

import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import io.github.moehreag.legacylwjgl3.implementation.glfw.GLFWKeyboardImplementation;
import io.github.moehreag.legacylwjgl3.implementation.glfw.GLFWMouseImplementation;
import io.github.moehreag.legacylwjgl3.implementation.input.CombinedInputImplementation;
import io.github.moehreag.legacylwjgl3.implementation.input.InputImplementation;
import io.github.moehreag.legacylwjgl3.implementation.input.KeyboardImplementation;
import io.github.moehreag.legacylwjgl3.implementation.input.MouseImplementation;
import io.github.moehreag.legacylwjgl3.implementation.sdl.SDLKeyboardImplementation;
import io.github.moehreag.legacylwjgl3.implementation.sdl.SDLMouseImplementation;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class LWJGLImplementationUtils {
	private static InputImplementation _inputImplementation;
	public static MouseImplementation _mouseImplementation;
	public static KeyboardImplementation _keyboardImplementation;

	public static InputImplementation getOrCreateInputImplementation() {
		if (_inputImplementation == null) {
			_inputImplementation = createImplementation();
		}
		return _inputImplementation;
	}

	private static InputImplementation createImplementation() {
		if (LegacyLWJGL3.USE_SDL) {
			_mouseImplementation = new SDLMouseImplementation();
			_keyboardImplementation = new SDLKeyboardImplementation();
		} else {
			_mouseImplementation = new GLFWMouseImplementation();
			_keyboardImplementation = new GLFWKeyboardImplementation();
		}
		return new CombinedInputImplementation(_keyboardImplementation, _mouseImplementation);
	}

}
