package us.z1x.lucerna.shaderpack.texture;

import java.util.Optional;

public enum TextureStage {
    // not documented in shaders.txt, but valid for custom textures
    SHADOWCOMP,
    // not documented in shaders.txt, but valid for custom textures
    PREPARE,
    GBUFFERS_AND_SHADOW,
    DEFERRED,
    COMPOSITE_AND_FINAL;

    public static Optional<TextureStage> parse(String name) {
        switch (name) {
            case "shadowcomp":
                return Optional.of(SHADOWCOMP);
            case "prepare":
                return Optional.of(PREPARE);
            case "gbuffers":
                return Optional.of(GBUFFERS_AND_SHADOW);
            case "deferred":
                return Optional.of(DEFERRED);
            case "composite":
                return Optional.of(COMPOSITE_AND_FINAL);
            default:
                return Optional.empty();
        }
    }
}
