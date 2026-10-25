package net.coderbot.iris.gbuffer_overrides.state;

import net.coderbot.iris.samplers.IrisSamplers;
import net.minecraft.client.render.platform.GlStateManager;

import net.coderbot.iris.gbuffer_overrides.matching.InputAvailability;

public class StateTracker {
	public static final StateTracker INSTANCE = new StateTracker();

	// All textures are disabled by default

	// TextureStateShard / TextureUnit.TERRAIN
	public boolean albedoSampler;
	// LightmapStateShard / TextureUnit.LIGHTMAP
	public boolean lightmapSampler;
	// OverlayStateShard / TextureUnit.OVERLAY
	public boolean overlaySampler;

	private static final InputAvailability[] INPUTS = new InputAvailability[InputAvailability.NUM_VALUES];

	static {
		for (int i = 0; i < INPUTS.length; i++) {
			INPUTS[i] = InputAvailability.unpack(i);
		}
	}

	public InputAvailability getInputs() {
		return INPUTS[(albedoSampler ? 1 : 0) | (lightmapSampler ? 2 : 0) | (overlaySampler ? 4 : 0)];
	}

	// vanilla may have enabled texturing before the pipeline started listening, so read the cached GL state
	public void syncWithGlState() {
		albedoSampler = GlStateManager.TEXTURES[IrisSamplers.ALBEDO_TEXTURE_UNIT].state.enabled;
		lightmapSampler = GlStateManager.TEXTURES[IrisSamplers.LIGHTMAP_TEXTURE_UNIT].state.enabled;
		overlaySampler = false;
	}
}
