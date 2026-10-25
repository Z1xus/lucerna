package us.z1x.lucerna.uniforms;

import us.z1x.lucerna.gl.uniform.UniformHolder;
import us.z1x.lucerna.gl.uniform.UniformUpdateFrequency;

import java.util.OptionalLong;
import java.util.function.IntSupplier;

public final class SystemTimeUniforms {
    public static final Timer TIMER = new Timer();
    public static final FrameCounter COUNTER = new FrameCounter();

    private SystemTimeUniforms() {
    }

    public static void addSystemTimeUniforms(UniformHolder uniforms) {
        uniforms
            .uniform1i(UniformUpdateFrequency.PER_FRAME, "frameCounter", COUNTER)
            .uniform1f(UniformUpdateFrequency.PER_FRAME, "frameTime", TIMER::getLastFrameTime)
            .uniform1f(UniformUpdateFrequency.PER_FRAME, "frameTimeCounter", TIMER::getFrameTimeCounter);
    }

    public static class FrameCounter implements IntSupplier {
        private int count;

        private FrameCounter() {
            this.count = 0;
        }

        @Override
        public int getAsInt() {
            return count;
        }

        public void beginFrame() {
            count = (count + 1) % 720720;
        }

        public void reset() {
            count = 0;
        }
    }

    public static final class Timer {
        private float frameTimeCounter;
        private float lastFrameTime;

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private OptionalLong lastStartTime;

        public Timer() {
            reset();
        }

        public void beginFrame(long frameStartTime) {
            long diffNs = frameStartTime - lastStartTime.orElse(frameStartTime);
            long diffMs = (diffNs / 1000) / 1000;

            lastFrameTime = diffMs / 1000.0F;

            frameTimeCounter += lastFrameTime;

            // large values break some shader packs
            if (frameTimeCounter >= 3600.0F) {
                frameTimeCounter = 0.0F;
            }

            lastStartTime = OptionalLong.of(frameStartTime);
        }

        public float getFrameTimeCounter() {
            return frameTimeCounter;
        }

        public float getLastFrameTime() {
            return lastFrameTime;
        }

        public void reset() {
            frameTimeCounter = 0.0F;
            lastFrameTime = 0.0F;
            lastStartTime = OptionalLong.empty();
        }
    }
}
