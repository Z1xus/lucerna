package net.coderbot.iris.compat.celeritas;

import org.embeddedt.embeddium.impl.gl.attribute.GlVertexAttributeFormat;
import org.embeddedt.embeddium.impl.gl.attribute.GlVertexFormat;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexEncoder;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;

// every attribute is a float attribute, so the patched programs work on the glsl 1.20 that most 1.8.9 packs target
public class XHFPModelVertexType implements ChunkVertexType {
	public static final XHFPModelVertexType INSTANCE = new XHFPModelVertexType();

	public static final int STRIDE = 52;
	public static final GlVertexFormat VERTEX_FORMAT = GlVertexFormat.builder(STRIDE)
		.addElement("a_PosId", 0, GlVertexAttributeFormat.FLOAT, 3, false, false)
		.addElement("a_Color", 12, GlVertexAttributeFormat.UNSIGNED_BYTE, 4, true, false)
		.addElement("a_TexCoord", 16, GlVertexAttributeFormat.FLOAT, 2, false, false)
		.addElement("iris_LightCoord", 24, GlVertexAttributeFormat.UNSIGNED_SHORT, 2, false, false)
		.addElement("iris_DrawParams", 28, GlVertexAttributeFormat.UNSIGNED_BYTE, 4, false, false)
		.addElement("mc_midTexCoord", 32, GlVertexAttributeFormat.UNSIGNED_SHORT, 2, true, false)
		.addElement("at_tangent", 36, GlVertexAttributeFormat.BYTE, 4, true, false)
		.addElement("iris_Normal", 40, GlVertexAttributeFormat.BYTE, 3, true, false)
		.addElement("mc_Entity", 44, GlVertexAttributeFormat.SHORT, 2, false, false)
		.addElement("at_midBlock", 48, GlVertexAttributeFormat.BYTE, 4, false, false)
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
