package net.coderbot.iris.compat.celeritas;

import org.embeddedt.embeddium.impl.gl.attribute.GlVertexAttributeFormat;
import org.embeddedt.embeddium.impl.gl.attribute.GlVertexFormat;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkMeshFormats;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexEncoder;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;

/**
 * The vanilla-like Celeritas vertex format, extended with the attributes that shader packs read.
 */
public class XHFPModelVertexType implements ChunkVertexType {
	public static final ChunkVertexType BASE_VERTEX_TYPE = ChunkMeshFormats.VANILLA_LIKE;

	public static final XHFPModelVertexType INSTANCE = new XHFPModelVertexType();

	public static final int STRIDE = 48;
	public static final GlVertexFormat VERTEX_FORMAT = GlVertexFormat.builder(STRIDE)
		// Exclude the rdh factor as shaders cannot use it
		.addElements(BASE_VERTEX_TYPE.getVertexFormat().getAttributes().stream().filter(a -> !a.getName().equals("a_RdhFactor")).toList())
		.addElement("mc_midTexCoord", GlVertexFormat.NEXT_ALIGNED_POINTER, GlVertexAttributeFormat.UNSIGNED_SHORT, 2, true, false)
		.addElement("at_tangent", GlVertexFormat.NEXT_ALIGNED_POINTER, GlVertexAttributeFormat.BYTE, 4, true, false)
		.addElement("iris_Normal", GlVertexFormat.NEXT_ALIGNED_POINTER, GlVertexAttributeFormat.BYTE, 3, true, false)
		.addElement("mc_Entity", GlVertexFormat.NEXT_ALIGNED_POINTER, GlVertexAttributeFormat.SHORT, 2, false, false)
		.addElement("at_midBlock", GlVertexFormat.NEXT_ALIGNED_POINTER, GlVertexAttributeFormat.BYTE, 4, false, false)
		.build();

	// the mid texture coordinate is a normalized unsigned short, so the shader reads it in [0, 1]
	public static int encodeTexture(float u, float v) {
		return (Math.round(u * 65535.0F) & 0xFFFF) | ((Math.round(v * 65535.0F) & 0xFFFF) << 16);
	}

	@Override
	public float getTextureScale() {
		return 1f;
	}

	@Override
	public float getPositionScale() {
		return 1f;
	}

	@Override
	public float getPositionOffset() {
		return 0;
	}

	@Override
	public GlVertexFormat getVertexFormat() {
		return VERTEX_FORMAT;
	}

	@Override
	public ChunkVertexEncoder createEncoder() {
		return new XHFPTerrainVertex();
	}
}
