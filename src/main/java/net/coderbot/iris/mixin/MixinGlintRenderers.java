package net.coderbot.iris.mixin;

import net.coderbot.iris.gbuffer_overrides.matching.SpecialCondition;
import net.coderbot.iris.layer.GbufferPrograms;
import net.minecraft.client.render.entity.ItemRenderer;
import net.minecraft.client.render.entity.layer.AbstractArmorLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ ItemRenderer.class, AbstractArmorLayer.class })
public class MixinGlintRenderers {
	@Inject(method = "renderEnchantmentGlint", at = @At("HEAD"))
	private void lucerna$beginGlint(CallbackInfo ci) {
		GbufferPrograms.setupSpecialRenderCondition(SpecialCondition.GLINT);
	}

	@Inject(method = "renderEnchantmentGlint", at = @At("RETURN"))
	private void lucerna$endGlint(CallbackInfo ci) {
		GbufferPrograms.teardownSpecialRenderCondition(SpecialCondition.GLINT);
	}
}
