package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.annotations.CreateStub;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

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
}
