package net.coderbot.iris.mixin;

import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.render.model.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPart.class)
public class MixinModelPart {
	@Inject(method = "compile", at = @At("HEAD"))
	private void iris$beginCompile(float scale, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCompilingDisplayList(true);
	}

	@Inject(method = "compile", at = @At("RETURN"))
	private void iris$endCompile(float scale, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCompilingDisplayList(false);
	}
}
