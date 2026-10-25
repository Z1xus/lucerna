package net.coderbot.iris.rendertarget;

import net.minecraft.client.render.texture.DynamicTexture;

public class NativeImageBackedSingleColorTexture extends DynamicTexture {
	public NativeImageBackedSingleColorTexture(int red, int green, int blue, int alpha) {
		super(1, 1);

		getPixels()[0] = alpha << 24 | red << 16 | green << 8 | blue;
		upload();
	}

	public NativeImageBackedSingleColorTexture(int rgba) {
		this(rgba >> 24 & 0xFF, rgba >> 16 & 0xFF, rgba >> 8 & 0xFF, rgba & 0xFF);
	}
}
