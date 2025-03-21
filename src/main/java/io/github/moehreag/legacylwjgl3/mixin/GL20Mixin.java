package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.annotations.CreateStub;
import io.github.moehreag.legacylwjgl3.annotations.Public;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GL20.class)
public abstract class GL20Mixin {
    @CreateStub("glUniformMatrix4")
    @Shadow
    public static void glUniformMatrix4fv(int location, boolean transpose, FloatBuffer value) {

    }

    @CreateStub("glUniform2")
    @Shadow
    public static void glUniform2fv(int location, FloatBuffer value) {

    }

    @CreateStub("glUniform1")
    @Shadow
    public static void glUniform1iv(int location, IntBuffer value) {

    }

    @CreateStub("glUniform1")
    @Shadow
    public static void glUniform1fv(int location, FloatBuffer value) {

    }

    @CreateStub("glUniform2i")
    @Shadow
    public static void glUniform2iv(int location, IntBuffer value) {

    }

    @CreateStub("glUniform3")
    @Shadow
    public static void glUniform3iv(int location, IntBuffer value) {

    }

    @CreateStub("glUniform3")
    @Shadow
    public static void glUniform3fv(int location, FloatBuffer value) {

    }

    @CreateStub("glUniform4")
    @Shadow
    public static void glUniform4iv(int location, IntBuffer value) {

    }

    @CreateStub("glUniform4")
    @Shadow
    public static void glUniform4fv(int location, FloatBuffer value) {

    }

    @CreateStub("glUniformMatrix2")
    @Shadow
    public static void glUniformMatrix2fv(int location, boolean transpose, FloatBuffer value) {

    }

    @CreateStub("glUniformMatrix3")
    @Shadow
    public static void glUniformMatrix3fv(int location, boolean transpose, FloatBuffer value) {

    }

    @Shadow
    public static void glShaderSource(int shader, CharSequence... strings) {
    }

    @Public
    @Unique
    private static void glShaderSource(int shader, java.nio.ByteBuffer string) {
        byte[] data = new byte[string.limit()];
        string.position(0);
        string.get(data);
        string.position(0);
        glShaderSource(shader, new String(data));
    }
}
