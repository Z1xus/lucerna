package net.coderbot.iris.pipeline;

import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

// fancy clouds from a display list that is rebuilt only when the cell, color or visible faces change
public final class CloudRenderer {
	public static final CloudRenderer INSTANCE = new CloudRenderer();

	// one texel of the 256x256 cloud texture
	private static final float TEXEL = 0.00390625F;
	private static final float INSET = 9.765625E-4F;

	private int list;
	private int cellX;
	private int cellZ;
	private int red;
	private int green;
	private int blue;
	private boolean bottom;
	private boolean top;

	private CloudRenderer() {
	}

	public void render(double x, double z, float height, float red, float green, float blue, int anaglyphRenderPass) {
		int cellX = MathHelper.floor(x);
		int cellZ = MathHelper.floor(z);
		boolean bottom = height > -5.0F;
		boolean top = height <= 5.0F;
		int redBits = Float.floatToIntBits(red);
		int greenBits = Float.floatToIntBits(green);
		int blueBits = Float.floatToIntBits(blue);

		if (list == 0 || cellX != this.cellX || cellZ != this.cellZ || bottom != this.bottom || top != this.top
				|| redBits != this.red || greenBits != this.green || blueBits != this.blue) {
			this.cellX = cellX;
			this.cellZ = cellZ;
			this.bottom = bottom;
			this.top = top;
			this.red = redBits;
			this.green = greenBits;
			this.blue = blueBits;
			compile(cellX * TEXEL, cellZ * TEXEL, red, green, blue);
		}

		GlStateManager.scalef(12.0F, 1.0F, 12.0F);
		GlStateManager.translatef((float) (cellX - x), height, (float) (cellZ - z));

		GlStateManager.colorMask(false, false, false, false);
		GlStateManager.callList(list);

		switch (anaglyphRenderPass) {
			case 0:
				GlStateManager.colorMask(false, true, true, true);
				break;
			case 1:
				GlStateManager.colorMask(true, false, false, true);
				break;
			default:
				GlStateManager.colorMask(true, true, true, true);
				break;
		}

		GlStateManager.callList(list);
	}

	private void compile(float u, float v, float red, float green, float blue) {
		if (list == 0) {
			list = GL11.glGenLists(1);
		}

		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder builder = tesselator.getBuffer();

		CapturedRenderingState.INSTANCE.setCompilingDisplayList(true);
		GL11.glNewList(list, GL11.GL_COMPILE);
		builder.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);

		for (int cellX = -3; cellX <= 4; cellX++) {
			for (int cellZ = -3; cellZ <= 4; cellZ++) {
				float x = cellX * 8;
				float z = cellZ * 8;

				if (bottom) {
					face(builder, x, z, 0.0F, -1.0F, red * 0.7F, green * 0.7F, blue * 0.7F, u, v);
				}

				if (top) {
					face(builder, x, z, 4.0F - INSET, 1.0F, red, green, blue, u, v);
				}

				if (cellX > -1) {
					for (int i = 0; i < 8; i++) {
						sideX(builder, x, z, x + i, i + 0.5F, -1.0F, red * 0.9F, green * 0.9F, blue * 0.9F, u, v);
					}
				}

				if (cellX <= 1) {
					for (int i = 0; i < 8; i++) {
						sideX(builder, x, z, x + i + 1.0F - INSET, i + 0.5F, 1.0F, red * 0.9F, green * 0.9F, blue * 0.9F, u, v);
					}
				}

				if (cellZ > -1) {
					for (int i = 0; i < 8; i++) {
						sideZ(builder, x, z, z + i, i + 0.5F, -1.0F, red * 0.8F, green * 0.8F, blue * 0.8F, u, v);
					}
				}

				if (cellZ <= 1) {
					for (int i = 0; i < 8; i++) {
						sideZ(builder, x, z, z + i + 1.0F - INSET, i + 0.5F, 1.0F, red * 0.8F, green * 0.8F, blue * 0.8F, u, v);
					}
				}
			}
		}

		tesselator.end();
		GL11.glEndList();
		CapturedRenderingState.INSTANCE.setCompilingDisplayList(false);
	}

	private static void face(BufferBuilder builder, float x, float z, float y, float normal, float red, float green, float blue, float u, float v) {
		vertex(builder, x, y, z + 8.0F, x, z + 8.0F, red, green, blue, u, v, 0.0F, normal, 0.0F);
		vertex(builder, x + 8.0F, y, z + 8.0F, x + 8.0F, z + 8.0F, red, green, blue, u, v, 0.0F, normal, 0.0F);
		vertex(builder, x + 8.0F, y, z, x + 8.0F, z, red, green, blue, u, v, 0.0F, normal, 0.0F);
		vertex(builder, x, y, z, x, z, red, green, blue, u, v, 0.0F, normal, 0.0F);
	}

	private static void sideX(BufferBuilder builder, float x, float z, float planeX, float texelX, float normal, float red, float green, float blue, float u, float v) {
		vertex(builder, planeX, 0.0F, z + 8.0F, x + texelX, z + 8.0F, red, green, blue, u, v, normal, 0.0F, 0.0F);
		vertex(builder, planeX, 4.0F, z + 8.0F, x + texelX, z + 8.0F, red, green, blue, u, v, normal, 0.0F, 0.0F);
		vertex(builder, planeX, 4.0F, z, x + texelX, z, red, green, blue, u, v, normal, 0.0F, 0.0F);
		vertex(builder, planeX, 0.0F, z, x + texelX, z, red, green, blue, u, v, normal, 0.0F, 0.0F);
	}

	private static void sideZ(BufferBuilder builder, float x, float z, float planeZ, float texelZ, float normal, float red, float green, float blue, float u, float v) {
		vertex(builder, x, 4.0F, planeZ, x, z + texelZ, red, green, blue, u, v, 0.0F, 0.0F, normal);
		vertex(builder, x + 8.0F, 4.0F, planeZ, x + 8.0F, z + texelZ, red, green, blue, u, v, 0.0F, 0.0F, normal);
		vertex(builder, x + 8.0F, 0.0F, planeZ, x + 8.0F, z + texelZ, red, green, blue, u, v, 0.0F, 0.0F, normal);
		vertex(builder, x, 0.0F, planeZ, x, z + texelZ, red, green, blue, u, v, 0.0F, 0.0F, normal);
	}

	private static void vertex(BufferBuilder builder, float x, float y, float z, float texelU, float texelV,
							   float red, float green, float blue, float u, float v, float normalX, float normalY, float normalZ) {
		builder.vertex(x, y, z)
			.texture(texelU * TEXEL + u, texelV * TEXEL + v)
			.color(red, green, blue, 0.8F)
			.normal(normalX, normalY, normalZ)
			.nextVertex();
	}
}
