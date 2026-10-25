package net.coderbot.iris.mixin.argentum;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// the query boxes have no texture or color, so pack programs can discard them and hide every entity behind nothing
@Mixin(targets = "dev.rdh.argentum.impl.render.entity.EntityOcclusionCuller", remap = false)
public class MixinEntityOcclusionCuller {
	@Inject(method = "beginQueryPass", at = @At("HEAD"))
	private static void lucerna$useFixedFunction(CallbackInfo ci) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null) {
			pipeline.getRenderTargetStateListener().beginPostChain();
		}
	}

	@Inject(method = "endQueryPass", at = @At("TAIL"))
	private static void lucerna$restorePipeline(CallbackInfo ci) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null) {
			pipeline.getRenderTargetStateListener().endPostChain();
		}
	}
}
