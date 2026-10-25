package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Disables the vanilla blob shadows when the shader pack renders shadows itself. This runs before Argentum batches
 * the blob shadows.
 */
@Mixin(value = EntityRenderer.class, priority = 900)
public class MixinEntityRenderer {
	@Inject(method = "renderShadow", at = @At("HEAD"), cancellable = true)
	private void iris$disableVanillaShadows(Entity entity, double dx, double dy, double dz, float yaw, float tickDelta, CallbackInfo ci) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null && pipeline.shouldDisableVanillaEntityShadows()) {
			ci.cancel();
		}
	}
}
