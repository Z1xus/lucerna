package net.coderbot.iris.gbuffer_overrides.state;

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
}
