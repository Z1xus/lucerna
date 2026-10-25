package us.z1x.lucerna.uniforms.custom.cached;

import us.z1x.lucerna.gl.IrisRenderSystem;
import us.z1x.lucerna.gl.uniform.UniformUpdateFrequency;
import us.z1x.lucerna.parsing.MatrixType;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.function.Supplier;

public class Float4MatrixCachedUniform extends VectorCachedUniform<Matrix4f> {
    final private FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

    public Float4MatrixCachedUniform(String name, UniformUpdateFrequency updateFrequency, Supplier<Matrix4f> supplier) {
        super(name, updateFrequency, new Matrix4f(), supplier);
    }

    @Override
    protected void setFrom(Matrix4f other) {
        this.cached.set(other);
    }

    @Override
    public void push(int location) {
        // `gets` the values from the matrix and put's them into a buffer
        this.cached.get(buffer);
        IrisRenderSystem.uniformMatrix4fv(location, false, buffer);
    }

    @Override
    public MatrixType<Matrix4f> getType() {
        return MatrixType.MAT4;
    }
}
