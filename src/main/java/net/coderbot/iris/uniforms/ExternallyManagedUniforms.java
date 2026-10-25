package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformType;

public class ExternallyManagedUniforms {
	private ExternallyManagedUniforms() {
		// no construction allowed
	}

	public static void addExternallyManagedUniforms(UniformHolder uniformHolder) {
		// Celeritas terrain
		addMat4(uniformHolder, "iris_ModelViewMatrix");
		addMat4(uniformHolder, "iris_ModelViewMatrixInverse");
		addMat4(uniformHolder, "iris_ProjectionMatrix");
		addMat4(uniformHolder, "iris_ProjectionMatrixInverse");
		uniformHolder.externallyManagedUniform("iris_NormalMatrix", UniformType.MAT3);
		uniformHolder.externallyManagedUniform("u_RegionOffset", UniformType.VEC3);
		addFloat(uniformHolder, "iris_currentAlphaTest");

		// argentum instanced entities, see InstancedEntityTransformer
		for (String name : new String[] { "uTexture", "uLightmap", "uBoxInstancing", "uGlintPass", "uChargePass", "uItemGlintPass", "uAlphaPass", "uEyeLight", "uLit", "uLightmapped", "u_FogShape" }) {
			uniformHolder.externallyManagedUniform(name, UniformType.INT);
		}

		addMat4(uniformHolder, "uItemGlintMatrix");
		addMat4(uniformHolder, "uItemGlintMatrix1");
		uniformHolder.externallyManagedUniform("uLightDirection0", UniformType.VEC3);
		uniformHolder.externallyManagedUniform("uLightDirection1", UniformType.VEC3);
		addVec4(uniformHolder, "u_FogColor");
		addFloat(uniformHolder, "u_FogDensity");
		addFloat(uniformHolder, "u_FogStart");
		addFloat(uniformHolder, "u_FogEnd");
	}

	private static void addMat4(UniformHolder uniformHolder, String name) {
		uniformHolder.externallyManagedUniform(name, UniformType.MAT4);
	}

	private static void addVec4(UniformHolder uniformHolder, String name) {
		uniformHolder.externallyManagedUniform(name, UniformType.VEC4);
	}

	private static void addFloat(UniformHolder uniformHolder, String name) {
		uniformHolder.externallyManagedUniform(name, UniformType.FLOAT);
	}
}
