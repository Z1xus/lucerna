package net.coderbot.iris.mixin.celeritas;

import com.llamalad7.mixinextras.sugar.Local;
import net.coderbot.iris.pipeline.ShadowRenderer;
import org.embeddedt.embeddium.impl.render.chunk.DefaultChunkRenderer;
import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

// each region caches two batches per render pass storage, keyed by the face culling flag, and the shadow pass shared those
// keys with the main pass, so both rebuilt every frame. the shadow pass uses the pass's sorting as its key instead: storage
// is per render pass, so within one storage this never matches the main pass key
@Mixin(value = DefaultChunkRenderer.class, remap = false)
public class MixinShadowBatchCache {
	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lorg/embeddedt/embeddium/impl/render/chunk/data/SectionRenderDataStorage$BatchCacheParams;<init>(Z)V"))
	private boolean lucerna$separateShadowBatches(boolean useBlockFaceCulling, @Local(argsOnly = true) TerrainRenderPass pass) {
		return ShadowRenderer.ACTIVE ? pass.isSorted() : useBlockFaceCulling;
	}
}
