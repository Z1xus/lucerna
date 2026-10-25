package net.coderbot.iris.vertices;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.VertexFormat;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

/**
 * Supplies mc_midTexCoord and at_tangent for vanilla quad draws, which have no room for them in their vertex format,
 * as client side vertex attribute arrays.
 */
public final class VanillaVertexAttributes {
	// mc_midTexCoord as two floats, then at_tangent as four normalized bytes
	private static final int STRIDE = 12;
	private static final BufferBuilderQuadView QUAD = new BufferBuilderQuadView();
	private static final Vector3f NORMAL = new Vector3f();

	private static ByteBuffer buffer = MemoryUtil.memAlloc(STRIDE * 4096);
	private static boolean bound;

	private VanillaVertexAttributes() {
	}

	public static void bind(BufferBuilder builder) {
		VertexFormat format = builder.getFormat();

		if (builder.getDrawMode() != GL11C.GL_QUADS || builder.getVertexCount() < 4 || !format.hasUv(0)) {
			return;
		}

		int stride = format.getVertexSize();
		int uvOffset = format.getUvOffset(0);
		int vertices = builder.getVertexCount() & ~3;

		if (buffer.capacity() < vertices * STRIDE) {
			buffer = MemoryUtil.memRealloc(buffer, vertices * STRIDE * 2);
		}

		long source = MemoryUtil.memAddress(builder.getBuffer(), 0);
		long target = MemoryUtil.memAddress(buffer, 0);

		for (int quad = 0; quad < vertices; quad += 4) {
			long address = source + (long) quad * stride;
			QUAD.setup(address, stride, uvOffset);
			NormalHelper.computeFaceNormal(NORMAL, QUAD);
			int tangent = NormalHelper.computeTangent(NORMAL.x, NORMAL.y, NORMAL.z, QUAD);
			float midU = (QUAD.u(0) + QUAD.u(1) + QUAD.u(2) + QUAD.u(3)) * 0.25F;
			float midV = (QUAD.v(0) + QUAD.v(1) + QUAD.v(2) + QUAD.v(3)) * 0.25F;

			for (int vertex = 0; vertex < 4; vertex++) {
				long pointer = target + (long) (quad + vertex) * STRIDE;
				MemoryUtil.memPutFloat(pointer, midU);
				MemoryUtil.memPutFloat(pointer + 4, midV);
				MemoryUtil.memPutInt(pointer + 8, tangent);
			}
		}

		GL20C.nglVertexAttribPointer(12, 2, GL11C.GL_FLOAT, false, STRIDE, target);
		GL20C.nglVertexAttribPointer(13, 4, GL11C.GL_BYTE, true, STRIDE, target + 8);
		GL20C.glEnableVertexAttribArray(12);
		GL20C.glEnableVertexAttribArray(13);
		bound = true;
	}

	public static void unbind() {
		if (!bound) {
			return;
		}

		bound = false;
		GL20C.glDisableVertexAttribArray(12);
		GL20C.glDisableVertexAttribArray(13);
		// the current value of an attribute is undefined after an array draw
		IrisRenderSystem.vertexAttrib4f(12, 0.0F, 0.0F, 0.0F, 1.0F);
		IrisRenderSystem.vertexAttrib4f(13, 1.0F, 0.0F, 0.0F, 1.0F);
	}
}
