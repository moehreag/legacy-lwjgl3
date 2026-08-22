package io.github.moehreag.legacylwjgl3.implementation.sdl;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

import io.github.moehreag.legacylwjgl3.implementation.input.MouseImplementation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EventQueue;
import org.lwjgl.opengl.SDLDisplay;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.sdl.SDL_MouseButtonEvent;
import org.lwjgl.sdl.SDL_MouseMotionEvent;
import org.lwjgl.sdl.SDL_MouseWheelEvent;

import static org.lwjgl.sdl.SDLEvents.*;
import static org.lwjgl.sdl.SDLMouse.*;

public class SDLMouseImplementation implements MouseImplementation {

	private long windowHandle;
	private boolean grabbed;
	private boolean isInsideWindow;

	private final EventQueue event_queue = new EventQueue(Mouse.EVENT_SIZE);

	private final ByteBuffer tmp_event = ByteBuffer.allocate(Mouse.EVENT_SIZE);

	private double last_x;
	private double last_y;
	private double accum_dx;
	private double accum_dy;
	private double accum_dz;
	protected byte[] button_states = new byte[this.getButtonCount()];
	private final SDL_MouseButtonEvent mouseButtonEvent = SDLDisplay.getInstance().getEvent().button();
	private final SDL_MouseMotionEvent mouseMotionEvent = SDLDisplay.getInstance().getEvent().motion();
	private final SDL_MouseWheelEvent mouseWheelEvent = SDLDisplay.getInstance().getEvent().wheel();

	@Override
	public void createMouse() {
		this.windowHandle = Display.getHandle();
	}

	protected void putMouseEvent(byte button, byte state, int dz, long nanos) {
		if (grabbed)
			putMouseEventWithCoords(button, state, 0, 0, dz, nanos);
		else
			putMouseEventWithCoords(button, state, last_x, last_y, dz, nanos);
	}

	protected void putMouseEventWithCoords(byte button, byte state, double coord1, double coord2, int dz, long nanos) {
		tmp_event.clear();
		tmp_event.put(button).put(state).putDouble(coord1).putDouble(coord2).putDouble(dz).putLong(nanos);
		tmp_event.flip();
		event_queue.putEvent(tmp_event);
	}

	private void putMouseMotionEvent(double coord1, double coord2, long nanos) {
		synchronized (event_queue) {
			if (event_queue.hasEvents()) {
				ByteBuffer lastEvent = event_queue.getLastEvent();
				if (lastEvent.get(0) == -1 && lastEvent.getDouble(18) == 0) {
					if (grabbed) {
						lastEvent.putDouble(2, lastEvent.getDouble(2) + coord1);
						lastEvent.putDouble(10, lastEvent.getDouble(10) + coord2);
					} else {
						lastEvent.putDouble(2, coord1);
						lastEvent.putDouble(10, coord2);
					}
					lastEvent.putLong(26, nanos);
					return;
				}
			}

			putMouseEventWithCoords((byte) -1, (byte) 0, coord1, coord2, 0, nanos);
		}
	}

	@Override
	public void destroyMouse() {

	}

	private void reset() {
		this.event_queue.clearEvents();
		accum_dx = accum_dy = 0;
	}

	@Override
	public void pollMouse(DoubleBuffer coord_buffer, ByteBuffer buttons_buffer) {
		if (grabbed) {
			coord_buffer.put(0, accum_dx);
			coord_buffer.put(1, accum_dy);
		} else {
			coord_buffer.put(0, last_x);
			coord_buffer.put(1, last_y);
		}
		coord_buffer.put(2, accum_dz);
		accum_dx = accum_dy = accum_dz = 0;
		for (int i = 0; i < button_states.length; i++)
			buttons_buffer.put(i, button_states[i]);
	}

	@Override
	public void readMouse(ByteBuffer readBuffer) {
		event_queue.copyEvents(readBuffer);
	}

	@Override
	public void setCursorPosition(double x, double y) {
		float scale = Display.getPixelScaleFactor();
		this.last_x = x;
		this.last_y = y;
		SDL_WarpMouseInWindow(windowHandle, (float) (x / scale), (float) (y / scale));
	}

	@Override
	public void grabMouse(boolean grab) {
		if (!grab) {
			setCursorPosition(last_x, last_y);
		}
		SDL_SetWindowRelativeMouseMode(windowHandle, grab);
		this.grabbed = grab;
		this.reset();
	}

	@Override
	public boolean hasWheel() {
		return true;
	}

	@Override
	public int getButtonCount() {
		return 5;
	}

	@Override
	public boolean isInsideWindow() {
		return isInsideWindow;
	}

	public void processMouseEvent(SDL_Event event) {
		switch (event.type()) {
			case SDL_EVENT_MOUSE_BUTTON_UP, SDL_EVENT_MOUSE_BUTTON_DOWN -> {
				byte state = mouseButtonEvent.down() ? (byte) 1 : (byte) 0;
				byte button = switch (mouseButtonEvent.button()) {
					// SDL has right & middle buttons switched
					case SDL_BUTTON_RIGHT -> 1;
					case SDL_BUTTON_MIDDLE -> 2;
					default -> (byte) (mouseButtonEvent.button() - 1);
				};
				putMouseEvent(button, state, 0, System.nanoTime());
				if (button < button_states.length)
					button_states[button] = state;
			}
			case SDL_EVENT_MOUSE_WHEEL -> {
				float yoffset = mouseWheelEvent.y();
				accum_dz += yoffset;
				putMouseEvent((byte) -1, (byte) 0, (int) yoffset, System.nanoTime());

			}
			case SDL_EVENT_MOUSE_MOTION -> {
				float scale = Display.getPixelScaleFactor();
				int x = (int) (mouseMotionEvent.x() * scale);
				// LWJGL2: (0, 0) = the bottom-left corner
				// SDL3: (0, 0) = the top-left corner
				int y = (int) ((Display.getScreenHeight() - mouseMotionEvent.y()) * scale);
				double dx = mouseMotionEvent.xrel();
				double dy = -mouseMotionEvent.yrel();
				if (dx != 0 || dy != 0) {
					accum_dx += dx;
					accum_dy += dy;
					last_x = x;
					last_y = y;
					long nanos = mouseMotionEvent.timestamp();
					if (grabbed) {
						putMouseMotionEvent(dx, dy, nanos);
					} else {
						putMouseMotionEvent(x, y, nanos);
					}
				}
			}
			case SDL_EVENT_WINDOW_MOUSE_ENTER -> isInsideWindow = true;
			case SDL_EVENT_WINDOW_MOUSE_LEAVE -> isInsideWindow = false;
		}
	}
}
