package io.github.moehreag.legacylwjgl3.mixin;

import java.util.List;

import io.github.moehreag.legacylwjgl3.api.PreeditAwareWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.BookEditScreen;
import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(BookEditScreen.class)
public abstract class IMEBookEditScreenMixin extends Screen implements PreeditAwareWidget {

	@Shadow
	@Final
	private boolean unsigned;
	@Shadow
	private NbtList pagesNbt;

	@Shadow
	protected abstract String getCurrentPageContent();

	@Shadow
	private int widthOffset;

	@Shadow
	private boolean signing;

	@Shadow
	private String title;

	@Inject(method = "init", at = @At("TAIL"))
	private void onInit(CallbackInfo ci) {
		if (this.unsigned) {
			onFocusUpdate(true);
		}
	}

	@Override
	public int getCursorX() {
		if (signing) {
			return (this.width - this.widthOffset) / 2 + 36 + (116 + textRenderer.getWidth(title)) / 2;
		}
		if (this.pagesNbt == null) return 0;
		var currentPageText = getCurrentPageContent() + "_";

		List<String> strings = textRenderer.split(currentPageText, 116);
		String last = strings.get(strings.size() - 1);
		int w = textRenderer.getWidth(last) - textRenderer.getWidth('_');

		return (this.width - this.widthOffset) / 2 + 36 + w;
	}

	@Override
	public int getCursorY() {
		if (signing) {
			return 2 + 48;
		}
		if (this.pagesNbt == null) return 0;
		var currentPageText = getCurrentPageContent() + "_";
		return 2 + 16 + 10 + textRenderer.splitAndGetHeight(currentPageText, 116);
	}

	@Override
	public int getInputHeight() {
		return textRenderer.fontHeight;
	}
}
