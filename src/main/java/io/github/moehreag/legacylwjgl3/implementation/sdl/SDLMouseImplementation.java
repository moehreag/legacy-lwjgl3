package io.github.moehreag.legacylwjgl3.implementation.sdl;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

import io.github.moehreag.legacylwjgl3.implementation.input.MouseImplementation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EventQueue;
import org.lwjgl.sdl.*;

import static org.lwjgl.sdl.SDLEvents.*;
import static org.lwjgl.sdl.SDLMouse.*;

/**
 * @author Zarzelcow
 * @created 28/09/2022 - 8:58 PM
 */
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
	private long last_event_nanos;
	private final SDL_MouseButtonEvent mouseButtonEvent = Display.getEvent().button();
	private final SDL_MouseMotionEvent mouseMotionEvent = Display.getEvent().motion();
	private final SDL_MouseWheelEvent mouseWheelEvent = Display.getEvent().wheel();

	@Override
	public void createMouse() {
		this.windowHandle = Display.getHandle();

        /*if (GLFW.glfwRawMouseMotionSupported() && !Mouse.getPrivilegedBoolean("org.lwjgl.input.Mouse.disableRawInput"))
            GLFW.glfwSetInputMode(this.windowHandle, GLFW.GLFW_RAW_MOUSE_MOTION, GLFW.GLFW_TRUE);

        this.buttonCallback = GLFWMouseButtonCallback.create((window, button, action, mods) -> {
            byte state = action == GLFW.GLFW_PRESS ? (byte)1 : (byte)0;
            putMouseEvent((byte) button, state, 0, System.nanoTime());
            if (button < button_states.length)
                button_states[button] = state;
        });
        this.posCallback = GLFWCursorPosCallback.create((window, xpos, ypos) -> {
            int x = (int) (xpos);
            int y = (int) (Display.getHeight() - ypos); // I don't know why but this un-inverts the y motion of mouse inputs
            double dx = x - last_x;
            double dy = y - last_y;
            if (dx != 0 || dy != 0) {
                accum_dx += dx;
                accum_dy += dy;
                last_x = x;
                last_y = y;
                long nanos = System.nanoTime();
                if (grabbed) {
                    putMouseEventWithCoords((byte)-1, (byte)0, dx, dy, 0, nanos);
                } else {
                    putMouseEventWithCoords((byte)-1, (byte)0, x, y, 0, nanos);
                }
            }
        });
        this.scrollCallback = GLFWScrollCallback.create((window, xoffset, yoffset) -> {
            accum_dz += yoffset;
            putMouseEvent((byte)-1, (byte)0, (int) yoffset, System.nanoTime());
        });
        this.cursorEnterCallback = GLFWCursorEnterCallback.create((window, entered) -> this.isInsideWindow = entered);*/
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
		last_event_nanos = nanos;
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
		this.last_x = x;
		this.last_y = y;
		SDL_WarpMouseInWindow(windowHandle, (float) x, (float) y);
	}

	@Override
	public void grabMouse(boolean grab) {
		if (!grab) {
			SDL_WarpMouseInWindow(windowHandle, (float) last_x, (float) last_y);
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

	@Override
	public void processMouseEvent(SDL_Event event) {
		switch (event.type()) {
			case SDL_EVENT_MOUSE_BUTTON_UP, SDL_EVENT_MOUSE_BUTTON_DOWN -> {
				byte state = mouseButtonEvent.down() ? (byte) 1 : (byte) 0;
				byte button = switch (mouseButtonEvent.button()) {
					// SDL has right & middle buttons switched
					case SDL_BUTTON_RIGHT -> 1;
					case SDL_BUTTON_MIDDLE -> 2;
					default -> (byte) (mouseButtonEvent.button()-1);
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
				int x = (int) (mouseMotionEvent.x());
				int y = (int) (Display.getHeight() - mouseMotionEvent.y());
				double dx = mouseMotionEvent.xrel();
				double dy = -mouseMotionEvent.yrel();
				if (dx != 0 || dy != 0) {
					accum_dx += dx;
					accum_dy += dy;
					last_x = x;
					last_y = y;
					long nanos = mouseMotionEvent.timestamp();
					if (grabbed) {
						putMouseEventWithCoords((byte) -1, (byte) 0, dx, dy, 0, nanos);
					} else {
						putMouseEventWithCoords((byte) -1, (byte) 0, x, y, 0, nanos);
					}
				}
			}
			case SDL_EVENT_WINDOW_MOUSE_ENTER -> isInsideWindow = true;
			case SDL_EVENT_WINDOW_MOUSE_LEAVE -> isInsideWindow = false;
		}
	}
}
