package org.lwjgl.opengl;

import org.lwjgl.LWJGLException;

@SuppressWarnings("unused")
public class SharedDrawable implements Drawable {

	private final Drawable drawable;
	public SharedDrawable(final Drawable drawable) {
		this.drawable = drawable;
	}

	@Override
	public void makeCurrent() throws LWJGLException {
		drawable.makeCurrent();
	}

	@Override
	public void releaseContext() throws LWJGLException {
		drawable.releaseContext();
	}

	@Override
	public void destroy() {
		drawable.destroy();
	}
}