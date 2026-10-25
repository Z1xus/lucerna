package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.global.LightningBoltEntity;
import net.minecraft.world.WorldSettings;
import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector4f;

public class IrisExclusiveUniforms {
	private static final Vector4f ZERO = new Vector4f(0, 0, 0, 0);

	public static void addIrisExclusiveUniforms(UniformHolder uniforms) {
		WorldInfoUniforms.addWorldInfoUniforms(uniforms);

		//All Iris-exclusive uniforms (uniforms which do not exist in either OptiFine or ShadersMod) should be registered here.
		uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME, "thunderStrength", IrisExclusiveUniforms::getThunderStrength);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "currentPlayerHealth", IrisExclusiveUniforms::getCurrentHealth);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "maxPlayerHealth", IrisExclusiveUniforms::getMaxHealth);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "currentPlayerHunger", IrisExclusiveUniforms::getCurrentHunger);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "maxPlayerHunger", () -> 20);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "currentPlayerAir", IrisExclusiveUniforms::getCurrentAir);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "maxPlayerAir", IrisExclusiveUniforms::getMaxAir);
		uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME, "firstPersonCamera", () -> Minecraft.getInstance().options.perspective == 0);
		uniforms.uniform1b(UniformUpdateFrequency.PER_TICK, "isSpectator", IrisExclusiveUniforms::isSpectator);
		uniforms.uniform3d(UniformUpdateFrequency.PER_FRAME, "eyePosition", IrisExclusiveUniforms::getEyePosition);
		uniforms.uniform4f(UniformUpdateFrequency.PER_TICK, "lightningBoltPosition", IrisExclusiveUniforms::getLightningBoltPosition);
	}

	private static Vector4f getLightningBoltPosition() {
		ClientWorld world = Minecraft.getInstance().world;

		if (world == null) {
			return ZERO;
		}

		for (Entity entity : world.globalEntities) {
			if (entity instanceof LightningBoltEntity) {
				Vector3d camera = CameraUniforms.getUnshiftedCameraPosition();
				return new Vector4f((float) (entity.x - camera.x), (float) (entity.y - camera.y), (float) (entity.z - camera.z), 1);
			}
		}

		return ZERO;
	}

	private static float getThunderStrength() {
		// Note: Ensure this is in the range of 0 to 1 - some custom servers send out of range values.
		return Math.clamp(0.0F, 1.0F,
			Minecraft.getInstance().world.getThunder(CapturedRenderingState.INSTANCE.getTickDelta()));
	}

	private static LocalClientPlayerEntity getSurvivalPlayer() {
		Minecraft client = Minecraft.getInstance();

		if (client.player == null || !client.interactionManager.getGameMode().isSurvival()) {
			return null;
		}

		return client.player;
	}

	private static float getCurrentHealth() {
		LocalClientPlayerEntity player = getSurvivalPlayer();
		return player == null ? -1 : player.getHealth() / player.getMaxHealth();
	}

	private static float getCurrentHunger() {
		LocalClientPlayerEntity player = getSurvivalPlayer();
		return player == null ? -1 : player.getHungerManager().getFoodLevel() / 20f;
	}

	private static float getCurrentAir() {
		LocalClientPlayerEntity player = getSurvivalPlayer();
		return player == null ? -1 : player.getBreath() / 300f;
	}

	private static float getMaxAir() {
		return getSurvivalPlayer() == null ? -1 : 300;
	}

	private static float getMaxHealth() {
		LocalClientPlayerEntity player = getSurvivalPlayer();
		return player == null ? -1 : player.getMaxHealth();
	}

	private static boolean isSpectator() {
		return Minecraft.getInstance().interactionManager.getGameMode() == WorldSettings.GameMode.SPECTATOR;
	}

	private static Vector3d getEyePosition() {
		Entity camera = Minecraft.getInstance().getCamera();
		return new Vector3d(camera.x, camera.y + camera.getEyeHeight(), camera.z);
	}

	public static class WorldInfoUniforms {
		public static void addWorldInfoUniforms(UniformHolder uniforms) {
			uniforms.uniform1i(UniformUpdateFrequency.PER_FRAME, "bedrockLevel", () -> 0);
			uniforms.uniform1i(UniformUpdateFrequency.PER_FRAME, "heightLimit", () -> 256);
			uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME, "hasCeiling", () -> {
				ClientWorld world = Minecraft.getInstance().world;
				return world != null && world.dimension.getId() == -1;
			});
			uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME, "hasSkylight", () -> {
				ClientWorld world = Minecraft.getInstance().world;
				return world == null || !world.dimension.hasNoSky();
			});
			uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME, "ambientLight", () -> {
				ClientWorld world = Minecraft.getInstance().world;
				return world != null ? world.dimension.getBrightnessTable()[0] : 0f;
			});
		}
	}
}
