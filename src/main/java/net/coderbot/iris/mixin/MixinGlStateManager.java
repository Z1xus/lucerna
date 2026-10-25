package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gbuffer_overrides.state.StateTracker;
import net.coderbot.iris.gl.blending.AlphaTestStorage;
import net.coderbot.iris.gl.blending.BlendModeStorage;
import net.coderbot.iris.gl.blending.DepthColorStorage;
import net.coderbot.iris.gl.state.StateUpdateNotifiers;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.samplers.IrisSamplers;
import net.coderbot.iris.texture.TextureInfoCache;
import net.coderbot.iris.texture.TextureTracker;
import net.minecraft.client.render.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class MixinGlStateManager {
	@Unique
	private static final int iris$TEXTURE_UNITS = 32;

	@Shadow
	private static int texture;

	@Unique
	private static Runnable fogToggleListener;
	@Unique
	private static Runnable fogModeListener;
	@Unique
	private static Runnable fogStartListener;
	@Unique
	private static Runnable fogEndListener;
	@Unique
	private static Runnable fogDensityListener;
	@Unique
	private static Runnable blendFuncListener;

	static {
		StateUpdateNotifiers.fogToggleNotifier = listener -> fogToggleListener = listener;
		StateUpdateNotifiers.fogModeNotifier = listener -> fogModeListener = listener;
		StateUpdateNotifiers.fogStartNotifier = listener -> fogStartListener = listener;
		StateUpdateNotifiers.fogEndNotifier = listener -> fogEndListener = listener;
		StateUpdateNotifiers.fogDensityNotifier = listener -> fogDensityListener = listener;
		StateUpdateNotifiers.blendFuncNotifier = listener -> blendFuncListener = listener;
	}

	// vanilla only tracks 8 texture units, shader packs bind far more than that
	@ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 8, ordinal = 1))
	private static int iris$increaseTextureArray(int units) {
		return iris$TEXTURE_UNITS;
	}

	@ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 8, ordinal = 3))
	private static int iris$increaseTextureLoop(int units) {
		return iris$TEXTURE_UNITS;
	}

	@Unique
	private static void iris$updateInputs() {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null) {
			pipeline.setInputs(StateTracker.INSTANCE.getInputs());
		}
	}

	@Inject(method = "enableTexture", at = @At("HEAD"))
	private static void iris$onEnableTexture(CallbackInfo ci) {
		if (texture == IrisSamplers.ALBEDO_TEXTURE_UNIT) {
			StateTracker.INSTANCE.albedoSampler = true;
		} else if (texture == IrisSamplers.LIGHTMAP_TEXTURE_UNIT) {
			StateTracker.INSTANCE.lightmapSampler = true;
		} else {
			return;
		}

		iris$updateInputs();
	}

	@Inject(method = "disableTexture", at = @At("HEAD"))
	private static void iris$onDisableTexture(CallbackInfo ci) {
		if (texture == IrisSamplers.ALBEDO_TEXTURE_UNIT) {
			StateTracker.INSTANCE.albedoSampler = false;
		} else if (texture == IrisSamplers.LIGHTMAP_TEXTURE_UNIT) {
			StateTracker.INSTANCE.lightmapSampler = false;
		} else {
			return;
		}

		iris$updateInputs();
	}

	@Inject(method = "callList", at = @At("HEAD"))
	private static void iris$beforeCallList(int list, CallbackInfo ci) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null) {
			pipeline.syncProgram();
		}
	}

	@Inject(method = "bindTexture", at = @At("RETURN"))
	private static void iris$onBindTexture(int id, CallbackInfo ci) {
		TextureTracker.INSTANCE.onBindTexture(id);
	}

	@Inject(method = "deleteTextures", at = @At("RETURN"))
	private static void iris$onDeleteTexture(int id, CallbackInfo ci) {
		TextureTracker.INSTANCE.onDeleteTexture(id);
		TextureInfoCache.INSTANCE.onDeleteTexture(id);
	}

	@Inject(method = { "enableFog", "disableFog" }, at = @At("RETURN"))
	private static void iris$onFogToggle(CallbackInfo ci) {
		if (fogToggleListener != null) {
			fogToggleListener.run();
		}
	}

	@Inject(method = "fogMode", at = @At("RETURN"))
	private static void iris$onFogMode(int mode, CallbackInfo ci) {
		if (fogModeListener != null) {
			fogModeListener.run();
		}
	}

	@Inject(method = "fogDensity", at = @At("RETURN"))
	private static void iris$onFogDensity(float density, CallbackInfo ci) {
		if (fogDensityListener != null) {
			fogDensityListener.run();
		}
	}

	@Inject(method = "fogStart", at = @At("RETURN"))
	private static void iris$onFogStart(float start, CallbackInfo ci) {
		if (fogStartListener != null) {
			fogStartListener.run();
		}
	}

	@Inject(method = "fogEnd", at = @At("RETURN"))
	private static void iris$onFogEnd(float end, CallbackInfo ci) {
		if (fogEndListener != null) {
			fogEndListener.run();
		}
	}

	@Inject(method = "disableBlend", at = @At("HEAD"), cancellable = true)
	private static void iris$blendDisableLock(CallbackInfo ci) {
		if (BlendModeStorage.isBlendLocked()) {
			BlendModeStorage.deferBlendModeToggle(false);
			ci.cancel();
		}
	}

	@Inject(method = "enableBlend", at = @At("HEAD"), cancellable = true)
	private static void iris$blendEnableLock(CallbackInfo ci) {
		if (BlendModeStorage.isBlendLocked()) {
			BlendModeStorage.deferBlendModeToggle(true);
			ci.cancel();
		}
	}

	@Inject(method = "blendFunc", at = @At("HEAD"), cancellable = true)
	private static void iris$blendFuncLock(int srcFactor, int dstFactor, CallbackInfo ci) {
		if (BlendModeStorage.isBlendLocked()) {
			BlendModeStorage.deferBlendFunc(srcFactor, dstFactor, srcFactor, dstFactor);
			ci.cancel();
		}
	}

	@Inject(method = "blendFuncSeparate", at = @At("HEAD"), cancellable = true)
	private static void iris$blendFuncSeparateLock(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha, CallbackInfo ci) {
		if (BlendModeStorage.isBlendLocked()) {
			BlendModeStorage.deferBlendFunc(srcRgb, dstRgb, srcAlpha, dstAlpha);
			ci.cancel();
		}
	}

	@Inject(method = { "blendFunc", "blendFuncSeparate" }, at = @At("RETURN"))
	private static void iris$onBlendFunc(CallbackInfo ci) {
		if (blendFuncListener != null) {
			blendFuncListener.run();
		}
	}

	@Inject(method = "disableAlphaTest", at = @At("HEAD"), cancellable = true)
	private static void iris$alphaTestDisableLock(CallbackInfo ci) {
		if (AlphaTestStorage.isAlphaTestLocked()) {
			AlphaTestStorage.deferAlphaTestToggle(false);
			ci.cancel();
		}
	}

	@Inject(method = "enableAlphaTest", at = @At("HEAD"), cancellable = true)
	private static void iris$alphaTestEnableLock(CallbackInfo ci) {
		if (AlphaTestStorage.isAlphaTestLocked()) {
			AlphaTestStorage.deferAlphaTestToggle(true);
			ci.cancel();
		}
	}

	@Inject(method = "alphaFunc", at = @At("HEAD"), cancellable = true)
	private static void iris$alphaFuncLock(int function, float reference, CallbackInfo ci) {
		if (AlphaTestStorage.isAlphaTestLocked()) {
			AlphaTestStorage.deferAlphaFunc(function, reference);
			ci.cancel();
		}
	}

	@Inject(method = "depthMask", at = @At("HEAD"), cancellable = true)
	private static void iris$depthMaskLock(boolean enable, CallbackInfo ci) {
		if (DepthColorStorage.isDepthColorLocked()) {
			DepthColorStorage.deferDepthEnable(enable);
			ci.cancel();
		}
	}

	@Inject(method = "colorMask", at = @At("HEAD"), cancellable = true)
	private static void iris$colorMaskLock(boolean red, boolean green, boolean blue, boolean alpha, CallbackInfo ci) {
		if (DepthColorStorage.isDepthColorLocked()) {
			DepthColorStorage.deferColorMask(red, green, blue, alpha);
			ci.cancel();
		}
	}
}
