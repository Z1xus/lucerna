package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Initializes Iris right before options are loaded, so that its key bindings are loaded from options.txt too, and
 * applies the cloud mode of the shader pack.
 */
@Mixin(GameOptions.class)
public class MixinGameOptions {
	@Shadow
	public KeyBinding[] keyBindings;

	@Inject(method = "<init>(Lnet/minecraft/client/Minecraft;Ljava/io/File;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/options/GameOptions;load()V"))
	private void lucerna$beforeLoadOptions(CallbackInfo ci) {
		keyBindings = ArrayUtils.addAll(keyBindings, Iris.onEarlyInitialize());
	}

	@Inject(method = "getCloudRenderMode", at = @At("RETURN"), cancellable = true)
	private void lucerna$overrideCloudMode(CallbackInfoReturnable<Integer> cir) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline == null) {
			return;
		}

		switch (pipeline.getCloudSetting()) {
			case OFF:
				cir.setReturnValue(0);
				break;
			case FAST:
				cir.setReturnValue(1);
				break;
			case FANCY:
				cir.setReturnValue(2);
				break;
			default:
				break;
		}
	}
}
