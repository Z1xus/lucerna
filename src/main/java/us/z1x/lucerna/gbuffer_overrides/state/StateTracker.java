package us.z1x.lucerna.gbuffer_overrides.state;

import us.z1x.lucerna.gbuffer_overrides.matching.InputAvailability;
import us.z1x.lucerna.samplers.IrisSamplers;
import net.minecraft.client.render.platform.GlStateManager;

public class StateTracker {
    public static final StateTracker INSTANCE = new StateTracker();

    public boolean albedoSampler;
    public boolean lightmapSampler;
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
