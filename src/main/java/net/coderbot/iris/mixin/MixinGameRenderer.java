package net.coderbot.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.pipeline.HandRenderer;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.SystemTimeUniforms;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleManager;
import net.minecraft.client.render.Culler;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.platform.Lighting;
import net.minecraft.client.render.texture.TextureAtlas;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.LivingEntity;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.FloatBuffer;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
	@Unique
	private static final String RENDER_WORLD_PASS = "render(IFJ)V";

	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	private float fogRed;

	@Shadow
	private float fogGreen;

	@Shadow
	private float fogBlue;

	@Unique
	private final FloatBuffer iris$matrixBuffer = BufferUtils.createFloatBuffer(16);

	@Unique
	private final Matrix4f iris$matrix = new Matrix4f();

	@Unique
	private WorldRenderingPipeline iris$pipeline;

	@Unique
	private boolean iris$deferParticlesAndWeather;

	@Inject(method = "setupClearColor", at = @At("RETURN"))
	private void iris$captureFogColor(float tickDelta, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setFogColor(fogRed, fogGreen, fogBlue);
	}

	// Begin shader rendering once the camera has been set up, so that the gbuffer matrices are known.
	@Inject(method = RENDER_WORLD_PASS, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setup(Lnet/minecraft/entity/living/player/PlayerEntity;Z)V", shift = At.Shift.AFTER))
	private void iris$beginLevelRender(int anaglyphRenderPass, float tickDelta, long renderTimeLimit, CallbackInfo ci) {
		GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, iris$matrixBuffer);
		CapturedRenderingState.INSTANCE.setGbufferModelView(iris$matrix.set(iris$matrixBuffer));
		GL11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, iris$matrixBuffer);
		CapturedRenderingState.INSTANCE.setGbufferProjection(iris$matrix.set(iris$matrixBuffer));
		CapturedRenderingState.INSTANCE.setTickDelta(tickDelta);
		SystemTimeUniforms.COUNTER.beginFrame();
		SystemTimeUniforms.TIMER.beginFrame(System.nanoTime());

		Program.unbind();

		iris$pipeline = Iris.getPipelineManager().preparePipeline(Iris.getCurrentDimension());
		iris$deferParticlesAndWeather = IrisApi.getInstance().isShaderPackInUse() && !iris$pipeline.shouldRenderParticlesBeforeDeferred();

		iris$pipeline.beginLevelRendering();
	}

	// Render shadows before the main terrain setup, since the shadow pass submits the terrain search for this frame.
	@Inject(method = RENDER_WORLD_PASS, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/world/WorldRenderer;setupRender(Lnet/minecraft/entity/Entity;DLnet/minecraft/client/render/Culler;IZ)V"))
	private void iris$renderShadows(int anaglyphRenderPass, float tickDelta, long renderTimeLimit, CallbackInfo ci, @Local Culler culler) {
		iris$pipeline.renderShadows(minecraft.worldRenderer, minecraft.getCamera(), culler);
	}

	@WrapOperation(method = RENDER_WORLD_PASS, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderClouds(Lnet/minecraft/client/render/world/WorldRenderer;FI)V"))
	private void iris$renderClouds(GameRenderer gameRenderer, WorldRenderer worldRenderer, float tickDelta, int anaglyphRenderPass, Operation<Void> original) {
		iris$pipeline.setPhase(WorldRenderingPhase.CLOUDS);
		original.call(gameRenderer, worldRenderer, tickDelta, anaglyphRenderPass);
		iris$pipeline.setPhase(WorldRenderingPhase.NONE);
	}

	@WrapOperation(method = RENDER_WORLD_PASS, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ParticleManager;renderLit(Lnet/minecraft/entity/Entity;F)V"))
	private void iris$renderLitParticles(ParticleManager particleManager, Entity camera, float tickDelta, Operation<Void> original) {
		if (!iris$deferParticlesAndWeather) {
			iris$pipeline.setPhase(WorldRenderingPhase.PARTICLES);
			original.call(particleManager, camera, tickDelta);
			iris$pipeline.setPhase(WorldRenderingPhase.NONE);
		}
	}

	@WrapOperation(method = RENDER_WORLD_PASS, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ParticleManager;render(Lnet/minecraft/entity/Entity;F)V"))
	private void iris$renderParticles(ParticleManager particleManager, Entity camera, float tickDelta, Operation<Void> original) {
		if (!iris$deferParticlesAndWeather) {
			iris$pipeline.setPhase(WorldRenderingPhase.PARTICLES);
			original.call(particleManager, camera, tickDelta);
			iris$pipeline.setPhase(WorldRenderingPhase.NONE);
		}
	}

	@WrapOperation(method = RENDER_WORLD_PASS, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderSnowAndRain(F)V"))
	private void iris$renderWeather(GameRenderer gameRenderer, float tickDelta, Operation<Void> original) {
		if (!iris$deferParticlesAndWeather) {
			iris$pipeline.setPhase(WorldRenderingPhase.RAIN_SNOW);
			GlStateManager.depthMask(iris$pipeline.shouldWriteRainAndSnowToDepthBuffer());
			original.call(gameRenderer, tickDelta);
			iris$pipeline.setPhase(WorldRenderingPhase.NONE);
		}
	}

	@Inject(method = RENDER_WORLD_PASS, at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=translucent"))
	private void iris$beginTranslucents(int anaglyphRenderPass, float tickDelta, long renderTimeLimit, CallbackInfo ci) {
		if (!IrisApi.getInstance().isShaderPackInUse()) {
			return;
		}

		iris$pipeline.beginHand();

		GlStateManager.depthMask(true);
		GlStateManager.disableBlend();
		HandRenderer.INSTANCE.renderSolid(tickDelta, iris$pipeline);
		GlStateManager.enableBlend();
		GlStateManager.depthMask(false);

		minecraft.profiler.swap("iris_pre_translucent");
		iris$pipeline.beginTranslucents();

		// the deferred passes unbind every texture unit
		minecraft.getTextureManager().bind(TextureAtlas.BLOCKS_LOCATION);
	}

	@Inject(method = RENDER_WORLD_PASS, at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=hand"))
	private void iris$renderDeferredParticlesAndWeather(int anaglyphRenderPass, float tickDelta, long renderTimeLimit, CallbackInfo ci) {
		if (!iris$deferParticlesAndWeather) {
			return;
		}

		GameRendererAccessor accessor = (GameRendererAccessor) this;
		Entity camera = minecraft.getCamera();

		iris$pipeline.setPhase(WorldRenderingPhase.PARTICLES);
		((GameRenderer) (Object) this).enableLightMap();
		minecraft.particleManager.renderLit(camera, tickDelta);
		Lighting.turnOff();
		accessor.invokeSetupFog(0, tickDelta);
		minecraft.particleManager.render(camera, tickDelta);
		((GameRenderer) (Object) this).disableLightMap();

		iris$pipeline.setPhase(WorldRenderingPhase.RAIN_SNOW);
		GlStateManager.depthMask(iris$pipeline.shouldWriteRainAndSnowToDepthBuffer());
		GlStateManager.enableCull();
		accessor.invokeRenderSnowAndRain(tickDelta);
		GlStateManager.depthMask(true);
		GlStateManager.disableBlend();
		GlStateManager.disableFog();
		iris$pipeline.setPhase(WorldRenderingPhase.NONE);
	}

	// The hand is rendered into the gbuffers before the translucent terrain, so skip the vanilla hand pass.
	@Redirect(method = RENDER_WORLD_PASS, at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;newCamPitch:Z"))
	private boolean iris$skipVanillaHand(GameRenderer gameRenderer) {
		return ((GameRendererAccessor) gameRenderer).shouldRenderHand() && !IrisApi.getInstance().isShaderPackInUse();
	}

	@Inject(method = RENDER_WORLD_PASS, at = @At("RETURN"))
	private void iris$endLevelRender(int anaglyphRenderPass, float tickDelta, long renderTimeLimit, CallbackInfo ci) {
		minecraft.profiler.swap("iris_final");
		iris$pipeline.finalizeLevelRendering();
		iris$pipeline = null;
		Program.unbind();

		if (IrisApi.getInstance().isShaderPackInUse()) {
			iris$renderScreenEffects(tickDelta);
		}
	}

	@Unique
	private void iris$renderScreenEffects(float tickDelta) {
		Entity camera = minecraft.getCamera();

		if (minecraft.options.perspective != 0 || camera instanceof LivingEntity && ((LivingEntity) camera).isSleeping()) {
			return;
		}

		GameRendererAccessor accessor = (GameRendererAccessor) this;

		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.loadIdentity();
		Project.gluPerspective(accessor.invokeGetFov(tickDelta, false), (float) minecraft.width / minecraft.height, 0.05F, accessor.getRenderDistance() * 2.0F);
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.loadIdentity();

		((GameRenderer) (Object) this).itemInHandRenderer.renderScreenEffects(tickDelta);
	}
}
