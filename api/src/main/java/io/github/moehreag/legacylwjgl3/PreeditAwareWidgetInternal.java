package io.github.moehreag.legacylwjgl3;

import io.github.moehreag.legacylwjgl3.api.PreeditAwareWidget;
import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("unused")
@ApiStatus.Internal
public final class PreeditAwareWidgetInternal {
	public static void updateWidgetFocus(PreeditAwareWidget widget, boolean focused) {
		throw new UnsupportedOperationException("Implemented in Mixin");
	}
}
