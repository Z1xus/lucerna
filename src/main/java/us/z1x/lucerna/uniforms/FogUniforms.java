package us.z1x.lucerna.uniforms;

import us.z1x.lucerna.gl.state.StateUpdateNotifiers;
import us.z1x.lucerna.gl.uniform.DynamicUniformHolder;
import net.minecraft.client.render.platform.GlStateManager;

public class FogUniforms {
    private FogUniforms() {
        // no construction
    }

    public static void addFogUniforms(DynamicUniformHolder uniforms) {
        uniforms.uniform1i("fogMode", () -> {
            GlStateManager.Fog fog = GlStateManager.FOG;

            if (!fog.state.enabled) {
                return 0;
            }

            return GlStateManager.FOG.mode;
        }, listener -> {
            StateUpdateNotifiers.fogToggleNotifier.setListener(listener);
            StateUpdateNotifiers.fogModeNotifier.setListener(listener);
        });

        uniforms.uniform1f("fogDensity", () -> GlStateManager.FOG.density, listener -> {
            StateUpdateNotifiers.fogToggleNotifier.setListener(listener);
            StateUpdateNotifiers.fogDensityNotifier.setListener(listener);
        });

        uniforms.uniform1f("fogStart", () -> GlStateManager.FOG.start, listener -> {
            StateUpdateNotifiers.fogToggleNotifier.setListener(listener);
            StateUpdateNotifiers.fogStartNotifier.setListener(listener);
        });

        uniforms.uniform1f("fogEnd", () -> GlStateManager.FOG.end, listener -> {
            StateUpdateNotifiers.fogToggleNotifier.setListener(listener);
            StateUpdateNotifiers.fogEndNotifier.setListener(listener);
        });
    }
}
