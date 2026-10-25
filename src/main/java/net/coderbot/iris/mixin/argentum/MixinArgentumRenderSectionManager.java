package net.coderbot.iris.mixin.argentum;

import dev.rdh.argentum.impl.render.terrain.ArgentumRenderSectionManager;
import net.coderbot.iris.Iris;
import net.coderbot.iris.shadows.ShadowRenderingState;
import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Gives Argentum's section manager the Celeritas shadow pass when a shader pack is loaded.
 */
@Mixin(value = ArgentumRenderSectionManager.class, remap = false)
public abstract class MixinArgentumRenderSectionManager extends RenderSectionManager {
	private MixinArgentumRenderSectionManager() {
		super(null, null, null, 0, null, 0, 0, 0);
	}

	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/embeddedt/embeddium/impl/render/chunk/RenderSectionManager;<init>(Lorg/embeddedt/embeddium/impl/render/chunk/RenderPassConfiguration;Ljava/util/function/Supplier;Ljava/util/function/BiFunction;ILorg/embeddedt/embeddium/impl/gl/device/CommandList;IIIZ)V"), index = 8)
	private static boolean iris$enableShadowPass(boolean hasShadowPass) {
		return hasShadowPass || Iris.getCurrentPack().isPresent();
	}

	@Override
	public boolean isInShadowPass() {
		return ShadowRenderingState.areShadowsCurrentlyBeingRendered();
	}
}
