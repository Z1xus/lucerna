package us.z1x.lucerna.mixin.entity_render_context;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import us.z1x.lucerna.Iris;
import us.z1x.lucerna.block_rendering.BlockRenderingSettings;
import us.z1x.lucerna.pipeline.WorldRenderingPhase;
import us.z1x.lucerna.pipeline.WorldRenderingPipeline;
import us.z1x.lucerna.uniforms.CapturedRenderingState;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher {
    @Unique
    private WorldRenderingPhase lucerna$previousPhase;

    @Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;DDDFI)V", at = @At("HEAD"))
    private void lucerna$beginBlockEntityRender(BlockEntity blockEntity, double dx, double dy, double dz, float tickDelta, int blockMiningProgress, CallbackInfo ci) {
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

        if (pipeline == null) {
            return;
        }

        lucerna$previousPhase = pipeline.getPhase();
        pipeline.setPhase(WorldRenderingPhase.BLOCK_ENTITIES);

        Object2IntMap<BlockState> blockStateIds = BlockRenderingSettings.INSTANCE.getBlockStateIds();

        if (blockStateIds != null) {
            Block block = blockEntity.getBlock();
            CapturedRenderingState.INSTANCE.setCurrentBlockEntity(blockStateIds.getInt(block.getStateFromMetadata(blockEntity.getBlockMetadata())));
        }
    }

    @Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;DDDFI)V", at = @At("RETURN"))
    private void lucerna$endBlockEntityRender(BlockEntity blockEntity, double dx, double dy, double dz, float tickDelta, int blockMiningProgress, CallbackInfo ci) {
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

        if (pipeline == null) {
            return;
        }

        pipeline.setPhase(lucerna$previousPhase);
        CapturedRenderingState.INSTANCE.setCurrentBlockEntity(0);
    }
}
