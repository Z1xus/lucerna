package us.z1x.lucerna.mixin.argentum;

import dev.rdh.argentum.impl.render.terrain.ArgentumWorldRenderer;
import org.embeddedt.embeddium.impl.render.chunk.ChunkRenderMatrices;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ArgentumWorldRenderer.class, remap = false)
public interface ArgentumWorldRendererAccessor {
    @Accessor("matrices")
    ChunkRenderMatrices lucerna$getMatrices();

    @Accessor("matrices")
    void lucerna$setMatrices(ChunkRenderMatrices matrices);
}
