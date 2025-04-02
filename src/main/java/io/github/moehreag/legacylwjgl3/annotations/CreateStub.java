package io.github.moehreag.legacylwjgl3.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Creates a stub method in the class that invokes the annotated method. Use with
 * {@link org.spongepowered.asm.mixin.Shadow}, since shadow copies annotations to the target.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface CreateStub {
    String value();
}
