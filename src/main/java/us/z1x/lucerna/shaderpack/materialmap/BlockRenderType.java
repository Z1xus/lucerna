package us.z1x.lucerna.shaderpack.materialmap;

import java.util.Optional;

// see https://github.com/sp614x/optifine/blob/9ea6d9b7f57f20babae64f3807b00a6ca6e1d44a/OptiFineDoc/doc/shaders.txt#L662-L681
public enum BlockRenderType {
    // alpha test off, blending off, mipmaps on
    SOLID,
    // alpha test on, blending off, mipmaps off, for tall grass and glass
    CUTOUT,
    // alpha test on, blending off, mipmaps on, for leaves
    CUTOUT_MIPPED,
    // alpha test on with a low cutoff, blending on, mipmaps on, for stained glass, portals, and water
    TRANSLUCENT;

    public static Optional<BlockRenderType> fromString(String name) {
        switch (name) {
            case "solid":
                return Optional.of(SOLID);
            case "cutout":
                return Optional.of(CUTOUT);
            case "cutout_mipped":
                return Optional.of(CUTOUT_MIPPED);
            case "translucent":
                return Optional.of(TRANSLUCENT);
            default:
                return Optional.empty();
        }
    }
}
