package us.z1x.lucerna.uniforms;

import us.z1x.lucerna.gl.uniform.UniformHolder;
import us.z1x.lucerna.gui.option.IrisVideoSettings;
import net.minecraft.client.Minecraft;

import static us.z1x.lucerna.gl.uniform.UniformUpdateFrequency.PER_FRAME;

/**
 * Implements uniforms relating the current viewport
 *
 * @see <a href="https://github.com/IrisShaders/ShaderDoc/blob/master/uniforms.md#viewport">Uniforms: Viewport</a>
 */
public final class ViewportUniforms {
    // cannot be constructed
    private ViewportUniforms() {
    }

    /**
     * Makes the viewport uniforms available to the given program
     *
     * @param uniforms the program to make the uniforms available to
     */
    public static void addViewportUniforms(UniformHolder uniforms) {
        uniforms
            .uniform1f(PER_FRAME, "viewHeight", () -> IrisVideoSettings.scaleRenderSize(Minecraft.getInstance().getRenderTarget().height))
            .uniform1f(PER_FRAME, "viewWidth", () -> IrisVideoSettings.scaleRenderSize(Minecraft.getInstance().getRenderTarget().width))
            .uniform1f(PER_FRAME, "aspectRatio", ViewportUniforms::getAspectRatio);
    }

    /**
     * @return the current viewport aspect ratio, calculated from the current Minecraft window size
     */
    private static float getAspectRatio() {
        return ((float) Minecraft.getInstance().width) / ((float) Minecraft.getInstance().height);
    }
}
