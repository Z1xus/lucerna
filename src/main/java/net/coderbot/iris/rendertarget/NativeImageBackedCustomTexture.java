package net.coderbot.iris.rendertarget;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.shaderpack.texture.CustomTextureData;
import net.minecraft.client.render.texture.DynamicTexture;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class NativeImageBackedCustomTexture extends DynamicTexture {
	public NativeImageBackedCustomTexture(CustomTextureData.PngData textureData) throws IOException {
		super(ImageIO.read(new ByteArrayInputStream(textureData.getContent())));

		// By default, images are unblurred and not clamped.

		if (textureData.getFilteringData().shouldBlur()) {
			IrisRenderSystem.texParameteri(getGlId(), GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
			IrisRenderSystem.texParameteri(getGlId(), GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
		}

		if (textureData.getFilteringData().shouldClamp()) {
			IrisRenderSystem.texParameteri(getGlId(), GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL13C.GL_CLAMP_TO_EDGE);
			IrisRenderSystem.texParameteri(getGlId(), GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL13C.GL_CLAMP_TO_EDGE);
		}
	}
}
