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
