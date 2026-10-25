package net.coderbot.iris.shaderpack;

import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.gl.texture.PixelFormat;
import net.coderbot.iris.gl.texture.PixelType;

// an image.<name> directive, the sampler name is null when the pack only reads it as an image
public record ImageInformation(String name, String samplerName, int target, PixelFormat format, InternalTextureFormat internalFormat, PixelType type,
							   int width, int height, int depth, boolean clear, boolean relative, float relativeWidth, float relativeHeight) {
}
