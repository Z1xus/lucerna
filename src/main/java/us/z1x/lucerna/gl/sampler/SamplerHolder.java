package us.z1x.lucerna.gl.sampler;

import us.z1x.lucerna.gl.state.ValueUpdateNotifier;

import java.util.function.IntSupplier;

public interface SamplerHolder {
    void addExternalSampler(int textureUnit, String... names);
    boolean hasSampler(String name);

    // unbound samplers sample from this one, call it before any addDynamicSampler since it needs unit 0
    boolean addDefaultSampler(IntSupplier sampler, String... names);
    boolean addDynamicSampler(IntSupplier sampler, String... names);
    boolean addDynamicSampler(IntSupplier sampler, ValueUpdateNotifier notifier, String... names);
}
