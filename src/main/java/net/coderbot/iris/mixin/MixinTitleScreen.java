package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.minecraft.client.gui.screen.menu.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {
	@Unique
	private static boolean iris$hasFirstInit;

	@Inject(method = "init", at = @At("RETURN"))
	private void iris$firstInit(CallbackInfo ci) {
		if (!iris$hasFirstInit) {
			iris$hasFirstInit = true;
			Iris.onLoadingComplete();
		}
	}
}
