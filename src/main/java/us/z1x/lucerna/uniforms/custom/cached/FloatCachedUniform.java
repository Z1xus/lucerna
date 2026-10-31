package us.z1x.lucerna.uniforms.custom.cached;

import kroppeb.stareval.function.FunctionReturn;
import kroppeb.stareval.function.Type;
import us.z1x.lucerna.gl.IrisRenderSystem;
import us.z1x.lucerna.gl.uniform.FloatSupplier;

public class FloatCachedUniform extends CachedUniform {

    final private FloatSupplier supplier;
    private float cached;

    public FloatCachedUniform(String name, FloatSupplier supplier) {
        super(name);
        this.supplier = supplier;
    }

    @Override
    protected boolean doUpdate() {
        float prev = this.cached;
        this.cached = this.supplier.getAsFloat();
        return prev != cached;
    }

    @Override
    public void push(int location) {
        IrisRenderSystem.uniform1f(location, this.cached);
    }

    @Override
    public void writeTo(FunctionReturn functionReturn) {
        functionReturn.floatReturn = this.cached;
    }

    @Override
    public Type getType() {
        return Type.Float;
    }
}
