package us.z1x.lucerna.shaderpack;

// a bufferObject.<index> directive, relative buffers hold size bytes per cell of the scaled screen
public record ShaderStorageInfo(long size, boolean relative, float scaleX, float scaleY) {
}
