package net.coderbot.iris.compat.celeritas;

import net.minecraft.client.render.block.BlockLayer;
import net.minecraft.util.math.BlockPos;

/**
 * Records which block emitted each vertex range of Argentum's per-layer vanilla buffers, since those are converted to
 * chunk vertices only after the whole section is meshed.
 */
public interface TerrainContextRanges {
	void lucerna$recordBlock(BlockLayer layer, int packedId, BlockPos pos);
}
