package us.z1x.lucerna.gl.image;

import us.z1x.lucerna.gl.texture.InternalTextureFormat;

import java.util.function.IntSupplier;

public interface ImageHolder {
    boolean hasImage(String name);
    void addTextureImage(IntSupplier textureID, InternalTextureFormat internalFormat, String name);
}
