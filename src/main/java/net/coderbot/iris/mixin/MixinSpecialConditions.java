package net.coderbot.iris.mixin;

import net.coderbot.iris.gbuffer_overrides.matching.SpecialCondition;
import net.coderbot.iris.layer.GbufferPrograms;
import net.minecraft.client.render.block.entity.BeaconRenderer;
import net.minecraft.client.render.entity.layer.EnderDragonEyesLayer;
import net.minecraft.client.render.entity.layer.EndermanEyesLayer;
import net.minecraft.client.render.entity.layer.SpiderEyesLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Selects the beacon beam and glowing eyes programs.
 */
@Mixin({ BeaconRenderer.class, SpiderEyesLayer.class, EndermanEyesLayer.class, EnderDragonEyesLayer.class })
public class MixinSpecialConditions {
	@Inject(method = "render", at = @At("HEAD"))
	private void iris$beginSpecialCondition(CallbackInfo ci) {
		GbufferPrograms.setupSpecialRenderCondition((Object) this instanceof BeaconRenderer ? SpecialCondition.BEACON_BEAM : SpecialCondition.ENTITY_EYES);
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void iris$endSpecialCondition(CallbackInfo ci) {
		GbufferPrograms.teardownSpecialRenderCondition(null);
	}
}
