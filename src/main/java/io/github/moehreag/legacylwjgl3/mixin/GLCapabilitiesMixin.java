package io.github.moehreag.legacylwjgl3.mixin;

import java.util.Set;
import java.util.function.IntFunction;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.FunctionProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GLCapabilities.class)
public class GLCapabilitiesMixin {
    @Mutable
    @Unique
    @Final
    public boolean GL_EXT_multi_draw_arrays;
    @Mutable
    @Unique
    @Final
    public boolean GL_EXT_paletted_texture;
    @Mutable
    @Unique
    @Final
    public boolean GL_EXT_rescale_normal;
    @Mutable
    @Unique
    @Final
    public boolean GL_EXT_texture_3d;
    @Mutable
    @Unique
    @Final
    public boolean GL_EXT_texture_lod_bias;
    @Mutable
    @Unique
    @Final
    public boolean GL_EXT_vertex_shader;
    @Mutable
    @Unique
    @Final
    public boolean GL_EXT_vertex_weighting;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initExtendedFields(FunctionProvider provider, Set<String> ext, boolean fc,
                                    IntFunction<PointerBuffer> bufferFactory, CallbackInfo ci) {
        GL_EXT_multi_draw_arrays = ext.contains("GL_EXT_multi_draw_arrays");
        GL_EXT_paletted_texture = ext.contains("GL_EXT_paletted_texture");
        GL_EXT_rescale_normal = ext.contains("GL_EXT_rescale_normal");
        GL_EXT_texture_3d = ext.contains("GL_EXT_texture_3d");
        GL_EXT_texture_lod_bias = ext.contains("GL_EXT_texture_lod_bias");
        GL_EXT_vertex_shader = ext.contains("GL_EXT_vertex_shader");
        GL_EXT_vertex_weighting = ext.contains("GL_EXT_vertex_weighting");
    }
}
