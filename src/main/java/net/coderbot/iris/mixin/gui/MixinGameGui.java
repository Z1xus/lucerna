package net.coderbot.iris.mixin.gui;

import net.coderbot.iris.gui.screen.HudHideable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GameGui;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameGui.class)
public class MixinGameGui {
	@Shadow
	@Final
	private Minecraft minecraft;

	// cancel after the gui projection is set up, since screens rely on it when in a world
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;setupGuiState()V", shift = At.Shift.AFTER), cancellable = true)
	private void iris$handleHudHidingScreens(float tickDelta, CallbackInfo ci) {
		if (this.minecraft.screen instanceof HudHideable) {
			ci.cancel();
		}
	}
}
