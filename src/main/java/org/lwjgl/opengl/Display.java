package org.lwjgl.opengl;

import java.nio.ByteBuffer;

import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.LWJGLException;

@SuppressWarnings("unused")
public class Display {
	static final Display.Impl impl = LegacyLWJGL3.USE_SDL ? new SDLDisplay() : new GLFWDisplay();

	public static long getHandle() {
		return impl.getHandle();
	}

	@NotNull
	public static String getTitle() {
		return impl.getTitle();
	}

	public static void setTitle(@NotNull String title) {
		impl.setTitle(title);
	}

	public static void setHandle(long handle) {
		impl.setHandle(handle);
	}

	@NotNull
	public static DisplayMode getDisplayMode() {
		return impl.getDisplayMode();
	}

	public static void setDisplayMode(@NotNull DisplayMode mode) {
		impl.setDisplayMode(mode);
	}

	public static int getWidth() {
		return impl.getWidth();
	}

	public static void setWidth(int width) {
		impl.setWidth(width);
	}

	public static int getHeight() {
		return impl.getHeight();
	}

	public static void setHeight(int height) {
		impl.setHeight(height);
	}

	public static void setScreenWidth(int width) {
		impl.setScreenWidth(width);
	}

	public static int getScreenWidth() {
		return impl.getScreenWidth();
	}

	public static void setScreenHeight(int height) {
		impl.setScreenHeight(height);
	}

	public static int getScreenHeight() {
		return impl.getScreenHeight();
	}

	@Nullable
	public static DisplayMode getDesktopDisplayMode() {
		return impl.getDesktopDisplayMode();
	}


	public static int setIcon(@NotNull ByteBuffer[] icons) {
		return impl.setIcon(icons);
	}


	public static void update() {
		impl.update();
	}

	public static void create() throws LWJGLException {
		create(new PixelFormat());
	}

	public static void create(@NotNull PixelFormat pixelFormat) throws LWJGLException {
		impl.create(pixelFormat);
	}

	public static void setFullscreen(boolean fullscreen) {
		impl.setFullscreen(fullscreen);
	}

	@NotNull
	public static DisplayMode[] getAvailableDisplayModes() {
		return impl.getAvailableDisplayModes();
	}

	public static void destroy() {
		impl.destroy();
	}

	public static boolean isCreated() {
		return getHandle() != -1L;
	}

	public static boolean isActive() {
		return impl.isActive();
	}

	public static void setResizable(boolean isResizable) {
		impl.setResizable(isResizable);
	}

	public static void sync(int fps) {
		Sync.sync(fps);
	}


	public static void setVSyncEnabled(boolean enabled) {
		impl.setVSyncEnabled(enabled);
	}

	public static boolean wasResized() {
		return impl.wasResized();
	}

	public static boolean isVisible() {
		return impl.isVisible();
	}

	public static void makeCurrent() {
		impl.makeCurrent();
	}

	public static Drawable getDrawable() {
		return impl.getDrawable();
	}

	public static boolean isCloseRequested() {
		return impl.isCloseRequested();
	}

	public static void swapBuffers() {
		impl.swapBuffers();
	}

	sealed interface Impl permits SDLDisplay, GLFWDisplay {
		long getHandle();

		@NotNull
		String getTitle();

		void setTitle(@NotNull String title);

		void setHandle(long handle);

		@NotNull
		DisplayMode getDisplayMode();

		void setDisplayMode(@NotNull DisplayMode mode);

		int getWidth();

		void setWidth(int width);

		int getHeight();

		void setHeight(int height);

		void setScreenWidth(int width);

		int getScreenWidth();

		void setScreenHeight(int height);

		int getScreenHeight();

		@Nullable
		DisplayMode getDesktopDisplayMode();


		int setIcon(@NotNull ByteBuffer[] icons);


		void update();

		void create(@NotNull PixelFormat pixelFormat) throws LWJGLException;

		void setFullscreen(boolean fullscreen);

		@NotNull
		DisplayMode[] getAvailableDisplayModes();

		void destroy();

		boolean isActive();

		void setResizable(boolean isResizable);

		void setVSyncEnabled(boolean enabled);

		boolean wasResized();

		boolean isVisible();

		void makeCurrent();

		Drawable getDrawable();

		void swapBuffers();

		boolean isCloseRequested();
	}
}
