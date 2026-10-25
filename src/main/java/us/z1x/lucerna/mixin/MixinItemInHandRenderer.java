package us.z1x.lucerna.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import us.z1x.lucerna.Iris;
import us.z1x.lucerna.pipeline.WorldRenderingPipeline;
import net.minecraft.client.render.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandRenderer {
	@WrapWithCondition(method = "renderScreenEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/ItemInHandRenderer;renderInWaterEffect(F)V"))
	private boolean lucerna$renderUnderwaterOverlay(ItemInHandRenderer renderer, float tickDelta) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		return pipeline == null || pipeline.shouldRenderUnderwaterOverlay();
	}
}
