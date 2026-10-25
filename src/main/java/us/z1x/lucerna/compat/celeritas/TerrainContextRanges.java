package us.z1x.lucerna.compat.celeritas;

import net.minecraft.client.render.block.BlockLayer;
import net.minecraft.util.math.BlockPos;

// argentum converts its vanilla layer buffers to chunk vertices only after the whole section is meshed
public interface TerrainContextRanges {
	void lucerna$recordBlock(BlockLayer layer, int packedId, BlockPos pos);
}
