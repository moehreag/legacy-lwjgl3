package io.github.moehreag.legacylwjgl3;

import io.github.moehreag.legacylwjgl3.api.PreeditAwareWidget;
import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("unused")
@ApiStatus.Internal
public final class PreeditAwareWidgetInternal {
	public static void updateWidgetFocus(PreeditAwareWidget widget, boolean focused) {
		// Does nothing if legacy-lwjgl3 isn't installed but this API package is present
		// This method is implemented using a mixin.
	}
}
