package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.PreeditAwareWidgetInternal;
import io.github.moehreag.legacylwjgl3.api.PreeditAwareWidget;
import io.github.moehreag.legacylwjgl3.util.IMEManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PreeditAwareWidgetInternal.class)
public abstract class PreeditAwareWidgetInternalMixin {
	/**
	 * @author moehreag
	 * @reason implement IME API
	 */
	@Overwrite
	public static void updateWidgetFocus(PreeditAwareWidget widget, boolean focused) {
		IMEManager.getInstance().onWidgetFocusUpdate(widget, focused);
	}
}
