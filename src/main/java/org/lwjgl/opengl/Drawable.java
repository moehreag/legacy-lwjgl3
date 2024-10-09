package org.lwjgl.opengl;

import org.lwjgl.glfw.GLFW;

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
