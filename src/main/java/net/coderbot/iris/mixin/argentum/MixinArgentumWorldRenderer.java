package net.coderbot.iris.mixin.argentum;

import dev.rdh.argentum.impl.render.terrain.ArgentumWorldRenderer;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.celeritas.XHFPModelVertexType;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Builds chunk meshes in the extended vertex format while a shader pack is loaded.
 */
@Mixin(value = ArgentumWorldRenderer.class, remap = false)
public class MixinArgentumWorldRenderer {
	@ModifyVariable(method = "createRenderSectionManager", at = @At("STORE"))
	private ChunkVertexType lucerna$useExtendedVertexFormat(ChunkVertexType vertexType) {
		return Iris.getCurrentPack().isPresent() ? XHFPModelVertexType.INSTANCE : vertexType;
	}
}
