package us.z1x.lucerna.parsing;

import us.z1x.lucerna.uniforms.SystemTimeUniforms;

// basic exponential smoothing, see https://en.wikipedia.org/wiki/Exponential_smoothing
public class SmoothFloat {
    private static final double LN_OF_2 = Math.log(2.0);

    private float accumulator;

    private boolean hasInitialValue;
    private float cachedHalfLifeUp;
    private float cachedDecayUp;
    private float cachedHalfLifeDown;
    private float cachedDecayDown;

    private static float exponentialDecayFactor(float k, float t) {
        return (float) Math.exp(-k * t);
    }

    private static float lerp(float v0, float v1, float t) {
        return (1 - t) * v0 + t * v1;
    }

    public float updateAndGet(float value, float halfLifeUp, float halfLifeDown) {
        if (halfLifeUp != cachedHalfLifeUp) {
            cachedHalfLifeUp = halfLifeUp;
            if (halfLifeUp == 0.0f) {
                cachedDecayUp = 0;
            } else {
                cachedDecayUp = computeDecay(halfLifeUp * 0.1F);
            }
        }

        if (halfLifeDown != cachedHalfLifeDown) {
            cachedHalfLifeDown = halfLifeDown;
            if (halfLifeDown == 0.0f) {
                cachedDecayDown = 0;
            } else {
                cachedDecayDown = computeDecay(halfLifeDown * 0.1F);
            }
        }

        if (!hasInitialValue) {
            // the first value is not smoothed
            accumulator = value;
            hasInitialValue = true;

            return accumulator;
        }

        float lastFrameTime = SystemTimeUniforms.TIMER.getLastFrameTime();

        float decay = value > this.accumulator ? cachedDecayUp : cachedDecayDown;

        if (decay == 0.0f) {
            accumulator = value;
            return accumulator;
        }

        float smoothingFactor = 1.0f - exponentialDecayFactor(decay, lastFrameTime);

        accumulator = lerp(accumulator, value, smoothingFactor);

        return accumulator;
    }

    private float computeDecay(float halfLife) {
        return (float) (1.0f / (halfLife / LN_OF_2));
    }
}
