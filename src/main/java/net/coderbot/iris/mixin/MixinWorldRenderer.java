package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.render.Culler;
import net.minecraft.client.render.block.BlockLayer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.world.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
	@Unique
	private static final String RENDER_SKY = "renderSky(FI)V";

	@Unique
	private static void iris$setPhase(WorldRenderingPhase phase) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null) {
			pipeline.setPhase(phase);
		}
	}

	@Inject(method = "render(Lnet/minecraft/client/render/block/BlockLayer;DILnet/minecraft/entity/Entity;)I", at = @At("HEAD"))
	private void iris$beginTerrainLayer(BlockLayer layer, double tickDelta, int anaglyphRenderPass, Entity camera, CallbackInfoReturnable<Integer> cir) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null) {
			pipeline.setPhase(WorldRenderingPhase.fromTerrainRenderType(layer));
			pipeline.beginSodiumTerrainRendering();
		}
	}

	@Inject(method = "render(Lnet/minecraft/client/render/block/BlockLayer;DILnet/minecraft/entity/Entity;)I", at = @At("RETURN"))
	private void iris$endTerrainLayer(BlockLayer layer, double tickDelta, int anaglyphRenderPass, Entity camera, CallbackInfoReturnable<Integer> cir) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null) {
			pipeline.endSodiumTerrainRendering();
			pipeline.setPhase(WorldRenderingPhase.NONE);
		}
	}

	@Inject(method = RENDER_SKY, at = @At("HEAD"))
	private void iris$beginSky(float tickDelta, int anaglyphRenderPass, CallbackInfo ci) {
		iris$setPhase(WorldRenderingPhase.SKY);
	}

	@Inject(method = RENDER_SKY, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/Dimension;getSunriseColor(FF)[F"))
	private void iris$beginSunset(float tickDelta, int anaglyphRenderPass, CallbackInfo ci) {
		iris$setPhase(WorldRenderingPhase.SUNSET);
	}

	@Inject(method = RENDER_SKY, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getTimeOfDay(F)F", ordinal = 1))
	private void iris$tiltSun(float tickDelta, int anaglyphRenderPass, CallbackInfo ci) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null) {
			GlStateManager.rotatef(pipeline.getSunPathRotation(), 0.0F, 0.0F, 1.0F);
		}
	}

	@Inject(method = RENDER_SKY, at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/world/WorldRenderer;SUN_LOCATION:Lnet/minecraft/resource/Identifier;"))
	private void iris$beginSun(float tickDelta, int anaglyphRenderPass, CallbackInfo ci) {
		iris$setPhase(WorldRenderingPhase.SUN);
	}

	@Inject(method = RENDER_SKY, at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/world/WorldRenderer;MOON_PHASES_LOCATION:Lnet/minecraft/resource/Identifier;"))
	private void iris$beginMoon(float tickDelta, int anaglyphRenderPass, CallbackInfo ci) {
		iris$setPhase(WorldRenderingPhase.MOON);
	}

	@Inject(method = RENDER_SKY, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getStarBrightness(F)F"))
	private void iris$beginStars(float tickDelta, int anaglyphRenderPass, CallbackInfo ci) {
		iris$setPhase(WorldRenderingPhase.STARS);
	}

	@Inject(method = RENDER_SKY, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/living/player/LocalClientPlayerEntity;getEyePosition(F)Lnet/minecraft/util/math/Vec3d;"))
	private void iris$beginVoid(float tickDelta, int anaglyphRenderPass, CallbackInfo ci) {
		iris$setPhase(WorldRenderingPhase.VOID);
	}

	@Inject(method = RENDER_SKY, at = @At("RETURN"))
	private void iris$endSky(float tickDelta, int anaglyphRenderPass, CallbackInfo ci) {
		iris$setPhase(WorldRenderingPhase.NONE);
	}

	@Inject(method = "renderEntities", at = @At("HEAD"))
	private void iris$beginEntities(Entity camera, Culler culler, float tickDelta, CallbackInfo ci) {
		iris$setPhase(WorldRenderingPhase.ENTITIES);
	}

	@Inject(method = "renderEntities", at = @At("RETURN"))
	private void iris$endEntities(Entity camera, Culler culler, float tickDelta, CallbackInfo ci) {
		iris$setPhase(WorldRenderingPhase.NONE);
	}

	@Inject(method = "renderBlockOutline", at = @At("HEAD"))
	private void iris$beginOutline(PlayerEntity camera, HitResult hit, int mode, float tickDelta, CallbackInfo ci) {
		iris$setPhase(WorldRenderingPhase.OUTLINE);
	}

	@Inject(method = "renderBlockOutline", at = @At("RETURN"))
	private void iris$endOutline(PlayerEntity camera, HitResult hit, int mode, float tickDelta, CallbackInfo ci) {
		iris$setPhase(WorldRenderingPhase.NONE);
	}

	@Inject(method = "renderMiningProgress", at = @At("HEAD"))
	private void iris$beginDestroy(Tesselator tesselator, BufferBuilder bufferBuilder, Entity camera, float tickDelta, CallbackInfo ci) {
		iris$setPhase(WorldRenderingPhase.DESTROY);
	}

	@Inject(method = "renderMiningProgress", at = @At("RETURN"))
	private void iris$endDestroy(Tesselator tesselator, BufferBuilder bufferBuilder, Entity camera, float tickDelta, CallbackInfo ci) {
		iris$setPhase(WorldRenderingPhase.NONE);
	}

	@Inject(method = "renderWorldBorder", at = @At("HEAD"))
	private void iris$beginWorldBorder(Entity camera, float tickDelta, CallbackInfo ci) {
		iris$setPhase(WorldRenderingPhase.WORLD_BORDER);
	}

	@Inject(method = "renderWorldBorder", at = @At("RETURN"))
	private void iris$endWorldBorder(Entity camera, float tickDelta, CallbackInfo ci) {
		iris$setPhase(WorldRenderingPhase.NONE);
	}
}
