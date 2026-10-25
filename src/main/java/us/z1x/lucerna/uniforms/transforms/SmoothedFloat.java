package us.z1x.lucerna.uniforms.transforms;

import us.z1x.lucerna.gl.uniform.FloatSupplier;
import us.z1x.lucerna.uniforms.FrameUpdateNotifier;
import us.z1x.lucerna.uniforms.SystemTimeUniforms;

public class SmoothedFloat implements FloatSupplier {
    private static final double LN_OF_2 = Math.log(2.0);

    private final FloatSupplier unsmoothed;

    private float accumulator;

    private boolean hasInitialValue;

    private final float decayConstantUp;

    private final float decayConstantDown;

    public SmoothedFloat(float halfLifeUp, float halfLifeDown, FloatSupplier unsmoothed, FrameUpdateNotifier updateNotifier) {
        // half life is in tenths of a second (2 ticks)
        this.decayConstantUp = computeDecay(halfLifeUp * 0.1F);
        this.decayConstantDown = computeDecay(halfLifeDown * 0.1F);

        this.unsmoothed = unsmoothed;

        updateNotifier.addListener(this::update);
    }

    private void update() {
        if (!hasInitialValue) {
            accumulator = unsmoothed.getAsFloat();
            hasInitialValue = true;

            return;
        }

        float newValue = unsmoothed.getAsFloat();

        float lastFrameTime = SystemTimeUniforms.TIMER.getLastFrameTime();

        float smoothingFactor = 1.0f - exponentialDecayFactor(newValue > this.accumulator ? this.decayConstantUp : decayConstantDown, lastFrameTime);

        accumulator = lerp(accumulator, newValue, smoothingFactor);
    }

    private float computeDecay(float halfLife) {
        return (float) (1.0f / (halfLife / LN_OF_2));
    }

    @Override
    public float getAsFloat() {
        if (!hasInitialValue) {
            return unsmoothed.getAsFloat();
        }

        return accumulator;
    }

    private static float exponentialDecayFactor(float k, float t) {
        return (float) Math.exp(-k * t);
    }

    private static float lerp(float v0, float v1, float t) {
        return (1 - t) * v0 + t * v1;
    }
}
