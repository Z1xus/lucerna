package net.coderbot.iris.mixin.celeritas;

import org.embeddedt.embeddium.impl.render.terrain.SimpleWorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = SimpleWorldRenderer.class, remap = false)
public class MixinSimpleWorldRenderer {
	// celeritas tracks visits and builds by frame number, so both passes share one counter that never resets
	@Unique
	private static int lucerna$frame;

	@ModifyVariable(method = {"setupTerrain", "setupShadowTerrain"}, at = @At("HEAD"), argsOnly = true)
	private int lucerna$shareFrameCounter(int frame) {
		return lucerna$frame++;
	}
}
