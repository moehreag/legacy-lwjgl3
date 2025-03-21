package io.github.moehreag.legacylwjgl3.annotations;

/**
 * Creates a stub method in the class that invokes the annotated method. Use with
 * {@link org.spongepowered.asm.mixin.Shadow}, since shadow copies annotations to the target.
 */
public @interface CreateStub {
    String value();
}
