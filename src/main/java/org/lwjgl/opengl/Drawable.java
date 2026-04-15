package org.lwjgl.opengl;

import org.lwjgl.sdl.SDLVideo;

public interface Drawable {
	Drawable INSTANCE = new Drawable.NoOp();

	void makeCurrent();

	void releaseContext();

	void destroy();

	class NoOp implements Drawable {
		private long prevHandle = -1L;
		@Override
		public void makeCurrent() {
			if (Display.isCreated()) {
				prevHandle = Display.getHandle();
				Display.create();
			}
			Display.makeCurrent();
		}

		@Override
		public void releaseContext() {
			SDLVideo.SDL_GL_MakeCurrent(Display.getHandle(), 0);
		}

		@Override
		public void destroy() {
			SDLVideo.SDL_DestroyWindow(Display.getHandle());
			if (prevHandle != -1L) {
				Display.setHandle(prevHandle);
			}
		}
	}
}