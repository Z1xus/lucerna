package net.coderbot.iris.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.CloudRenderer;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Culler;
import net.minecraft.client.render.block.BlockLayer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.client.render.texture.TextureManager;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.resource.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
	@Shadow
	@Final
	private static Identifier CLOUDS_LOCATION;

	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	@Final
	private TextureManager textureManager;

	@Shadow
	private ClientWorld world;

	@Shadow
	private int ticks;

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

	@WrapWithCondition(method = RENDER_SKY, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/vertex/Tesselator;end()V", ordinal = 1))
	private boolean iris$renderSun(Tesselator tesselator) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		return pipeline == null || pipeline.shouldRenderSun();
	}

	@WrapWithCondition(method = RENDER_SKY, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/vertex/Tesselator;end()V", ordinal = 2))
	private boolean iris$renderMoon(Tesselator tesselator) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		return pipeline == null || pipeline.shouldRenderMoon();
	}

	@Inject(method = RENDER_SKY, at = @At("RETURN"))
	private void iris$endSky(float tickDelta, int anaglyphRenderPass, CallbackInfo ci) {
		iris$setPhase(WorldRenderingPhase.NONE);
	}

	@Inject(method = "renderFancyClouds", at = @At("HEAD"), cancellable = true)
	private void iris$renderCachedClouds(float tickDelta, int anaglyphRenderPass, CallbackInfo ci) {
		if (!IrisApi.getInstance().isShaderPackInUse()) {
			return;
		}

		Entity camera = minecraft.getCamera();
		double ticks = this.ticks + tickDelta;
		double x = (camera.lastX + (camera.x - camera.lastX) * tickDelta + ticks * 0.03F) / 12.0;
		double z = (camera.lastZ + (camera.z - camera.lastZ) * tickDelta) / 12.0 + 0.33F;
		float height = world.dimension.getCloudHeight() - (float) (camera.prevY + (camera.y - camera.prevY) * tickDelta) + 0.33F;
		Vec3d color = world.getCloudColor(tickDelta);
		float red = (float) color.x;
		float green = (float) color.y;
		float blue = (float) color.z;

		if (anaglyphRenderPass != 2) {
			float gray = (red * 30.0F + green * 59.0F + blue * 11.0F) / 100.0F;
			float redGreen = (red * 30.0F + green * 70.0F) / 100.0F;
			float redBlue = (red * 30.0F + blue * 70.0F) / 100.0F;
			red = gray;
			green = redGreen;
			blue = redBlue;
		}

		GlStateManager.disableCull();
		textureManager.bind(CLOUDS_LOCATION);
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(770, 771, 1, 0);
		CloudRenderer.INSTANCE.render(x - MathHelper.floor(x / 2048.0) * 2048, z - MathHelper.floor(z / 2048.0) * 2048, height, red, green, blue, anaglyphRenderPass);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
		ci.cancel();
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
