package net.coderbot.iris.mixin;

import dev.rdh.argentum.impl.render.terrain.ArgentumWorldRenderer;
import org.embeddedt.embeddium.impl.render.chunk.ChunkRenderMatrices;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ArgentumWorldRenderer.class, remap = false)
public interface ArgentumWorldRendererAccessor {
	@Accessor("matrices")
	ChunkRenderMatrices getMatrices();

	@Accessor("matrices")
	void setMatrices(ChunkRenderMatrices matrices);
}
