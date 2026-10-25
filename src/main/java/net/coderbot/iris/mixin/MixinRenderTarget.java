package net.coderbot.iris.mixin;

import net.coderbot.iris.rendertarget.Blaze3dRenderTargetExt;
import net.minecraft.client.render.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Allows Iris to detect when the color texture was re-created, so we can re-attach it
 * to the final pass framebuffer. See FinalPassRenderer.
 */
@Mixin(RenderTarget.class)
public class MixinRenderTarget implements Blaze3dRenderTargetExt {
	@Unique
	private int iris$depthBufferVersion;

	@Unique
	private int iris$colorBufferVersion;

	@Inject(method = "destroyBuffers()V", at = @At("HEAD"))
	private void iris$onDestroyBuffers(CallbackInfo ci) {
		iris$depthBufferVersion++;
		iris$colorBufferVersion++;
	}

	@Override
	public int iris$getDepthBufferVersion() {
		return iris$depthBufferVersion;
	}

	@Override
	public int iris$getColorBufferVersion() {
		return iris$colorBufferVersion;
	}
}
