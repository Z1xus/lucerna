package net.coderbot.iris.mixin.entity_render_context;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.coderbot.iris.Iris;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.uniforms.CapturedRenderingState;
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
	private WorldRenderingPhase iris$previousPhase;

	@Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;DDDFI)V", at = @At("HEAD"))
	private void iris$beginBlockEntityRender(BlockEntity blockEntity, double dx, double dy, double dz, float tickDelta, int blockMiningProgress, CallbackInfo ci) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline == null) {
			return;
		}

		iris$previousPhase = pipeline.getPhase();
		pipeline.setPhase(WorldRenderingPhase.BLOCK_ENTITIES);

		Object2IntMap<BlockState> blockStateIds = BlockRenderingSettings.INSTANCE.getBlockStateIds();

		if (blockStateIds != null) {
			Block block = blockEntity.getBlock();
			CapturedRenderingState.INSTANCE.setCurrentBlockEntity(blockStateIds.getInt(block.getStateFromMetadata(blockEntity.getBlockMetadata())));
		}
	}

	@Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;DDDFI)V", at = @At("RETURN"))
	private void iris$endBlockEntityRender(BlockEntity blockEntity, double dx, double dy, double dz, float tickDelta, int blockMiningProgress, CallbackInfo ci) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline == null) {
			return;
		}

		pipeline.setPhase(iris$previousPhase);
		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(0);
	}
}
