package io.github.moehreag.legacylwjgl3.api;

import io.github.moehreag.legacylwjgl3.PreeditAwareWidgetInternal;
import org.jetbrains.annotations.ApiStatus;

/**
 * A widget (or screen) that handles text input and should support IME preedit overlay rendering.
 *
 * <p>Note: This system also handles enabling/disabling text input on SDL3.</p>
 */
public interface PreeditAwareWidget {
	/**
	 * Get the current window-relative X coordinate of the text cursor.
	 *
	 * @return the cursor's X coordinate.
	 */
	int getCursorX();

	/**
	 * Get the current window-relative Y coordinate of the text cursor.
	 * The IME overlay will be positioned below the input line.
	 *
	 * @return the cursor's Y coordinate
	 */
	int getCursorY();

	/**
	 * Get the height of this input or, if this input has multiple lines, the line height.
	 * This value is used in order to position the IME overlay below the input line.
	 *
	 * @return the input height
	 */
	int getInputHeight();

	/**
	 * Notify the IME manager of a focus change to this widget.
	 *
	 * <p>This method <strong>MUST</strong> be called for IME to function correctly.</p>
	 * <b>DO NOT OVERRIDE THIS METHOD</b>
	 *
	 * @param focused whether this widget is focused
	 */
	@ApiStatus.NonExtendable
	default void onFocusUpdate(boolean focused) {
		PreeditAwareWidgetInternal.updateWidgetFocus(this, focused);
	}
}
