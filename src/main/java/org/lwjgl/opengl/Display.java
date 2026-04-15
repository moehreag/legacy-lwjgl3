package org.lwjgl.opengl;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import io.github.moehreag.legacylwjgl3.LegacyLWJGL3ScreenEx;
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
import org.lwjgl.sdl.*;
import org.lwjgl.system.Callback;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.sdl.SDLError.SDL_GetError;
import static org.lwjgl.sdl.SDLEvents.*;
import static org.lwjgl.sdl.SDLInit.*;
import static org.lwjgl.sdl.SDLProperties.*;
import static org.lwjgl.sdl.SDLStdinc.SDL_SetMemoryFunctions;
import static org.lwjgl.sdl.SDLStdinc.nSDL_GetMemoryFunctions;
import static org.lwjgl.sdl.SDLVideo.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memFree;

@SuppressWarnings("unused")
public final class Display {
	@NotNull
	private static String title = "";
	@Getter
	private static long handle = -1L;
	private static long glContext = 0;
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
	private static boolean isFullscreen;
	private static ByteBuffer @Nullable [] cached_icons = null;
	private static boolean focused;
	@Getter
	private static boolean closeRequested;
	@Getter
	private static final SDL_Event event = SDL_Event.calloc();
	private static final SDL_WindowEvent windowEvent = event.window();
	private static final SDL_DropEvent dropEvent = event.drop();
	private static final List<Path> currentEventDrops = new ArrayList<>(2);

	private Display() {
	}

	static {
		SDL_SetMemoryFunctions(
				MemoryUtil::nmemAllocChecked,
				MemoryUtil::nmemCallocChecked,
				MemoryUtil::nmemReallocChecked,
				MemoryUtil::nmemFree
		);

		checkSdlError(SDLHints.SDL_SetHint(SDLHints.SDL_HINT_MOUSE_FOCUS_CLICKTHROUGH, "1"));
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
			SDL_SetWindowTitle(handle, title);
		}
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
		var mode = SDL_GetDesktopDisplayMode(SDL_GetPrimaryDisplay());
		if (mode == null) {
			DisplayMode best = null;
			for (DisplayMode displayMode : getAvailableDisplayModes()) {
				if (best == null || displayMode.getWidth() * displayMode.getHeight() > best.getWidth() * best.getHeight()) {
					best = displayMode;
				}
			}
			return best;
		}
		return new DisplayMode(mode.w(), mode.h(), SDL_PixelFormatDetails.nbits_per_pixel(SDLPixels.nSDL_GetPixelFormatDetails(mode.format())), (int) mode.refresh_rate());

	}


	public static int setIcon(@NotNull ByteBuffer[] icons) {

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

		if (isCreated() && icons.length > 0) {
			try (MemoryStack memoryStack = stackPush()) {
				var first = icons[0];
				int size = (int) Math.sqrt(first.limit() / 4f);
				try (var surface = SDLSurface.SDL_CreateSurface(size, size, SDLPixels.SDL_PIXELFORMAT_RGBA32)) {
					surface.pixels(memoryStack.malloc(first.limit()).put(first).flip());
					for (int j = 1; j < icons.length; j++) {
						var buf = icons[j];
						int currentSize = (int) Math.sqrt(buf.limit() / 4f);
						SDLSurface.SDL_AddSurfaceAlternateImage(surface, SDLSurface.SDL_CreateSurface(currentSize, currentSize, SDLPixels.SDL_PIXELFORMAT_RGBA32)
								.pixels(memoryStack.malloc(buf.limit()).put(buf).flip()));
					}
					SDL_SetWindowIcon(handle, surface);
				}
			}
			return 1;
		} else {
			return 0;
		}
	}


	public static void update() {
		window_resized = false;
		while (SDL_PollEvent(event)) {
			switch (event.type()) {
				case SDL_EVENT_QUIT, SDL_EVENT_WINDOW_CLOSE_REQUESTED -> closeRequested = true;
				case SDL_EVENT_WINDOW_FOCUS_GAINED -> focused = true;
				case SDL_EVENT_WINDOW_FOCUS_LOST -> focused = false;
				case SDL_EVENT_WINDOW_SHOWN, SDL_EVENT_WINDOW_RESTORED, SDL_EVENT_WINDOW_MAXIMIZED -> minimized = false;
				case SDL_EVENT_WINDOW_HIDDEN, SDL_EVENT_WINDOW_MINIMIZED -> minimized = true;
				case SDL_EVENT_WINDOW_RESIZED -> resizeCallback(handle, windowEvent.data1(), windowEvent.data2());
				case SDL_EVENT_WINDOW_MOVED -> {
					x = windowEvent.data1();
					y = windowEvent.data2();
				}
				case SDL_EVENT_WINDOW_PIXEL_SIZE_CHANGED ->
						onFramebufferResize(handle, windowEvent.data1(), windowEvent.data2());
				case SDL_EVENT_WINDOW_ENTER_FULLSCREEN -> {
					isFullscreen = true;
					window_resized = true;
				}
				case SDL_EVENT_WINDOW_LEAVE_FULLSCREEN -> {
					isFullscreen = false;
					window_resized = true;
				}
				case SDL_EVENT_KEY_DOWN, SDL_EVENT_KEY_UP, SDL_EVENT_TEXT_INPUT, SDL_EVENT_TEXT_EDITING -> {
					if (Keyboard.isCreated()) {
						Keyboard.processKeyboardEvent(event);
					}
				}
				case SDL_EVENT_MOUSE_BUTTON_DOWN, SDL_EVENT_MOUSE_BUTTON_UP, SDL_EVENT_MOUSE_MOTION,
				     SDL_EVENT_MOUSE_WHEEL, SDL_EVENT_WINDOW_MOUSE_ENTER, SDL_EVENT_WINDOW_MOUSE_LEAVE -> {
					if (Mouse.isCreated()) {
						Mouse.processMouseEvent(event);
					}
				}
				case SDL_EVENT_DROP_BEGIN -> currentEventDrops.clear();
				case SDL_EVENT_DROP_FILE -> {
					var data = dropEvent.dataString();
					if (data != null) {
						currentEventDrops.add(Path.of(data));
					}
				}
				case SDL_EVENT_DROP_COMPLETE -> {
					if (!currentEventDrops.isEmpty()) {
						LegacyLWJGL3ScreenEx.handleFileDrop(currentEventDrops);
						currentEventDrops.clear();
					}
				}
			}
		}
		Keyboard.poll();
		Mouse.poll();

		checkSdlError(SDL_GL_SwapWindow(handle));
	}

	public static void checkSdlError(boolean success) {
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

	public static void create() {
		try {
			create(new PixelFormat());
		} catch (LWJGLException e) {
			throw new RuntimeException(e);
		}
	}

	public static void create(@NotNull PixelFormat pixelFormat) throws LWJGLException {
		windowedWidth = width = displayMode.getWidth();
		windowedHeight = height = displayMode.getHeight();
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
		handle = checkSdlError(SDL_CreateWindowWithProperties(props));
		SDL_DestroyProperties(props);

		glContext = checkSdlError(SDL_GL_CreateContext(handle));
		checkSdlError(SDL_GL_LoadLibrary((ByteBuffer) null));
		Configuration.OPENGL_EXPLICIT_INIT.set(true);
		GL.create(SDLVideo::SDL_GL_GetProcAddress);
		GL.createCapabilities(MemoryUtil::memCallocPointer);

		try (MemoryStack ms = stackPush()) {
			var xBox = ms.mallocInt(1);
			var yBox = ms.mallocInt(1);
			SDL_GetWindowPosition(handle, xBox, yBox);
			windowedX = x = xBox.get(0);
			windowedY = y = yBox.get(0);
			setFullscreen(false);

			IntBuffer width = ms.mallocInt(1);
			IntBuffer height = ms.mallocInt(1);
			checkSdlError(SDL_GetWindowSizeInPixels(handle, width, height));
			framebufferWidth = Math.max(1, width.get(0));
			framebufferHeight = Math.max(1, height.get(0));
		}

		Mouse.create();
		Keyboard.create();
		SDLKeyboard.SDL_StartTextInput(Display.getHandle());
		checkSdlError(SDL_ShowWindow(handle));
		if (cached_icons != null) {
			setIcon(cached_icons);
		}
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

			if (fullscreen) {
				int monitor = SDL_GetPrimaryDisplay();
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

				// This code would enable exclusive fullscreen. It doesn't work.
				// mode.internal() ends up being 0 which lwjgl checks for.
				//var mode = SDL_GetCurrentDisplayMode(monitor);
				//SDL_GetClosestFullscreenDisplayMode(monitor, width, height, -1, true, mode);
				/*if (mode != null) {
					if (mode.internal() == 0) {
						mode = null;
					}
					SDL_SetWindowFullscreenMode(handle, mode);
					if (mode != null) {
						width = mode.w();
						height = mode.h();
					}
				}*/

			} else {
				x = windowedX;
				y = windowedY;
				width = windowedWidth;
				height = windowedHeight;
			}
			SDL_SetWindowFullscreen(handle, fullscreen);
			window_resized = true;

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@NotNull
	public static DisplayMode[] getAvailableDisplayModes() {

		int currentMonitor = handle != 0 ? SDL_GetDisplayForWindow(handle) : 0;
		if (currentMonitor == 0) {
			currentMonitor = SDL_GetPrimaryDisplay();
		}
		if (currentMonitor == 0) {
			return new DisplayMode[0];
		} else {
			var buf = SDL_GetFullscreenDisplayModes(currentMonitor);
			if (buf == null) {
				throw new IllegalStateException("No video modes found");
			} else {
				int bound = buf.limit();
				DisplayMode[] modes = new DisplayMode[bound];
				for (int i = 0; i < bound; i++) {
					long l = buf.get(i);
					DisplayMode mode = new DisplayMode(SDL_DisplayMode.nw(l), SDL_DisplayMode.nh(l),
							SDL_PixelFormatDetails.nbits_per_pixel(
									SDLPixels.nSDL_GetPixelFormatDetails(SDL_DisplayMode.nformat(l))),
							(int) SDL_DisplayMode.nrefresh_rate(l));
					modes[i] = mode;
				}
				return modes;
			}
		}
	}

	public static void destroy() {
		// free callbacks
		SDLKeyboard.SDL_StopTextInput(getHandle());
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
		event.free();
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

	public static boolean isActive() {
		return focused;
	}

	public static void setResizable(boolean isResizable) {
		resizable = isResizable;
		if (isCreated()) {
			SDL_SetWindowResizable(handle, resizable);
		}
	}

	public static void sync(int fps) {
		Sync.sync(fps);
	}


	public static void setVSyncEnabled(boolean enabled) {
		if (glContext != 0) {
			SDL_GL_SetSwapInterval(enabled ? 1 : 0);
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

	public static void makeCurrent() {
		// No-Op
		SDLVideo.SDL_GL_MakeCurrent(getHandle(), handle);
	}

	public static Drawable getDrawable() {
		return Drawable.INSTANCE;
	}

	public static void swapBuffers() {
		checkSdlError(SDLVideo.SDL_GL_SwapWindow(handle));
	}
}
