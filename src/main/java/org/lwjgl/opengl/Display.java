package org.lwjgl.opengl;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;

import io.github.moehreag.legacylwjgl3.DesktopFileInjector;
import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import lombok.Getter;
import lombok.Setter;
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
	private static int width, height,
			framebufferWidth, framebufferHeight,
			windowedWidth, windowedHeight;
	@Getter
	@Setter
	private static int x;
	private static int windowedX;
	@Setter
	@Getter
	private static int y;
	private static int windowedY;
	private static boolean window_resized = true;
	private static boolean minimized;
	@Getter
	private static boolean iconified;
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
		return framebufferWidth;
	}

	public static void setWidth(int width) {
		Display.framebufferWidth = width;
	}

	public static int getHeight() {
		return framebufferHeight;
	}

	public static void setHeight(int height) {
		Display.framebufferHeight = height;
	}

	public static void setScreenWidth(int width) {
		Display.width = width;
	}

	public static int getScreenWidth() {
		return width;
	}

	public static void setScreenHeight(int height) {
		Display.height = height;
	}

	public static int getScreenHeight() {
		return height;
	}

	@Nullable
	public static DisplayMode getDesktopDisplayMode() {
		long primaryMonitor = GLFW.glfwGetPrimaryMonitor();
		GLFWVidMode mode = GLFW.glfwGetVideoMode(primaryMonitor);
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
		windowedWidth = width = displayMode.getWidth();
		windowedHeight = height = displayMode.getHeight();
		long primaryMonitor = GLFW.glfwGetPrimaryMonitor();
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

		GLFW.glfwMakeContextCurrent(handle);
		GL.createCapabilities();

		if (primaryMonitor != 0) {
			var mode = GLFW.glfwGetVideoMode(primaryMonitor);
			var xBox = new int[1];
			var yBox = new int[1];
			GLFW.glfwGetMonitorPos(primaryMonitor, xBox, yBox);
			windowedX = x = xBox[0] + mode.width() / 2 - width / 2;
			windowedY = y = yBox[0] + mode.height() / 2 - height / 2;
		} else if (GLFW.glfwGetPlatform() != GLFW.GLFW_PLATFORM_WAYLAND) {
			var xBox = new int[1];
			var yBox = new int[1];
			GLFW.glfwGetWindowPos(handle, xBox, yBox);
			windowedX = x = xBox[0];
			windowedY = y = yBox[0];
		}
		setFullscreen(false);
		int[] xBox = new int[1];
		int[] yBox = new int[1];
		GLFW.glfwGetFramebufferSize(handle, xBox, yBox);
		framebufferWidth = xBox[0] <= 0 ? 1 : xBox[0];
		framebufferHeight = yBox[0] <= 0 ? 1 : yBox[0];

		// create general callbacks
		GLFW.glfwSetWindowSizeCallback(handle, GLFWWindowSizeCallback.create(Display::resizeCallback));
		GLFW.glfwSetFramebufferSizeCallback(handle, GLFWFramebufferSizeCallback.create(Display::onFramebufferResize));
		GLFW.glfwSetWindowFocusCallback(handle, (window, focused1) -> {
			if (window == handle) {
				focused = focused1;
			}
		});
		GLFW.glfwSetWindowIconifyCallback(handle, GLFWWindowIconifyCallback.create(Display::onIconify));
		GLFW.glfwSetWindowPosCallback(handle, GLFWWindowPosCallback.create((window, xpos, ypos) -> {
			x = xpos;
			y = ypos;
		}));
		Mouse.create();
		Keyboard.create();
		GLFW.glfwShowWindow(handle);
		if (cached_icons != null) {
			setIcon(cached_icons);
		}
	}

	private static void onIconify(long window, boolean iconified) {
		Display.iconified = iconified;
	}

	private static void onFramebufferResize(long window, int framebufferWidth, int framebufferHeight) {
		if (window != handle) return;
		int prevWidth = Display.framebufferWidth;
		int prevHeight = Display.framebufferHeight;
		if (framebufferWidth != 0 && framebufferHeight != 0) {
			minimized = false;
			Display.framebufferWidth = framebufferWidth;
			Display.framebufferHeight = framebufferHeight;
			if (Display.framebufferWidth != prevWidth || Display.framebufferHeight != prevHeight) {
				window_resized = true;
			}
		} else {
			minimized = true;
		}
	}

	public static void setFullscreen(boolean fullscreen) {

		try {
			boolean isFullscreen = GLFW.glfwGetWindowMonitor(handle) != 0;

			if (fullscreen) {
				var monitor = getPrimaryMonitor();
				if (monitor == 0) {
					LegacyLWJGL3.LOGGER.warn("Failed to find monitor for fullscreen");
					return;
				}
				if (!isFullscreen) {
					windowedX = x;
					windowedY = y;
					windowedWidth = width;
					windowedHeight = height;
				}
				x = 0;
				y = 0;
				var mode = GLFW.glfwGetVideoMode(monitor);
				width = mode.width();
				height = mode.height();
				GLFW.glfwSetWindowMonitor(getHandle(),
						monitor,
						x,
						y,
						width,
						height,
						mode.refreshRate());
			} else {
				x = windowedX;
				y = windowedY;
				width = windowedWidth;
				height = windowedHeight;
				GLFW.glfwSetWindowMonitor(getHandle(),
						0L,
						x,
						y,
						width,
						height,
						GLFW.GLFW_DONT_CARE);
			}
			window_resized = true;

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private static int clamp(int value, int min, int max) {
		return value < min ? min : Math.min(value, max);
	}

	private static long getPrimaryMonitor() {
		long l = GLFW.glfwGetWindowMonitor(handle);
		if (l != 0L) {
			return l;
		} else {
			int xStart = x;
			int xEnd = xStart + getScreenWidth();
			int yStart = y;
			int yEnd = yStart + getScreenHeight();
			int largestArea = -1;
			long monitor = 0;
			long primary = GLFW.glfwGetPrimaryMonitor();
			var buf = GLFW.glfwGetMonitors();
			if (buf == null) return 0;
			for (int i = 0; i < buf.limit(); i++) {
				long monitor2 = buf.get(i);
				int[] posXBox = new int[1], posYBox = new int[1];
				GLFW.glfwGetMonitorPos(monitor2, posXBox, posYBox);
				var currentMode = GLFW.glfwGetVideoMode(monitor2);
				int monitorXStart = posXBox[0];
				int monitorXEnd = monitorXStart + currentMode.width();
				int monitorYStart = posYBox[0];
				int monitorYEnd = monitorYStart + currentMode.height();
				int left = clamp(xStart, monitorXStart, monitorXEnd);
				int right = clamp(xEnd, monitorXStart, monitorXEnd);
				int top = clamp(yStart, monitorYStart, monitorYEnd);
				int bottom = clamp(yEnd, monitorYStart, monitorYEnd);
				int maxWidth = Math.max(0, right - left);
				int maxHeight = Math.max(0, bottom - top);
				int maxArea = maxWidth * maxHeight;
				if (maxArea > largestArea) {
					monitor = monitor2;
					largestArea = maxArea;
				} else if (maxArea == largestArea && primary == monitor2) {
					monitor = monitor2;
				}
			}

			return monitor;
		}
	}

	@NotNull
	public static DisplayMode[] getAvailableDisplayModes() {
		long primaryMonitor = GLFW.glfwGetPrimaryMonitor();
		if (primaryMonitor == 0) {
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
		return !minimized;
	}

	private static void resizeCallback(long window, int width, int height) {
		if (window == handle) {
			window_resized = true;
			Display.width = width;
			Display.height = height;
		}
	}
}
