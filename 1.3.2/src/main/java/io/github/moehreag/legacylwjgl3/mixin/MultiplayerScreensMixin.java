package io.github.moehreag.legacylwjgl3.mixin;

import net.minecraft.client.gui.screen.DirectConnectScreen;
import net.minecraft.client.gui.screen.menu.AddServerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings({"unused", "UnusedMixin"})
@Mixin({AddServerScreen.class, DirectConnectScreen.class})
public abstract class MultiplayerScreensMixin {

	@Redirect(method = "init", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;focused:Z", opcode = Opcodes.PUTFIELD))
	private void useSetterInsteadOfFieldAccess(TextFieldWidget instance, boolean value) {
		instance.setFocused(value);
	}
}
