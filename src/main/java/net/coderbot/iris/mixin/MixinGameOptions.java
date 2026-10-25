package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Initializes Iris right before options are loaded, so that its key bindings are loaded from options.txt too.
 */
@Mixin(GameOptions.class)
public class MixinGameOptions {
	@Shadow
	public KeyBinding[] keyBindings;

	@Inject(method = "<init>(Lnet/minecraft/client/Minecraft;Ljava/io/File;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/options/GameOptions;load()V"))
	private void iris$beforeLoadOptions(CallbackInfo ci) {
		keyBindings = ArrayUtils.addAll(keyBindings, Iris.onEarlyInitialize());
	}
}
