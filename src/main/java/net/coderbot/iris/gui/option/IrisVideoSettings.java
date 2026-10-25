package net.coderbot.iris.gui.option;

import net.coderbot.iris.Iris;

public class IrisVideoSettings {
	public static int shadowDistance = 32;
	public static float renderQuality = 1.0F;
	public static float shadowQuality = 1.0F;

	public static int scaleRenderSize(int size) {
		return Math.max(1, Math.round(size * renderQuality));
	}

	public static int scaleShadowResolution(int resolution) {
		return Math.max(1, Math.round(resolution * shadowQuality));
	}

	public static int getOverriddenShadowDistance(int base) {
		return Iris.getPipelineManager().getPipeline()
				.map(pipeline -> pipeline.getForcedShadowRenderDistanceChunksForDisplay().orElse(base))
				.orElse(base);
	}

	public static boolean isShadowDistanceSliderEnabled() {
		return Iris.getPipelineManager().getPipeline()
				.map(pipeline -> !pipeline.getForcedShadowRenderDistanceChunksForDisplay().isPresent())
				.orElse(true);
	}
}
