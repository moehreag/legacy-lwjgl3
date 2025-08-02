package io.github.moehreag.legacylwjgl3.implementation.input;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

import org.lwjgl.sdl.SDL_Event;

/**
 * @author Zarzelcow
 * @created 28/09/2022 - 8:58 PM
 */
public interface MouseImplementation {
	void createMouse();

	void destroyMouse();

	void pollMouse(DoubleBuffer coord_buffer, ByteBuffer buttons_buffer);

	void readMouse(ByteBuffer readBuffer);

	void setCursorPosition(double x, double y);

	void grabMouse(boolean grab);

	boolean hasWheel();

	int getButtonCount();

	boolean isInsideWindow();

	void processMouseEvent(SDL_Event event);
}
