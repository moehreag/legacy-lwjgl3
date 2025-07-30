package org.lwjgl.opengl;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;

import io.github.moehreag.legacylwjgl3.DesktopFileInjector;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.LWJGLException;
import org.lwjgl.glfw.*;
import org.lwjgl.glfw.GLFWImage.Buffer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public final class Display {
	@NotNull
	private static String title = "";
	private static long handle = -1L;
	private static boolean resizable;
	@NotNull
	private static DisplayMode displayMode = new DisplayMode(640, 480, 24, 60);
	private static int scaledWidth;
	private static int scaledHeight;
	@Getter
	private static float xScale = 1, yScale = 1;
	private static int unscaledWidth, unscaledHeight;
	private static int xPos;
	private static int yPos;
	private static boolean window_resized = true;
	@Nullable
	private static GLFWWindowSizeCallback sizeCallback;
	@Nullable
	private static ByteBuffer[] cached_icons = null;
	private static boolean focused;

	private Display() {
	}

	static {
		GLFWErrorCallback.createPrint(System.err).set();
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}
	}

	@NotNull
	public static String getTitle() {
		return title;
	}

	public static void setTitle(@NotNull String title) {
		Display.title = title;
		if (isCreated()) {
			GLFW.glfwSetWindowTitle(handle, title);
		}
	}

	public static long getHandle() {
		return handle;
	}

	public static void setHandle(long handle) {
		Display.handle = handle;
	}

	@NotNull
	public static DisplayMode getDisplayMode() {
		return displayMode;
	}

	public static void setDisplayMode(@NotNull DisplayMode mode) {
		displayMode = mode;
	}

	public static int getWidth() {
		return scaledWidth;
	}

	public static void setWidth(int width) {
		Display.scaledWidth = width;
	}

	public static int getHeight() {
		return scaledHeight;
	}

	public static void setHeight(int height) {
		Display.scaledHeight = height;
	}

	public static int getXPos() {
		return xPos;
	}

	public static void setXPos(int XPos) {
		xPos = XPos;
	}

	public static int getYPos() {
		return yPos;
	}

	public static void setYPos(int YPos) {
		yPos = YPos;
	}

	@Nullable
	public static DisplayMode getDesktopDisplayMode() {
		long mon = GLFW.glfwGetPrimaryMonitor();
		GLFWVidMode mode = GLFW.glfwGetVideoMode(mon);
		if (mode == null) {
			return Arrays.stream(getAvailableDisplayModes()).max(Comparator.comparingInt(d -> d.getWidth() * d.getHeight())).orElse(null);
		}
		return new DisplayMode(mode.width(), mode.height(), mode.redBits() + mode.greenBits() + mode.blueBits(),
				mode.refreshRate());
	}


	public static int setIcon(@NotNull ByteBuffer[] icons) {

		if (GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND) {
			// Wayland does not have a standardised way of setting window icons, see
			// https://www.glfw.org/docs/latest/group__window.html#gadd7ccd39fe7a7d1f0904666ae5932dc5
			// for more information.
			return DesktopFileInjector.setIcon(icons);
		}

		// LWJGL2 doesn't enforce this to be called after window creation,
		// meaning you have to keep hold the icons to use them when the window is created
		if (!Arrays.equals(cached_icons, icons)) {
			// you have to also clone the byte buffers to avoid seg faults from them being freed
			cached_icons = Arrays.stream(icons).map(buf -> {
				ByteBuffer copy = ByteBuffer.allocate(buf.capacity());
				int old_pos = buf.position();
				copy.put(buf);
				buf.position(old_pos);
				copy.flip();
				return copy;
			}).toArray(ByteBuffer[]::new);
		}

		if (isCreated() && GLFW.glfwGetPlatform() != GLFW.GLFW_PLATFORM_COCOA) {
			try (MemoryStack memoryStack = MemoryStack.stackPush()) {
				Buffer buffer = GLFWImage.malloc(icons.length, memoryStack);

				for (int j = 0; j < icons.length; j++) {
					var buf = icons[j];

					int size = (int) Math.sqrt(buf.limit() / 4f);
					ByteBuffer byteBuffer = memoryStack.malloc(buf.limit()).put(buf).flip(); // have to copy the buffer from a heap buffer to a direct (off-heap) buffer
					buffer.position(j).width(size).height(size).pixels(byteBuffer);
				}

				GLFW.glfwSetWindowIcon(handle, buffer);
			}
			return 1;
		} else {
			return 0;
		}
	}

	public static void update() {
		window_resized = false;
		GLFW.glfwPollEvents();
		if (Mouse.isCreated()) {
			Mouse.poll();
		}

		if (Keyboard.isCreated()) {
			Keyboard.poll();
		}

		GLFW.glfwSwapBuffers(handle);
	}

	public static void create(@NotNull PixelFormat pixelFormat) throws LWJGLException {
		// Configure GLFW
		GLFW.glfwDefaultWindowHints();

		if (GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND) {
			DesktopFileInjector.inject();
			GLFW.glfwWindowHintString(GLFW.GLFW_WAYLAND_APP_ID, DesktopFileInjector.APP_ID);
		}

		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_NATIVE_CONTEXT_API);
		if (GLFW.glfwGetPlatform() != GLFW.GLFW_PLATFORM_COCOA) { // macOS does not support the compat profile
			GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
			GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
			GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_COMPAT_PROFILE);
		}
		GLFW.glfwWindowHint(GLFW.GLFW_ALPHA_BITS, pixelFormat.getAlphaBits());
		GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, pixelFormat.getDepthBits());
		GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, pixelFormat.getStencilBits());
		GLFW.glfwWindowHint(GLFW.GLFW_STEREO, pixelFormat.isStereo() ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);

		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, 0);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, resizable ? 1 : 0);
		handle =
				GLFW.glfwCreateWindow(displayMode.getWidth(), displayMode.getHeight(), title, MemoryUtil.NULL, MemoryUtil.NULL);
		unscaledWidth = displayMode.getWidth();
		unscaledHeight = displayMode.getHeight();
		GLFW.glfwMakeContextCurrent(handle);
		GL.createCapabilities();
		// create general callbacks
		sizeCallback = GLFWWindowSizeCallback.create(Display::resizeCallback);
		GLFW.glfwSetWindowSizeCallback(handle, sizeCallback);
		GLFW.glfwSetWindowFocusCallback(handle, (window, focused1) -> {
			if (window == handle) {
				focused = focused1;
			}
		});
		GLFW.glfwSetWindowContentScaleCallback(handle, GLFWWindowContentScaleCallback.create((window, xscale, yscale) -> {
			if (window != handle) return;
			xScale = xscale;
			yScale = yscale;
			resizeCallback(handle, unscaledWidth, unscaledHeight);
		}));
		Mouse.create();
		Keyboard.create();
		GLFW.glfwShowWindow(handle);
		if (cached_icons != null) {
			setIcon(cached_icons);
		}
	}

	public static void setFullscreen(boolean fullscreen) {

		try {
			resizeCallback(handle, displayMode.getWidth(), displayMode.getHeight());

			if (fullscreen) {
				long monitor = GLFW.glfwGetWindowMonitor(handle);
				if (monitor == 0L) {
					monitor = GLFW.glfwGetPrimaryMonitor();
				}
				GLFW.glfwSetWindowMonitor(getHandle(),
						monitor,
						0,
						0,
						getWidth(),
						getHeight(),
						getDisplayMode().getFrequency());
				setXPos(getDisplayMode().getWidth() / 2);
				setYPos(getDisplayMode().getHeight() / 2);
			} else {
				setXPos(getXPos() - getWidth() / 2);
				setYPos(getYPos() - getHeight() / 2);
				GLFW.glfwSetWindowMonitor(getHandle(),
						0L,
						getXPos(), // need a xPos
						getYPos(), // need a yPos
						getWidth(),
						getHeight(),
						-1);
			}

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@NotNull
	public static DisplayMode[] getAvailableDisplayModes() {
		long primaryMonitor = GLFW.glfwGetPrimaryMonitor();
		if (primaryMonitor == MemoryUtil.NULL) {
			return new DisplayMode[0];
		} else {
			GLFWVidMode.Buffer videoModes = GLFW.glfwGetVideoModes(primaryMonitor);
			if (videoModes == null) {
				throw new IllegalStateException("No video modes found");
			} else {
				return videoModes.stream().map(mode -> new DisplayMode(mode.width(),
						mode.height(), mode.redBits() + mode.blueBits() + mode.greenBits(),
						mode.refreshRate())).toArray(DisplayMode[]::new);
			}
		}
	}

	public static void destroy() {
		// free callbacks
		Callbacks.glfwFreeCallbacks(handle);
		GLFWErrorCallback callback = GLFW.glfwSetErrorCallback(null);
		if (callback != null) {
			callback.free();
		}
		// Destroy the window
		GLFW.glfwDestroyWindow(handle);
		GLFW.glfwTerminate();
	}

	public static boolean isCreated() {
		return handle != -1L;
	}

	public static boolean isCloseRequested() {
		return GLFW.glfwWindowShouldClose(handle);
	}

	public static boolean isActive() {
		return focused;
	}

	public static void setResizable(boolean isResizable) {
		resizable = isResizable;
		if (isCreated()) {
			GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, resizable ? 1 : 0);
		}
	}

	public static void sync(int fps) {
		Sync.sync(fps);
	}


	public static void setVSyncEnabled(boolean enabled) {
		if (GLFW.glfwGetCurrentContext() != 0) {
			GLFW.glfwSwapInterval(enabled ? 1 : 0);
		}
	}

	public static boolean wasResized() {
		return window_resized;
	}

	public static boolean isVisible() {
		return GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_VISIBLE) != 0;
	}

	private static void resizeCallback(long window, int width, int height) {
		if (window == handle) {
			window_resized = true;
			Display.unscaledWidth = width;
			Display.unscaledHeight = height;
			scaledWidth = (int) (width*xScale);
			scaledHeight = (int) (height*yScale);
		}
	}
}
