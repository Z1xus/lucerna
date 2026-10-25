package net.coderbot.iris.mixin;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.minecraft.client.render.platform.GLX;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GLX.class)
public class MixinGLX {
	@Inject(method = "bindFramebuffer", at = @At("RETURN"))
	private static void lucerna$onBindFramebuffer(int target, int framebuffer, CallbackInfo ci) {
		IrisRenderSystem.onFramebufferBound(target, framebuffer);
	}
}
