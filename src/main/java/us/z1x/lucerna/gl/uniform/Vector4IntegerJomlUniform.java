package us.z1x.lucerna.gl.uniform;

import us.z1x.lucerna.gl.LucernaRenderSystem;
import us.z1x.lucerna.gl.state.ValueUpdateNotifier;
import org.joml.Vector4i;

import java.util.function.Supplier;

public class Vector4IntegerJomlUniform extends Uniform {
    private Vector4i cachedValue;
    private final Supplier<Vector4i> value;

    Vector4IntegerJomlUniform(int location, Supplier<Vector4i> value) {
        this(location, value, null);
    }

    Vector4IntegerJomlUniform(int location, Supplier<Vector4i> value, ValueUpdateNotifier notifier) {
        super(location, notifier);

        this.cachedValue = null;
        this.value = value;
    }

    @Override
    public void update() {
        updateValue();

        if (notifier != null) {
            notifier.setListener(this::updateValue);
        }
    }

    private void updateValue() {
        Vector4i newValue = value.get();

        if (cachedValue == null || !newValue.equals(cachedValue)) {
            cachedValue = newValue;
            LucernaRenderSystem.uniform4i(this.location, newValue.x, newValue.y, newValue.z, newValue.w);
        }
    }
}
