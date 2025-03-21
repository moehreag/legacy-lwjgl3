package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.annotations.CreateStub;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.openal.AL10;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AL10.class)
public abstract class AL10Mixin {
    @CreateStub("alListener")
    @Shadow
    public static void alListenerfv(int paramName, FloatBuffer values) {
    }

    @CreateStub("alSource")
    @Shadow
    public static void alSourcefv(int source, int param, FloatBuffer values) {
    }

    @CreateStub("alSourceStop")
    @Shadow
    public static void alSourceStopv(IntBuffer sources) {
    }
}
