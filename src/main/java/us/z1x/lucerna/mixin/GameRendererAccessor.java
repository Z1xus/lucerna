package us.z1x.lucerna.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
	@Accessor("lightmapTexture")
	DynamicTexture lucerna$getLightmapTexture();

	@Accessor("renderDistance")
	float lucerna$getRenderDistance();

	@Accessor("newCamPitch")
	boolean lucerna$shouldRenderHand();

	@Invoker("getFov")
	float lucerna$getFov(float tickDelta, boolean fovChanged);

	@Invoker("applyHurtCam")
	void lucerna$applyHurtCam(float tickDelta);

	@Invoker("applyViewBobbing")
	void lucerna$applyViewBobbing(float tickDelta);
}
