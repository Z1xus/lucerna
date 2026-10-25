package net.coderbot.iris.pipeline;

import net.minecraft.client.render.block.BlockLayer;

public enum WorldRenderingPhase {
	NONE,
	SKY,
	SUNSET,
	CUSTOM_SKY, // Unused, just here to match OptiFine ordinals
	SUN,
	MOON,
	STARS,
	VOID,
	TERRAIN_SOLID,
	TERRAIN_CUTOUT_MIPPED,
	TERRAIN_CUTOUT,
	ENTITIES,
	BLOCK_ENTITIES,
	DESTROY,
	OUTLINE,
	DEBUG,
	HAND_SOLID,
	TERRAIN_TRANSLUCENT,
	TRIPWIRE,
	PARTICLES,
	CLOUDS,
	RAIN_SNOW,
	WORLD_BORDER,
	HAND_TRANSLUCENT;

	public static WorldRenderingPhase fromTerrainRenderType(BlockLayer layer) {
		switch (layer) {
			case SOLID:
				return WorldRenderingPhase.TERRAIN_SOLID;
			case CUTOUT:
				return WorldRenderingPhase.TERRAIN_CUTOUT;
			case CUTOUT_MIPPED:
				return WorldRenderingPhase.TERRAIN_CUTOUT_MIPPED;
			case TRANSLUCENT:
				return WorldRenderingPhase.TERRAIN_TRANSLUCENT;
			default:
				throw new IllegalStateException("Illegal block layer!");
		}
	}
}
