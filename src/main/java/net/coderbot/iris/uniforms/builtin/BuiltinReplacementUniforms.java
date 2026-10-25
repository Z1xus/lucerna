package net.coderbot.iris.uniforms.builtin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import org.joml.Matrix4f;

public class BuiltinReplacementUniforms {
	// This mimics the transformations done in GameRenderer to the GL_TEXTURE matrix of the lightmap unit.
	private static final Matrix4f lightmapTextureMatrix = new Matrix4f().scale(0.00390625f).translate(8.0f, 8.0f, 8.0f);

	public static void addBuiltinReplacementUniforms(UniformHolder uniforms) {
		uniforms.uniformMatrix(UniformUpdateFrequency.ONCE, "iris_LightmapTextureMatrix", () -> {
			Iris.logger.warn("A shader appears to require the lightmap texture matrix even after transformations have occurred");
			Iris.logger.warn("Iris handles this correctly but it indicates that the shader is doing weird things with lightmap coordinates");

			return lightmapTextureMatrix;
		});
	}
}
