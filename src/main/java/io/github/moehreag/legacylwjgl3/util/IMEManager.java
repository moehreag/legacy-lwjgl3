package io.github.moehreag.legacylwjgl3.util;

import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import io.github.moehreag.legacylwjgl3.api.PreeditAwareWidget;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;
import net.ornithemc.osl.lifecycle.impl.client.MinecraftAccess;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.Display;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDL_Rect;
import org.lwjgl.system.MemoryStack;

public class IMEManager {
	private boolean textInputEnabled;
	private boolean imeRequested;
	private volatile boolean imeStatusChanged = true;
	private boolean cachedIMEStatus;
	private PreeditAwareWidget currentWidget;
	private IMEPreeditOverlay overlay;
	private PreeditEvent lastEvent;

	private static final IMEManager INSTANCE = new IMEManager();

	private IMEManager() {
		MinecraftClientEvents.TICK_END.register(mc -> tick());
	}

	public static IMEManager getInstance() {
		return INSTANCE;
	}

	public void submitPreeditEvent(PreeditEvent event) {
		lastEvent = event;
		if (MinecraftAccess.getInstance().screen != null) {
			if (currentWidget != null) {
				overlay = event != null ? new IMEPreeditOverlay(event, MinecraftAccess.getInstance().textRenderer, currentWidget.getInputHeight()+1) : null;
			}
		}
	}

	public void renderPreeditOverlay(int guiScale, int windowWidth, int windowHeight) {
		if (overlay != null) {
			overlay.updateInputPosition(currentWidget.getCursorX(), currentWidget.getCursorY());
			overlay.render(guiScale, windowWidth, windowHeight);
		}
	}

	public void setTextInputArea(final int x0, final int y0, final int x1, final int y1, int guiScale) {
		if (LegacyLWJGL3.USE_SDL) {
			try (var stack = MemoryStack.stackPush()) {
				var rect = SDL_Rect.malloc(1, stack);
				rect.get(0).set(x0 * guiScale, y0 * guiScale, (x1 - x0) * guiScale, (y1 - y0) * guiScale);
				SDLKeyboard.SDL_SetTextInputArea(Display.getHandle(), rect, 0);
			}
		} else {
			GLFW.glfwSetPreeditCursorRectangle(Display.getHandle(), x0 * guiScale, y0 * guiScale, (x1 - x0) * guiScale, (y1 - y0) * guiScale);
		}
	}

	public void notifyIMEChanged() {
		this.imeStatusChanged = true;
	}

	public void tick() {
		if (this.textInputEnabled) {
			this.tickDuringTextInput();
		} else {
			this.tickOutsideTextInput();
		}
	}

	private boolean getIMEStatus() {
		if (this.imeStatusChanged) {
			this.imeStatusChanged = false;
			this.cachedIMEStatus = GLFW.glfwGetInputMode(Display.getHandle(), GLFW.GLFW_IME) == 1;
		}

		return this.cachedIMEStatus;
	}

	private void tickOutsideTextInput() {
		if (Display.isActive() && this.getIMEStatus()) {
			this.setIMEInputMode(false);
		}
	}

	private void tickDuringTextInput() {
		this.imeRequested = this.getIMEStatus();
	}

	public void startTextInput() {
		this.textInputEnabled = true;
		if (LegacyLWJGL3.USE_SDL || this.imeRequested) {
			this.setIMEInputMode(true);
		}
	}

	public void stopTextInput() {
		this.textInputEnabled = false;
	}

	public void onWidgetFocusUpdate(PreeditAwareWidget widget, boolean focused) {
		if (focused) {
			this.currentWidget = widget;
			this.startTextInput();
		} else {
			if (currentWidget == widget) {
				this.currentWidget = null;
			}
			this.stopTextInput();
		}
		if (MinecraftAccess.getInstance().screen != null) {
			if (focused) {
				submitPreeditEvent(lastEvent);
			} else {
				submitPreeditEvent(null);
			}
		}
	}

	private void setIMEInputMode(final boolean value) {
		if (LegacyLWJGL3.USE_SDL) {
			if (value) {
				SDLKeyboard.SDL_StartTextInput(Display.getHandle());
			} else {
				SDLKeyboard.SDL_StopTextInput(Display.getHandle());
			}
		} else {
			GLFW.glfwSetInputMode(Display.getHandle(), 208903, value ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
		}
	}
}
