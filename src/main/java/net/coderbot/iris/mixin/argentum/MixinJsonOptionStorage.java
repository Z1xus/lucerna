package net.coderbot.iris.mixin.argentum;

import dev.rdh.argentum.api.config.JsonOptionStorage;
import net.coderbot.iris.compat.argentum.ArgentumCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps the overrides of {@link ArgentumCompat} out of Argentum's config file.
 */
@Mixin(value = JsonOptionStorage.class, remap = false)
public class MixinJsonOptionStorage {
	@Inject(method = "save", at = @At("HEAD"))
	private void iris$beforeSave(CallbackInfo ci) {
		ArgentumCompat.beforeSave();
	}

	@Inject(method = "save", at = @At("RETURN"))
	private void iris$afterSave(CallbackInfo ci) {
		ArgentumCompat.afterSave();
	}
}
