package us.z1x.lucerna.layer;

import us.z1x.lucerna.Lucerna;
import us.z1x.lucerna.gbufferoverrides.matching.SpecialCondition;
import us.z1x.lucerna.gl.state.StateUpdateNotifiers;
import us.z1x.lucerna.pipeline.WorldRenderingPhase;
import us.z1x.lucerna.pipeline.WorldRenderingPipeline;

public class GbufferPrograms {
    private static boolean entities;
    private static boolean blockEntities;
    private static boolean outline;
    private static Runnable phaseChangeListener;

    public static WorldRenderingPhase getCurrentPhase() {
        WorldRenderingPipeline pipeline = Lucerna.getPipelineManager().getPipelineNullable();

        if (pipeline != null) {
            return pipeline.getPhase();
        } else {
            return WorldRenderingPhase.NONE;
        }
    }

    private static void setPhase(WorldRenderingPhase phase) {
        WorldRenderingPipeline pipeline = Lucerna.getPipelineManager().getPipelineNullable();

        if (pipeline != null) {
            pipeline.setPhase(phase);
        }
    }

    public static void setOverridePhase(WorldRenderingPhase phase) {
        WorldRenderingPipeline pipeline = Lucerna.getPipelineManager().getPipelineNullable();

        if (pipeline != null) {
            pipeline.setOverridePhase(phase);
        }
    }

    public static void runPhaseChangeNotifier() {
        if (phaseChangeListener != null) {
            phaseChangeListener.run();
        }
    }

    public static void setupSpecialRenderCondition(SpecialCondition override) {
        Lucerna.getPipelineManager().getPipeline().ifPresent(p -> p.setSpecialCondition(override));
    }

    public static void teardownSpecialRenderCondition(SpecialCondition override) {
        Lucerna.getPipelineManager().getPipeline().ifPresent(p -> p.setSpecialCondition(null));
    }

    static {
        StateUpdateNotifiers.phaseChangeNotifier = listener -> phaseChangeListener = listener;
    }

    public static void init() {
        // empty, calling it runs the static initializer
    }
}
