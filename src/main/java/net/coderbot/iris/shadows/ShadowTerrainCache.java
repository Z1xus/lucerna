package net.coderbot.iris.shadows;

import net.coderbot.iris.texture.TextureInfoCache;
import net.minecraft.client.render.platform.GlStateManager;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL42C;
import org.lwjgl.opengl.GL43C;

// copies of the shadow textures that opaque terrain writes, put back on frames where that terrain would come out the same
public final class ShadowTerrainCache {
	private final ShadowRenderTargets targets;
	private final int[] colorTargets;
	private final int[] copies;

	public ShadowTerrainCache(ShadowRenderTargets targets, int[] colorTargets) {
		this.targets = targets;
		this.colorTargets = colorTargets;
		this.copies = new int[colorTargets.length + 1];
	}

	public void store() {
		int resolution = targets.getResolution();

		for (int i = 0; i < copies.length; i++) {
			int source = source(i);

			if (copies[i] == 0) {
				copies[i] = GL11C.glGenTextures();
				GlStateManager.bindTexture(copies[i]);
				GL42C.glTexStorage2D(GL11C.GL_TEXTURE_2D, 1, TextureInfoCache.INSTANCE.getInfo(source).getInternalFormat(), resolution, resolution);
			}

			copy(source, copies[i], resolution);
		}

		GlStateManager.bindTexture(0);
	}

	public void restore() {
		int resolution = targets.getResolution();

		for (int i = 0; i < copies.length; i++) {
			copy(copies[i], source(i), resolution);
		}
	}

	public void destroy() {
		GL11C.glDeleteTextures(copies);
	}

	// the flip state picks the main or alt texture, it is the same at store and restore time within a frame
	private int source(int index) {
		return index == 0 ? targets.getDepthTexture().getTextureId() : targets.getColorTextureId(colorTargets[index - 1]);
	}

	private static void copy(int source, int destination, int resolution) {
		GL43C.glCopyImageSubData(source, GL11C.GL_TEXTURE_2D, 0, 0, 0, 0, destination, GL11C.GL_TEXTURE_2D, 0, 0, 0, 0, resolution, resolution, 1);
	}
}
