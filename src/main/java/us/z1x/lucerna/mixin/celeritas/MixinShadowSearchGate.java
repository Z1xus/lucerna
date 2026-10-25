package us.z1x.lucerna.mixin.celeritas;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import us.z1x.lucerna.pipeline.ShadowRenderer;
import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.embeddedt.embeddium.impl.render.chunk.lists.RenderListManager;
import org.embeddedt.embeddium.impl.render.viewport.Viewport;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// the shadow search runs every frame, but while the cached shadow terrain is reused and no section changed, last
// frame's result is still the right one
@Mixin(value = RenderSectionManager.class, remap = false)
public class MixinShadowSearchGate {
	@WrapOperation(method = "updateForShadowPass", at = @At(value = "INVOKE", target = "Lorg/embeddedt/embeddium/impl/render/chunk/lists/RenderListManager;startShadowGraphUpdate(Lorg/embeddedt/embeddium/impl/render/viewport/Viewport;IIFLorg/joml/Vector3fc;I)V"))
	private void lucerna$skipUnchangedShadowSearch(RenderListManager manager, Viewport viewport, int frame, int regionIdsLength, float searchDistance,
												   Vector3fc lightVector, int targetQueueSize, Operation<Void> original) {
		if (!ShadowRenderer.reusingTerrain || manager.isNeedsUpdate()) {
			original.call(manager, viewport, frame, regionIdsLength, searchDistance, lightVector, targetQueueSize);
		}
	}
}
