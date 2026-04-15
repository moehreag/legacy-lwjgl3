package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.annotations.CreateStub;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import io.github.moehreag.legacylwjgl3.annotations.Public;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings("unused")
@Mixin(value = GL11.class, remap = false)
public abstract class GL11Mixin {
    @CreateStub("glGetFloat")
    @Shadow
    public static void glGetFloatv(int pname, FloatBuffer params) {
    }

    @CreateStub("glGetInteger")
    @Shadow
    public static void glGetIntegerv(int pname, IntBuffer params) {
    }

    @CreateStub("glFog")
    @Shadow
    public static void glFogfv(int pname, FloatBuffer params) {
    }

    @CreateStub("glLight")
    @Shadow
    public static void glLightfv(int light, int pname, FloatBuffer params) {
    }

    @CreateStub("glLightModel")
    @Shadow
    public static void glLightModelfv(int pname, FloatBuffer params) {
    }

    @CreateStub("glMultMatrix")
    @Shadow
    public static void glMultMatrixf(FloatBuffer m) {
    }

    @CreateStub("glTexEnv")
    @Shadow
    public static void glTexEnvfv(int target, int pname, FloatBuffer params) {
    }

    @CreateStub("glTexGen")
    @Shadow
    public static void glTexGenfv(int coord, int pname, FloatBuffer params) {
    }

	@Shadow
	public static void nglDrawElements(int mode, int count, int type, long indices) {
	}

	@Unique
	@Public
	private static void glDrawElements(int mode, int count, int type, ByteBuffer indices) {
		nglDrawElements(mode, count, type, MemoryUtil.memAddress(indices));
	}

	@Shadow
	public static void glTexCoordPointer(int size, int type, int stride, FloatBuffer pointer) {
	}

	@Shadow
	public static void glTexCoordPointer(int size, int type, int stride, ShortBuffer pointer) {
	}

	@Shadow
	public static void glColorPointer(int size, int type, int stride, ByteBuffer pointer) {
	}

	@Shadow
	public static void glVertexPointer(int size, int type, int stride, FloatBuffer pointer) {
	}

	@Shadow
	public static void glNormalPointer(int type, int stride, ByteBuffer pointer) {
	}

	@CreateStub("glLoadMatrix")
	@Shadow
	public static void glLoadMatrixf(FloatBuffer m) {
	}

	@SuppressWarnings({"unused", "MissingUnique"})
	@Public
	private static void glTexCoordPointer(int i, int stride, FloatBuffer pointer) {
		glTexCoordPointer(i, 0x1406, stride, pointer);
	}

	@SuppressWarnings({"unused", "MissingUnique"})
	@Public
	private static void glTexCoordPointer(int i, int stride, ShortBuffer pointer) {
		glTexCoordPointer(i, 0x1402, stride, pointer);
	}

	@SuppressWarnings({"unused", "MissingUnique"})
	@Public
	private static void glColorPointer(int i, boolean bl, int i2, ByteBuffer pointer) {
		glColorPointer(i, 0x1401, i2, pointer);
	}

	@SuppressWarnings({"unused", "MissingUnique"})
	@Public
	private static void glVertexPointer(int i, int i2, FloatBuffer pointer) {
		glVertexPointer(i, 0x1406, i2, pointer);
	}
	@SuppressWarnings({"unused", "MissingUnique"})
	@Public
	private static void glNormalPointer(int stride, ByteBuffer pointer) {
		glNormalPointer(0x1400, stride, pointer);
	}
}
