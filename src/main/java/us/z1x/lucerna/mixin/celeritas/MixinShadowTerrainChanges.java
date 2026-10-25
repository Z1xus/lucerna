package us.z1x.lucerna.mixin.celeritas;

import us.z1x.lucerna.pipeline.ShadowRenderer;
import org.embeddedt.embeddium.impl.render.chunk.data.SectionRenderDataStorage;
import org.embeddedt.embeddium.impl.render.chunk.data.SectionRenderDataUnsafe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// opaque meshes changing invalidates the cached shadow terrain, sorted translucent storage is drawn every frame anyway
@Mixin(value = SectionRenderDataStorage.class, remap = false)
public class MixinShadowTerrainChanges {
    @Shadow
    @Final
    private SectionRenderDataUnsafe.Strategy storageStrategy;

    @Inject(method = {"setMeshes", "removeMeshes"}, at = @At("HEAD"))
    private void lucerna$invalidateShadowTerrain(CallbackInfo ci) {
        if (storageStrategy != SectionRenderDataUnsafe.Strategy.FULL) {
            ShadowRenderer.terrainChanged = true;
        }
    }
}
