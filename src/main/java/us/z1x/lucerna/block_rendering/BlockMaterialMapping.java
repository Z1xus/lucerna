package us.z1x.lucerna.block_rendering;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import us.z1x.lucerna.Iris;
import us.z1x.lucerna.shaderpack.materialmap.BlockEntry;
import us.z1x.lucerna.shaderpack.materialmap.BlockRenderType;
import us.z1x.lucerna.shaderpack.materialmap.NamespacedId;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.property.Property;
import net.minecraft.client.render.block.BlockLayer;
import net.minecraft.resource.Identifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockMaterialMapping {
	public static Object2IntMap<BlockState> createBlockStateIdMap(Int2ObjectMap<List<BlockEntry>> blockPropertiesMap) {
		Object2IntMap<BlockState> blockStateIds = new Object2IntOpenHashMap<>();
		blockStateIds.defaultReturnValue(-1);

		blockPropertiesMap.forEach((intId, entries) -> {
			for (BlockEntry entry : entries) {
				addBlockStates(entry, blockStateIds, intId);
			}
		});

		return blockStateIds;
	}

	public static Map<Block, BlockLayer> createBlockTypeMap(Map<NamespacedId, BlockRenderType> blockPropertiesMap) {
		Map<Block, BlockLayer> blockTypeIds = new Reference2ReferenceOpenHashMap<>();

		blockPropertiesMap.forEach((id, blockType) -> {
			Block block = lookupBlock(id);

			if (block != Blocks.AIR) {
				blockTypeIds.put(block, convertBlockToRenderType(blockType));
			}
		});

		return blockTypeIds;
	}

	private static Block lookupBlock(NamespacedId id) {
		String name = id.getName();

		if (!name.isEmpty() && name.chars().allMatch(Character::isDigit)) {
			Block block = Block.byId(Integer.parseInt(name));
			return block == null ? Blocks.AIR : block;
		}

		return Block.REGISTRY.get(new Identifier(id.getNamespace(), name));
	}

	private static BlockLayer convertBlockToRenderType(BlockRenderType type) {
		if (type == null) {
			return null;
		}

		switch (type) {
			case SOLID: return BlockLayer.SOLID;
			case CUTOUT: return BlockLayer.CUTOUT;
			case CUTOUT_MIPPED: return BlockLayer.CUTOUT_MIPPED;
			case TRANSLUCENT: return BlockLayer.TRANSLUCENT;
			default: return null;
		}
	}

	private static void addBlockStates(BlockEntry entry, Object2IntMap<BlockState> idMap, int intId) {
		Block block = lookupBlock(entry.getId());

		// If the block doesn't exist, by default the registry will return AIR. That probably isn't what we want.
		if (block == Blocks.AIR) {
			return;
		}

		Map<String, String> propertyPredicates = new HashMap<>(entry.getPropertyPredicates());
		String legacyMetadata = propertyPredicates.remove(BlockEntry.LEGACY_METADATA);
		int[] metadata = legacyMetadata == null ? null : Arrays.stream(legacyMetadata.split(",")).filter(part -> !part.isEmpty()).mapToInt(Integer::parseInt).toArray();

		// We first collect each key=value pair in order to determine what properties we need to filter on.
		// We convert the keys to `Property`s to ensure they exist and to avoid string comparisons later.
		Map<Property<?>, String> properties = new HashMap<>();

		propertyPredicates.forEach((key, value) -> {
			Property<?> property = findProperty(block, key);

			if (property == null) {
				Iris.logger.warn("Error while parsing the block ID map entry for \"" + "block." + intId + "\":");
				Iris.logger.warn("- The block " + entry.getId() + " has no property with the name " + key + ", ignoring!");

				return;
			}

			properties.put(property, value);
		});

		// Once we have a list of properties and their expected values, we iterate over every possible state of this
		// block and check for ones that match the filters. This isn't particularly efficient, but it works!
		for (BlockState state : block.stateDefinition().all()) {
			if (checkState(state, properties) && checkMetadata(block, state, metadata)) {
				// NB: Using putIfAbsent means that the first successful mapping takes precedence
				//     Needed for OptiFine parity:
				//     https://github.com/IrisShaders/Iris/issues/1327
				idMap.putIfAbsent(state, intId);
			}
		}
	}

	private static Property<?> findProperty(Block block, String name) {
		for (Property<?> property : block.stateDefinition().properties()) {
			if (property.getName().equals(name)) {
				return property;
			}
		}

		return null;
	}

	private static boolean checkMetadata(Block block, BlockState state, int[] metadata) {
		if (metadata == null || metadata.length == 0) {
			return true;
		}

		int stateMetadata = block.getMetadataFromState(state);

		for (int value : metadata) {
			if (value == stateMetadata) {
				return true;
			}
		}

		return false;
	}

	// We ignore generics here, the actual types don't matter because we just convert
	// them to strings anyways, and the compiler checks just get in the way.
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static boolean checkState(BlockState state, Map<Property<?>, String> expectedValues) {
		for (Map.Entry<Property<?>, String> condition : expectedValues.entrySet()) {
			Property property = condition.getKey();
			String expectedValue = condition.getValue();

			String actualValue = property.getName(state.get(property));

			if (!expectedValue.equals(actualValue)) {
				return false;
			}
		}

		return true;
	}
}
