package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.api.PreeditAwareWidget;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.menu.SignEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(SignEditScreen.class)
public abstract class IMESignEditScreenMixin extends Screen implements PreeditAwareWidget {

	@Shadow
	private SignBlockEntity sign;
	@Shadow
	private int row;

	@Inject(method = "init", at = @At("TAIL"))
	private void onInit(CallbackInfo ci) {
		onFocusUpdate(true);
	}

	@Override
	public int getCursorX() {
		var currentRowText = this.sign.lines[this.row].getString();
		return width/2 + textRenderer.getWidth(currentRowText) / 2;
	}

	@Override
	public int getCursorY() {
		return 40 + 20 + 12 + this.row * 10;
	}

	@Override
	public int getInputHeight() {
		return 9;
	}
}
