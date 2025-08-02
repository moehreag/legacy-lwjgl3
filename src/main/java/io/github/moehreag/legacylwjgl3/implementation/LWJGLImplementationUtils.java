package io.github.moehreag.legacylwjgl3.implementation;

import io.github.moehreag.legacylwjgl3.implementation.sdl.SDLKeyboardImplementation;
import io.github.moehreag.legacylwjgl3.implementation.sdl.SDLMouseImplementation;
import io.github.moehreag.legacylwjgl3.implementation.input.CombinedInputImplementation;
import io.github.moehreag.legacylwjgl3.implementation.input.InputImplementation;

/**
 * @author Zarzelcow
 * @created 28/09/2022 - 3:12 PM
 */
public class LWJGLImplementationUtils {
	private static final boolean allowVirtualCursor = Boolean.getBoolean("legacy_lwjgl3.allow_virtual_cursor") || System.getenv("LEGACY_LWJGL3_ALLOW_VIRTUAL_CURSOR") != null;
	private static InputImplementation _inputImplementation;

	public static InputImplementation getOrCreateInputImplementation() {
		if (_inputImplementation == null) {
			_inputImplementation = createImplementation();
		}
		return _inputImplementation;
	}

	private static InputImplementation createImplementation() {
		return new CombinedInputImplementation(new SDLKeyboardImplementation(), new SDLMouseImplementation());
	}

}
