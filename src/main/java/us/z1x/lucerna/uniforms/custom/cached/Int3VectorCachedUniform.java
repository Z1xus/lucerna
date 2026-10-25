package us.z1x.lucerna.uniforms.custom.cached;

import kroppeb.stareval.function.FunctionReturn;
import us.z1x.lucerna.gl.uniform.UniformUpdateFrequency;
import us.z1x.lucerna.parsing.VectorType;
import org.joml.Vector3i;
import us.z1x.lucerna.gl.IrisRenderSystem;

import java.util.function.Supplier;

public class Int3VectorCachedUniform extends VectorCachedUniform<Vector3i> {

    public Int3VectorCachedUniform(String name, UniformUpdateFrequency updateFrequency, Supplier<Vector3i> supplier) {
        super(name, updateFrequency, new Vector3i(), supplier);
    }

    @Override
    protected void setFrom(Vector3i other) {
        this.cached.set(other);
    }

    @Override
    public void push(int location) {
        IrisRenderSystem.uniform3i(location, this.cached.x, this.cached.y, this.cached.z);
    }

    @Override
    public void writeTo(FunctionReturn functionReturn) {
        functionReturn.objectReturn = this.cached;
    }

    @Override
    public VectorType getType() {
        return VectorType.I_VEC3;
    }
}
