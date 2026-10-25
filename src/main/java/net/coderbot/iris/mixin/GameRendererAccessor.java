package net.coderbot.iris.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
	@Accessor("lightmapTexture")
	DynamicTexture getLightMapTexture();

	@Accessor("renderDistance")
	float getRenderDistance();

	@Accessor("newCamPitch")
	boolean shouldRenderHand();

	@Invoker("getFov")
	float invokeGetFov(float tickDelta, boolean fovChanged);

	@Invoker("applyHurtCam")
	void invokeApplyHurtCam(float tickDelta);

	@Invoker("applyViewBobbing")
	void invokeApplyViewBobbing(float tickDelta);

	@Invoker("setupFog")
	void invokeSetupFog(int mode, float tickDelta);

	@Invoker("renderSnowAndRain")
	void invokeRenderSnowAndRain(float tickDelta);
}
