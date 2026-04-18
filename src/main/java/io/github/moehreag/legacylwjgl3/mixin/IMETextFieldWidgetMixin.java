package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.util.IMEManager;
import io.github.moehreag.legacylwjgl3.util.IMEPreeditOverlay;
import io.github.moehreag.legacylwjgl3.util.PreeditEvent;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.TextRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextFieldWidget.class)
public abstract class IMETextFieldWidgetMixin implements IMEManager.PreeditListener {

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
	@Unique
	private IMEPreeditOverlay overlay;

	@Inject(method = "setFocused", at = @At("HEAD"))
	private void startTextInput(boolean focused, CallbackInfo ci) {
		if (focused != this.focused && editable) {
			IMEManager.getInstance().onTextInputFocusChange(this, focused);
		}
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void updateRect(CallbackInfo ci) {
		if (overlay != null) {
			overlay.updateInputPosition((this.hasBorder ? this.x + 4 : this.x) + textRenderer.getWidth(text.substring(firstCharacterIndex, selectionStart)),
					this.hasBorder ? this.y + (this.height - 8) / 2 : this.y);
		}
	}

	@Override
	public void legacy_lwjgl3$onPreeditChange(PreeditEvent event) {
		overlay = event != null ? new IMEPreeditOverlay(event, textRenderer, textRenderer.fontHeight + 1) : null;
	}

	@Override
	public IMEPreeditOverlay legacy_lwjgl3$getOverlay() {
		return overlay;
	}
}
