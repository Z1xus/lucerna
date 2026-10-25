package net.coderbot.iris.mixin;

import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.living.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.FloatBuffer;

/**
 * Exposes the hurt and creeper flash color as the entityColor uniform, which 1.8.9 otherwise applies with texture
 * combiners that shader programs cannot see.
 */
@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer {
	@Shadow
	protected FloatBuffer tintBuffer;

	@Inject(method = "setupOverlayColor(Lnet/minecraft/entity/living/LivingEntity;FZ)Z", at = @At("RETURN"))
	private void iris$captureOverlayColor(LivingEntity entity, float tickDelta, boolean alwaysRender, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ()) {
			CapturedRenderingState.INSTANCE.setCurrentEntityColor(tintBuffer.get(0), tintBuffer.get(1), tintBuffer.get(2), tintBuffer.get(3));
		}
	}

	@Inject(method = "tearDownOverlayColor", at = @At("RETURN"))
	private void iris$clearOverlayColor(CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentEntityColor(0.0F, 0.0F, 0.0F, 0.0F);
	}
}
