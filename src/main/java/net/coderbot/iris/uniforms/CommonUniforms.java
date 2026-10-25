package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.state.StateUpdateNotifiers;
import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.layer.GbufferPrograms;
import net.coderbot.iris.shaderpack.IdMap;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.texture.TextureInfoCache;
import net.coderbot.iris.texture.TextureInfoCache.TextureInfo;
import net.coderbot.iris.texture.TextureTracker;
import net.coderbot.iris.uniforms.transforms.SmoothedFloat;
import net.coderbot.iris.uniforms.transforms.SmoothedVec2f;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.texture.AbstractTexture;
import net.minecraft.client.render.texture.TextureAtlas;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.living.effect.StatusEffect;
import net.minecraft.entity.living.effect.StatusEffectInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector4f;
import org.joml.Vector4i;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.ONCE;
import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;
import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_TICK;

public final class CommonUniforms {
	private static final Minecraft client = Minecraft.getInstance();
	private static final Vector2i ZERO_VECTOR_2i = new Vector2i();
	private static final Vector4i ZERO_VECTOR_4i = new Vector4i(0, 0, 0, 0);
	private static final Vector3d ZERO_VECTOR_3d = new Vector3d();

	private CommonUniforms() {
		// no construction allowed
	}

	// Needs to use a LocationalUniformHolder as we need it for the common uniforms
	public static void addCommonUniforms(DynamicUniformHolder uniforms, IdMap idMap, PackDirectives directives, FrameUpdateNotifier updateNotifier) {
		CameraUniforms.addCameraUniforms(uniforms, updateNotifier);
		ViewportUniforms.addViewportUniforms(uniforms);
		WorldTimeUniforms.addWorldTimeUniforms(uniforms);
		SystemTimeUniforms.addSystemTimeUniforms(uniforms);
		new CelestialUniforms(directives.getSunPathRotation()).addCelestialUniforms(uniforms);
		IdMapUniforms.addIdMapUniforms(updateNotifier, uniforms, idMap, directives.isOldHandLight());
		IrisExclusiveUniforms.addIrisExclusiveUniforms(uniforms);
		MatrixUniforms.addMatrixUniforms(uniforms, directives);
		HardcodedCustomUniforms.addHardcodedCustomUniforms(uniforms, updateNotifier);
		FogUniforms.addFogUniforms(uniforms);

		// TODO: OptiFine doesn't think that atlasSize is a "dynamic" uniform,
		//       but we do. How will custom uniforms depending on atlasSize work?
		uniforms.uniform2i("atlasSize", () -> {
			int glId = GlStateManager.TEXTURES[0].texture;

			AbstractTexture texture = TextureTracker.INSTANCE.getTexture(glId);
			if (texture instanceof TextureAtlas) {
				TextureInfo info = TextureInfoCache.INSTANCE.getInfo(glId);
				return new Vector2i(info.getWidth(), info.getHeight());
			}

			return ZERO_VECTOR_2i;
		}, StateUpdateNotifiers.bindTextureNotifier);

		uniforms.uniform2i("gtextureSize", () -> {
			int glId = GlStateManager.TEXTURES[0].texture;

			TextureInfo info = TextureInfoCache.INSTANCE.getInfo(glId);
			return new Vector2i(info.getWidth(), info.getHeight());

		}, StateUpdateNotifiers.bindTextureNotifier);

		uniforms.uniform4i("blendFunc", () -> {
			GlStateManager.Blend blend = GlStateManager.BLEND;

			if (blend.state.enabled) {
				return new Vector4i(blend.sfactorRGB, blend.dfactorRGB, blend.sfactorAlpha, blend.dfactorAlpha);
			} else {
				return ZERO_VECTOR_4i;
			}
		}, StateUpdateNotifiers.blendFuncNotifier);

		uniforms.uniform1i("renderStage", () -> GbufferPrograms.getCurrentPhase().ordinal(), StateUpdateNotifiers.phaseChangeNotifier);

		CommonUniforms.generalCommonUniforms(uniforms, updateNotifier, directives);
	}

	public static void generalCommonUniforms(UniformHolder uniforms, FrameUpdateNotifier updateNotifier, PackDirectives directives) {
		ExternallyManagedUniforms.addExternallyManagedUniforms116(uniforms);

		SmoothedVec2f eyeBrightnessSmooth = new SmoothedVec2f(directives.getEyeBrightnessHalfLife(), directives.getEyeBrightnessHalfLife(), CommonUniforms::getEyeBrightness, updateNotifier);

		uniforms
			.uniform1b(PER_FRAME, "hideGUI", () -> client.options.hideGui)
			.uniform1f(PER_FRAME, "eyeAltitude", () -> client.getCamera().y + client.getCamera().getEyeHeight())
			.uniform1i(PER_FRAME, "isEyeInWater", CommonUniforms::isEyeInWater)
			.uniform1f(PER_FRAME, "blindness", CommonUniforms::getBlindness)
			.uniform1f(PER_FRAME, "nightVision", CommonUniforms::getNightVision)
			// TODO: Do we need to clamp this to avoid fullbright breaking shaders? Or should shaders be able to detect
			//       that the player is trying to turn on fullbright?
			.uniform1f(PER_FRAME, "screenBrightness", () -> client.options.gamma)
			// just a dummy value for shaders where entityColor isn't supplied through a vertex attribute (and thus is
			// not available) - suppresses warnings. See AttributeShaderTransformer for the actual entityColor code.
			.uniform4f(ONCE, "entityColor", Vector4f::new)
			.uniform1f(PER_TICK, "playerMood", CommonUniforms::getPlayerMood)
			.uniform2i(PER_FRAME, "eyeBrightness", CommonUniforms::getEyeBrightness)
			.uniform2i(PER_FRAME, "eyeBrightnessSmooth", () -> {
				Vector2f smoothed = eyeBrightnessSmooth.get();
				return new Vector2i((int) smoothed.x(),(int) smoothed.y());
			})
			.uniform1f(PER_TICK, "rainStrength", CommonUniforms::getRainStrength)
			.uniform1f(PER_TICK, "wetness", new SmoothedFloat(directives.getWetnessHalfLife(), directives.getDrynessHalfLife(), CommonUniforms::getRainStrength, updateNotifier))
			.uniform3d(PER_FRAME, "skyColor", CommonUniforms::getSkyColor)
			.uniform3d(PER_FRAME, "fogColor", CapturedRenderingState.INSTANCE::getFogColor);
	}

	private static Vector3d getSkyColor() {
		if (client.world == null || client.getCamera() == null) {
			return ZERO_VECTOR_3d;
		}

		Vec3d color = client.world.getSkyColor(client.getCamera(), CapturedRenderingState.INSTANCE.getTickDelta());
		return new Vector3d(color.x, color.y, color.z);
	}

	static float getBlindness() {
		Entity cameraEntity = client.getCamera();

		if (cameraEntity instanceof LivingEntity) {
			StatusEffectInstance blindness = ((LivingEntity) cameraEntity).getEffectInstance(StatusEffect.BLINDNESS);

			if (blindness != null) {
				return Math.clamp(0.0F, 1.0F, blindness.getDuration() / 20.0F);
			}
		}

		return 0.0F;
	}

	private static float getPlayerMood() {
		return 0.0F;
	}

	static float getRainStrength() {
		if (client.world == null) {
			return 0f;
		}

		return Math.clamp(0.0F, 1.0F, client.world.getRain(CapturedRenderingState.INSTANCE.getTickDelta()));
	}

	private static Vector2i getEyeBrightness() {
		Entity camera = client.getCamera();

		if (camera == null || client.world == null) {
			return ZERO_VECTOR_2i;
		}

		BlockPos eyeBlockPos = new BlockPos(camera.x, camera.y + camera.getEyeHeight(), camera.z);

		int blockLight = client.world.getBrightness(LightType.BLOCK, eyeBlockPos);
		int skyLight = client.world.getBrightness(LightType.SKY, eyeBlockPos);

		return new Vector2i(blockLight * 16, skyLight * 16);
	}

	private static float getNightVision() {
		Entity cameraEntity = client.getCamera();

		if (cameraEntity instanceof LivingEntity) {
			StatusEffectInstance nightVision = ((LivingEntity) cameraEntity).getEffectInstance(StatusEffect.NIGHTVISION);

			if (nightVision != null) {
				int duration = nightVision.getDuration();
				float tickDelta = CapturedRenderingState.INSTANCE.getTickDelta();
				return duration > 200 ? 1.0F : 0.7F + MathHelper.sin((duration - tickDelta) * (float) java.lang.Math.PI * 0.2F) * 0.3F;
			}
		}

		return 0.0F;
	}

	static int isEyeInWater() {
		if (client.world == null || client.getCamera() == null) {
			return 0;
		}

		Block block = Camera.getBlockInside(client.world, client.getCamera(), CapturedRenderingState.INSTANCE.getTickDelta());
		Material material = block.getMaterial();

		if (material == Material.WATER) {
			return 1;
		} else if (material == Material.LAVA) {
			return 2;
		} else {
			return 0;
		}
	}

	static {
		GbufferPrograms.init();
	}
}
