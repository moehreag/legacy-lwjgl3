package org.lwjgl.opengl;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Comparator;

import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import io.github.moehreag.legacylwjgl3.SDLPlatforms;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.sdl.SDL;
import org.lwjgl.sdl.SDLPlatform;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.sdl.SDL_PixelFormatDetails;
import org.lwjgl.system.Callback;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.sdl.SDLVideo.*;
import static org.lwjgl.sdl.SDLError.*;
import static org.lwjgl.sdl.SDLEvents.*;
import static org.lwjgl.sdl.SDLInit.*;
import static org.lwjgl.sdl.SDLKeycode.*;
import static org.lwjgl.sdl.SDLMouse.*;
import static org.lwjgl.sdl.SDLProperties.*;
import static org.lwjgl.sdl.SDLStdinc.*;
import static org.lwjgl.sdl.SDLVideo.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public final class Display {
	@NotNull
	private static String title = "";
	private static long handle = -1L, glContext = 0;
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
		SDL_SetMemoryFunctions(
				MemoryUtil::nmemAllocChecked,
				MemoryUtil::nmemCallocChecked,
				MemoryUtil::nmemReallocChecked,
				MemoryUtil::nmemFree
		);

		checkSdlError(SDL_SetAppMetadata("Minecraft", FabricLoader.getInstance().getModContainer("minecraft").orElseThrow(IllegalStateException::new)
				.getMetadata().getVersion().getFriendlyString(), "com.mojang.minecraft"));
		checkSdlError(SDL_SetAppMetadataProperty(SDL_PROP_APP_METADATA_URL_STRING, "https://minecraft.net"));
		checkSdlError(SDL_SetAppMetadataProperty(SDL_PROP_APP_METADATA_CREATOR_STRING, "Mojang AB"));
		checkSdlError(SDL_SetAppMetadataProperty(SDL_PROP_APP_METADATA_COPYRIGHT_STRING, "Minecraft EULA: https://minecraft.net/eula"));
		checkSdlError(SDL_SetAppMetadataProperty(SDL_PROP_APP_METADATA_TYPE_STRING, "game"));

		if (!SDL_Init(SDL_INIT_VIDEO)) {
			throw new IllegalStateException("Unable to initialize SDL" + SDL_GetError());
		}
	}

	@NotNull
	public static String getTitle() {
		return title;
	}

	public static void setTitle(@NotNull String title) {
		Display.title = title;
		if (isCreated()) {
			SDLVideo.SDL_SetWindowTitle(handle, title);
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
		try (var mode = SDLVideo.SDL_GetDesktopDisplayMode(SDLVideo.SDL_GetPrimaryDisplay())) {
			return new DisplayMode(mode.w(), mode.h(), SDL_PixelFormatDetails.mode.format());
		}
		long primaryMonitor = GLFW.glfwGetWindowMonitor(handle);
		if (primaryMonitor == 0) {
			primaryMonitor = GLFW.glfwGetPrimaryMonitor();
		}
		GLFWVidMode mode = GLFW.glfwGetVideoMode(primaryMonitor);
		if (mode == null) {
			return Arrays.stream(getAvailableDisplayModes()).max(Comparator.comparingInt(d -> d.getWidth() * d.getHeight())).orElse(null);
		}
		return new DisplayMode(mode.width(), mode.height(), mode.redBits() + mode.greenBits() + mode.blueBits(),
				mode.refreshRate());
	}


	public static int setIcon(@NotNull ByteBuffer[] icons) {
		SDLVideo.SDL_SetWindowIcon(handle, )

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
		SDL_PollEvent()
		GLFW.glfwPollEvents();
		if (Mouse.isCreated()) {
			Mouse.poll();
		}

		if (Keyboard.isCreated()) {
			Keyboard.poll();
		}

		checkSdlError(SDL_GL_SwapWindow(handle));
	}

	private static void checkSdlError(boolean success) {
		if (!success) {
			throw new IllegalStateException("SDL error encountered: " + SDL_GetError());
		}
	}

	private static long checkSdlError(long resultPointer) {
		if (resultPointer == 0) {
			throw new IllegalStateException("SDL error encountered: " + SDL_GetError());
		}
		return resultPointer;
	}

	public static void create(@NotNull PixelFormat pixelFormat) throws LWJGLException {
		windowedWidth = width = displayMode.getWidth();
		windowedHeight = height = displayMode.getHeight();
		long primaryMonitor = GLFW.glfwGetPrimaryMonitor();
		// Configure GLFW
		int props = SDL_CreateProperties();
		checkSdlError(SDL_SetNumberProperty(props, SDL_PROP_WINDOW_CREATE_X_NUMBER, SDL_WINDOWPOS_CENTERED));
		checkSdlError(SDL_SetNumberProperty(props, SDL_PROP_WINDOW_CREATE_Y_NUMBER, SDL_WINDOWPOS_CENTERED));
		checkSdlError(SDL_SetNumberProperty(props, SDL_PROP_WINDOW_CREATE_WIDTH_NUMBER, width));
		checkSdlError(SDL_SetNumberProperty(props, SDL_PROP_WINDOW_CREATE_HEIGHT_NUMBER, height));

		checkSdlError(SDL_SetStringProperty(props, SDL_PROP_WINDOW_CREATE_TITLE_STRING, title));
		checkSdlError(SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_OPENGL_BOOLEAN, true));
		if (!SDLPlatforms.MAC_OS.equals(SDLPlatform.SDL_GetPlatform())) { // macOS does not support the compat profile
			checkSdlError(SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 3));
			checkSdlError(SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 2));
			checkSdlError(SDL_GL_SetAttribute(SDL_GL_CONTEXT_PROFILE_MASK, SDL_GL_CONTEXT_PROFILE_COMPATIBILITY));
		}
		checkSdlError(SDL_GL_SetAttribute(SDL_GL_DOUBLEBUFFER, 1));
		checkSdlError(SDL_GL_SetAttribute(SDL_GL_ALPHA_SIZE, pixelFormat.getAlphaBits()));
		checkSdlError(SDL_GL_SetAttribute(SDL_GL_DEPTH_SIZE, pixelFormat.getDepthBits()));
		checkSdlError(SDL_GL_SetAttribute(SDL_GL_STENCIL_SIZE, pixelFormat.getStencilBits()));
		checkSdlError(SDL_GL_SetAttribute(SDL_GL_STEREO, pixelFormat.isStereo() ? 1 : 0));

		checkSdlError(SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_HIDDEN_BOOLEAN, true));
		checkSdlError(SDL_SetBooleanProperty(props, SDL_PROP_WINDOW_CREATE_RESIZABLE_BOOLEAN, resizable));
		handle = checkSdlError(SDLVideo.SDL_CreateWindowWithProperties(props));
		SDL_DestroyProperties(props);

		glContext = checkSdlError(SDL_GL_CreateContext(handle));
		checkSdlError(SDL_GL_LoadLibrary((ByteBuffer) null));
//		SDL_GL_MakeCurrent(handle, glContext);
		Configuration.OPENGL_EXPLICIT_INIT.set(true);
		GL.create(SDLVideo::SDL_GL_GetProcAddress);
		GL.createCapabilities(MemoryUtil::memCallocPointer);

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
		try (MemoryStack ms = stackPush()) {
			IntBuffer width = ms.mallocInt(1);
			IntBuffer height = ms.mallocInt(1);
			checkSdlError(SDL_GetWindowSizeInPixels(handle, width, height));
			framebufferWidth = Math.min(1, width.get(0));
			framebufferHeight = Math.min(1, height.get(0));
		}

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
		checkSdlError(SDL_ShowWindow(handle));
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
		long primaryMonitor = GLFW.glfwGetWindowMonitor(handle);
		if (primaryMonitor == 0) {
			primaryMonitor = GLFW.glfwGetPrimaryMonitor();
		}
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
		Keyboard.destroy();
		Mouse.destroy();
		memFree(GL.getCapabilities().getAddressBuffer());
		GL.setCapabilities(null);
		GL.destroy();
		if (glContext != 0) {
			SDL_GL_DestroyContext(glContext);
			glContext = 0;
		}
		if (handle != 0) {
			SDL_DestroyWindow(handle);
			handle = 0;
		}
		if (SDL_WasInit(SDL_INIT_VIDEO) != 0) {
			SDL_QuitSubSystem(SDL_INIT_VIDEO);
		}
		SDL_Quit();
		try (MemoryStack stack = stackPush()) {
			PointerBuffer funcs = stack.mallocPointer(4);

			nSDL_GetMemoryFunctions(
					memAddress(funcs, 0),
					memAddress(funcs, 1),
					memAddress(funcs, 2),
					memAddress(funcs, 3)
			);

			for (int i = 0; i < 4; i++) {
				Callback.free(funcs.get(i));
			}
		}
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
