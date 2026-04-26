package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.api.PreeditAwareWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.TextRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(TextFieldWidget.class)
public abstract class IMETextFieldWidgetMixin implements PreeditAwareWidget {

	@Shadow
	@Final
	private TextRenderer textRenderer;
	@Shadow
	private boolean focused;
	@Shadow
	private String text;
	@Shadow
	private int firstCharacterIndex;
	@Shadow
	private int selectionStart;

	@Shadow
	private boolean editable;
	@Shadow
	private boolean hasBorder;
	@Shadow
	public int y;
	@Shadow
	@Final
	private int height;
	@Shadow
	public int x;

	@Inject(method = "setFocused", at = @At("HEAD"))
	private void startTextInput(boolean focused, CallbackInfo ci) {
		if (focused != this.focused && this.editable) {
			onFocusUpdate(focused);
		}
	}

	@Override
	public int getCursorX() {
		return (this.hasBorder ? this.x + 4 : this.x) + textRenderer.getWidth(text.substring(firstCharacterIndex, selectionStart));
	}

	@Override
	public int getCursorY() {
		return this.hasBorder ? this.y + (this.height - 8) / 2 : this.y;
	}

	@Override
	public int getInputHeight() {
		return textRenderer.fontHeight;
	}
}
