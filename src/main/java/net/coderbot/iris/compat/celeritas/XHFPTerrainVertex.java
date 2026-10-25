package net.coderbot.iris.compat.celeritas;

import net.coderbot.iris.vertices.ExtendedDataHelper;
import net.coderbot.iris.vertices.NormI8;
import net.coderbot.iris.vertices.NormalHelper;
import org.embeddedt.embeddium.impl.render.chunk.terrain.material.Material;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexEncoder;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import static net.coderbot.iris.compat.celeritas.XHFPModelVertexType.STRIDE;

public class XHFPTerrainVertex implements ChunkVertexEncoder {
	private static final int MID_TEX_OFFSET = XHFPModelVertexType.VERTEX_FORMAT.getAttribute("mc_midTexCoord").getPointer();
	private static final int MC_ENTITY_OFFSET = XHFPModelVertexType.VERTEX_FORMAT.getAttribute("mc_Entity").getPointer();
	private static final int MID_BLOCK_OFFSET = XHFPModelVertexType.VERTEX_FORMAT.getAttribute("at_midBlock").getPointer();
	private static final int NORMAL_OFFSET = XHFPModelVertexType.VERTEX_FORMAT.getAttribute("iris_Normal").getPointer();
	private static final int TANGENT_OFFSET = XHFPModelVertexType.VERTEX_FORMAT.getAttribute("at_tangent").getPointer();

	private final QuadViewTerrain.QuadViewTerrainUnsafe quad = new QuadViewTerrain.QuadViewTerrainUnsafe();
	private final Vector3f normal = new Vector3f();
	private final ChunkVertexEncoder baseEncoder = XHFPModelVertexType.BASE_VERTEX_TYPE.createEncoder();

	private TerrainBlockContext context;
	private int vertexCount;
	private float uSum;
	private float vSum;

	@Override
	public boolean supportsBilinearCorrection() {
		return false;
	}

	@Override
	public long write(long ptr, Material material, Vertex vertex, int chunkId) {
		if (vertexCount == 0) {
			context = TerrainBlockContext.get();
		}

		uSum += vertex.u;
		vSum += vertex.v;
		vertexCount++;

		vertex.rdhFactor = 0;
		this.baseEncoder.write(ptr, material, vertex, chunkId);

		MemoryUtil.memPutShort(ptr + MC_ENTITY_OFFSET + 2, context.renderType);
		MemoryUtil.memPutInt(ptr + MID_BLOCK_OFFSET, ExtendedDataHelper.computeMidBlock(vertex.x, vertex.y, vertex.z, context.localPosX, context.localPosY, context.localPosZ));
		MemoryUtil.memPutByte(ptr + MID_BLOCK_OFFSET + 3, context.lightValue);

		if (vertexCount == 4) {
			vertexCount = 0;

			int midUV = XHFPModelVertexType.encodeTexture(uSum * 0.25f, vSum * 0.25f);
			short blockId = context.blockId;

			uSum = 0;
			vSum = 0;

			// normal computation
			// Implementation based on the algorithm found here:
			// https://github.com/IrisShaders/ShaderDoc/blob/master/vertex-format-extensions.md#surface-normal-vector
			quad.setup(ptr, STRIDE);
			NormalHelper.computeFaceNormal(normal, quad);
			int packedNormal = NormI8.pack(normal);
			int tangent = NormalHelper.computeTangent(normal.x, normal.y, normal.z, quad);

			for (int i = 0; i < 4; i++) {
				long vertexPtr = ptr - (long) STRIDE * i;
				MemoryUtil.memPutInt(vertexPtr + MID_TEX_OFFSET, midUV);
				MemoryUtil.memPutShort(vertexPtr + MC_ENTITY_OFFSET, blockId);
				MemoryUtil.memPutInt(vertexPtr + NORMAL_OFFSET, packedNormal);
				MemoryUtil.memPutInt(vertexPtr + TANGENT_OFFSET, tangent);
			}
		}

		return ptr + STRIDE;
	}
}
