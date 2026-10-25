package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
	@Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GLX;init()V", shift = At.Shift.AFTER))
	private void iris$onRenderSystemInit(CallbackInfo ci) {
		Iris.onRenderSystemInit();
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void iris$handleKeybinds(CallbackInfo ci) {
		Iris.handleKeybinds((Minecraft) (Object) this);
	}
}
