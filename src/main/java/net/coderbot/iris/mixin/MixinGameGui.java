package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.gui.GameGui;
import net.minecraft.client.render.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameGui.class)
public class MixinGameGui {
	@Inject(method = "renderVignette", at = @At("HEAD"), cancellable = true)
	private void lucerna$disableVignette(float brightnessAtEyes, Window window, CallbackInfo ci) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null && !pipeline.shouldRenderVignette()) {
			ci.cancel();
		}
	}
}
