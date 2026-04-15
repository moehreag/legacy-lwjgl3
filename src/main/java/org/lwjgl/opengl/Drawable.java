package org.lwjgl.opengl;

import org.lwjgl.LWJGLException;

@SuppressWarnings("unused")
public interface Drawable {

	void makeCurrent() throws LWJGLException;

	void releaseContext() throws LWJGLException;

	void destroy();
}