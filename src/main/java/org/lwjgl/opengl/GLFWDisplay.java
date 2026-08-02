package org.lwjgl.opengl;

import java.nio.ByteBuffer;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.IntStream;

import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import io.github.moehreag.legacylwjgl3.LegacyLWJGL3ScreenEx;
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

public final class GLFWDisplay implements Display.Impl {
	@NotNull
	private String title = "";
	@Setter
	@Getter
	private long handle = -1L;
	private boolean resizable;
	private int width = 640, height = 480,
			framebufferWidth = 640, framebufferHeight = 480,
			windowedWidth, windowedHeight;
	@Getter
	@Setter
	private int x;
	private int windowedX;
	@Setter
	@Getter
	private int y;
	private int windowedY;
	private float scale;
	private boolean window_resized = true;
	private boolean minimized;
	@Getter
	private boolean iconified;
	@Nullable
	private ByteBuffer[] cached_icons = null;
	private boolean focused;
	private final Drawable drawable = new NoOpImpl();
	private boolean useFullscreenDeferred;

	GLFWDisplay() {
		GLFWErrorCallback.createPrint(System.err).set();
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}
	}

	@NotNull
	public String getTitle() {
		return title;
	}

	public void setTitle(@NotNull String title) {
		this.title = title;
		if (isCreated()) {
			GLFW.glfwSetWindowTitle(handle, title);
		}
	}

	@NotNull
	public DisplayMode getDisplayMode() {
		return new DisplayMode(framebufferWidth, framebufferHeight, 24, 60);
	}

	public void setDisplayMode(@NotNull DisplayMode mode) {
		setWidth(mode.getWidth());
		setScreenWidth(mode.getWidth());
		setHeight(mode.getHeight());
		setScreenHeight(mode.getHeight());
		windowedWidth = mode.getWidth();
		windowedHeight = mode.getHeight();
		window_resized = true;
	}

	public int getWidth() {
		return framebufferWidth;
	}

	public void setWidth(int width) {
		this.framebufferWidth = width;
	}

	public int getHeight() {
		return framebufferHeight;
	}

	public void setHeight(int height) {
		this.framebufferHeight = height;
	}

	public void setScreenWidth(int width) {
		this.width = width;
	}

	public int getScreenWidth() {
		return width;
	}

	public void setScreenHeight(int height) {
		this.height = height;
	}

	public int getScreenHeight() {
		return height;
	}

	public float getPixelScaleFactor() {
		return scale;
	}

	@Nullable
	public DisplayMode getDesktopDisplayMode() {
		long primaryMonitor = GLFW.glfwGetPrimaryMonitor();
		GLFWVidMode mode = GLFW.glfwGetVideoMode(primaryMonitor);
		if (mode == null) {
			return Arrays.stream(getAvailableDisplayModes()).max(Comparator.comparingInt(d -> d.getWidth() * d.getHeight())).orElse(null);
		}
		return new DisplayMode(mode.width(), mode.height(), mode.redBits() + mode.greenBits() + mode.blueBits(),
				mode.refreshRate());
	}


	public int setIcon(ByteBuffer[] icons) {

		if (GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND) {
			return 0;
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

	public void update() {
		GLFW.glfwPollEvents();
		if (Mouse.isCreated()) {
			Mouse.poll();
		}

		if (Keyboard.isCreated()) {
			Keyboard.poll();
		}

		GLFW.glfwSwapBuffers(handle);
	}

	public void create(@NotNull PixelFormat pixelFormat) throws LWJGLException {
		windowedWidth = width;
		windowedHeight = height;
		// Configure GLFW
		GLFW.glfwDefaultWindowHints();

		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_NATIVE_CONTEXT_API);
		if (GLFW.glfwGetPlatform() != GLFW.GLFW_PLATFORM_COCOA) { // macOS does not support the compat profile
			GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
			GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
			GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_COMPAT_PROFILE);
		}
		GLFW.glfwWindowHint(GLFW.GLFW_SCALE_FRAMEBUFFER, LegacyLWJGL3.SCALE_FRAMEBUFFER ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_ALPHA_BITS, pixelFormat.getAlphaBits());
		GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, pixelFormat.getDepthBits());
		GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, pixelFormat.getStencilBits());
		GLFW.glfwWindowHint(GLFW.GLFW_STEREO, pixelFormat.isStereo() ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);

		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, 0);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, resizable ? 1 : 0);
		handle = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);

		GLFW.glfwMakeContextCurrent(handle);
		GL.createCapabilities();

		long primaryMonitor = GLFW.glfwGetPrimaryMonitor();
		if (GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND) {
			windowedX = windowedY = x = y = -1;
		} else if (primaryMonitor != 0) {
			var mode = GLFW.glfwGetVideoMode(primaryMonitor);
			var xBox = new int[1];
			var yBox = new int[1];
			GLFW.glfwGetMonitorPos(primaryMonitor, xBox, yBox);
			windowedX = x = xBox[0] + mode.width() / 2 - width / 2;
			windowedY = y = yBox[0] + mode.height() / 2 - height / 2;
		} else {
			var xBox = new int[1];
			var yBox = new int[1];
			GLFW.glfwGetWindowPos(handle, xBox, yBox);
			windowedX = x = xBox[0];
			windowedY = y = yBox[0];
		}

		int[] xBox = new int[1];
		int[] yBox = new int[1];
		GLFW.glfwGetWindowSize(handle, xBox, yBox);
		framebufferWidth = xBox[0] <= 0 ? 1 : xBox[0];
		framebufferHeight = yBox[0] <= 0 ? 1 : yBox[0];
		updateScaleFactor();

		// create general callbacks
		GLFW.glfwSetWindowSizeCallback(handle, GLFWWindowSizeCallback.create(this::onWindowResize));
		GLFW.glfwSetFramebufferSizeCallback(handle, GLFWFramebufferSizeCallback.create(this::onFramebufferResize));
		GLFW.glfwSetWindowFocusCallback(handle, (window, focused1) -> {
			if (window == handle) {
				focused = focused1;
			}
		});
		GLFW.glfwSetWindowIconifyCallback(handle, GLFWWindowIconifyCallback.create((window1, iconified1) -> this.iconified = iconified1));
		GLFW.glfwSetWindowPosCallback(handle, GLFWWindowPosCallback.create((window, xpos, ypos) -> {
			x = xpos;
			y = ypos;
		}));
		GLFW.glfwSetDropCallback(handle, GLFWDropCallback.create((window, count, names) -> {
			var dropped = IntStream.range(0, count).mapToObj(i -> GLFWDropCallback.getName(names, i))
					.map(s -> {
						try {
							return Path.of(s);
						} catch (InvalidPathException e) {
							LegacyLWJGL3.LOGGER.warn("Failed to parse dropped path! '{}'", s, e);
						}
						return null;
					}).filter(Objects::nonNull).toList();

			if (!dropped.isEmpty()) {
				LegacyLWJGL3ScreenEx.handleFileDrop(dropped);
			}
		}));
		Mouse.create();
		Keyboard.create();
		GLFW.glfwShowWindow(handle);
		setFullscreen(useFullscreenDeferred);
		if (cached_icons != null) {
			setIcon(cached_icons);
		}
	}

	private void onFramebufferResize(long window, int framebufferWidth, int framebufferHeight) {
		if (window != handle) return;
		int prevWidth = this.framebufferWidth;
		int prevHeight = this.framebufferHeight;
		if (framebufferWidth != 0 && framebufferHeight != 0) {
			minimized = false;
			this.framebufferWidth = framebufferWidth;
			this.framebufferHeight = framebufferHeight;
			if (this.framebufferWidth != prevWidth || this.framebufferHeight != prevHeight) {
				window_resized = true;
			}
			this.updateScaleFactor();
		} else {
			minimized = true;
		}
	}

	public void setFullscreen(boolean fullscreen) {
		if (!isCreated()) {
			useFullscreenDeferred = fullscreen;
			return;
		}

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
			LegacyLWJGL3.LOGGER.warn("Failed to set fullscreen", t);
		}
	}

	private int clamp(int value, int min, int max) {
		return value < min ? min : Math.min(value, max);
	}

	private long getPrimaryMonitor() {
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
				int left = xStart == -1 ? monitorXStart : clamp(xStart, monitorXStart, monitorXEnd);
				int right = xStart == -1 ? monitorXEnd : clamp(xEnd, monitorXStart, monitorXEnd);
				int top = yStart == -1 ? monitorYStart : clamp(yStart, monitorYStart, monitorYEnd);
				int bottom = yStart == -1 ? monitorYEnd : clamp(yEnd, monitorYStart, monitorYEnd);
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
	public DisplayMode[] getAvailableDisplayModes() {
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

	public void destroy() {
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

	public boolean isCreated() {
		return handle != -1L;
	}

	public boolean isCloseRequested() {
		return GLFW.glfwWindowShouldClose(handle);
	}

	public boolean isActive() {
		return focused;
	}

	public void setResizable(boolean isResizable) {
		resizable = isResizable;
		if (isCreated()) {
			GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, resizable ? 1 : 0);
		}
	}

	public void setVSyncEnabled(boolean enabled) {
		if (GLFW.glfwGetCurrentContext() != 0) {
			GLFW.glfwSwapInterval(enabled ? 1 : 0);
		}
	}

	public boolean wasResized() {
		var bl = window_resized;
		window_resized = false;
		return bl;
	}

	public boolean isVisible() {
		return !minimized;
	}

	private void onWindowResize(long window, int width, int height) {
		if (window == handle) {
			window_resized = true;
			this.width = width;
			this.height = height;
			this.updateScaleFactor();
		}
	}

	private void updateScaleFactor() {
		if (framebufferHeight != 0 && framebufferWidth != 0 && width != 0 && height != 0) {
			float xscale = framebufferWidth * 1.0f / width;
			float yscale = framebufferHeight * 1.0f / height;
			this.scale = Math.max(xscale, yscale);
		}
	}

	public void makeCurrent() {
		GLFW.glfwMakeContextCurrent(handle);
	}

	public void swapBuffers() {
		GLFW.glfwSwapBuffers(handle);
	}

	@Override
	public Drawable getDrawable() {
		return drawable;
	}

	class NoOpImpl implements Drawable {
		private long prevHandle = -1L;

		@Override
		public void makeCurrent() throws LWJGLException {
			if (isCreated()) {
				prevHandle = Display.getHandle();
				Display.create();
			}
			GLFWDisplay.this.makeCurrent();
		}

		@Override
		public void releaseContext() {
			GLFW.glfwMakeContextCurrent(0);
		}

		@Override
		public void destroy() {
			GLFW.glfwDestroyWindow(Display.getHandle());
			if (prevHandle != -1L) {
				Display.setHandle(prevHandle);
			}
		}
	}
}