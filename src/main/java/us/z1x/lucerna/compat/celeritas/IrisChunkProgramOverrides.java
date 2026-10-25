package us.z1x.lucerna.compat.celeritas;

import us.z1x.lucerna.Iris;
import us.z1x.lucerna.pipeline.SodiumTerrainPipeline;
import us.z1x.lucerna.pipeline.WorldRenderingPipeline;
import us.z1x.lucerna.pipeline.transform.PatchShaderType;
import us.z1x.lucerna.shadows.ShadowRenderingState;
import org.embeddedt.embeddium.impl.gl.GlObject;
import org.embeddedt.embeddium.impl.gl.shader.GlProgram;
import org.embeddedt.embeddium.impl.gl.shader.GlShader;
import org.embeddedt.embeddium.impl.gl.shader.ShaderType;
import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class IrisChunkProgramOverrides {
    private static final Map<PatchShaderType, ShaderType> SHADER_TYPES = Map.of(
        PatchShaderType.VERTEX, ShaderType.VERTEX,
        PatchShaderType.GEOMETRY, ShaderType.GEOM,
        PatchShaderType.FRAGMENT, ShaderType.FRAGMENT
    );

    private final EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>> programs = new EnumMap<>(IrisTerrainPass.class);
    private boolean shadersCreated;
    private int versionCounterForSodiumShaderReload = -1;

    @Nullable
    private GlProgram<IrisChunkShaderInterface> createProgram(IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
        SodiumTerrainPipeline.PassInfo info = pipeline.getPassInfo(pass);

        if (info.sources().get(PatchShaderType.VERTEX) == null || info.sources().get(PatchShaderType.FRAGMENT) == null) {
            return null;
        }

        List<GlShader> shaders = new ArrayList<>();

        try {
            info.sources().forEach((type, source) -> {
                if (source != null) {
                    ShaderType shaderType = SHADER_TYPES.get(type);
                    shaders.add(new GlShader(shaderType, "iris:celeritas-terrain-" + pass.toString().toLowerCase(Locale.ROOT) + "." + shaderType.fileExtension, source));
                }
            });

            GlProgram.Builder builder = GlProgram.builder("iris:chunk_shader_for_" + pass.getName());
            shaders.forEach(builder::attachShader);

            int i = 0;

            for (var attr : XHFPModelVertexType.VERTEX_FORMAT.getAttributes()) {
                builder.bindAttribute(attr.getName(), i++);
            }

            return builder.link(shader -> new IrisChunkShaderInterface(((GlObject) shader).handle(), shader, pipeline, pass,
                info.blendModeOverride(), info.bufferOverrides(), info.alphaReference()));
        } finally {
            shaders.forEach(GlShader::delete);
        }
    }

    private void createShaders(SodiumTerrainPipeline pipeline) {
        for (IrisTerrainPass pass : IrisTerrainPass.values()) {
            if (pass.isShadow() && !pipeline.hasShadowPass()) {
                continue;
            }

            programs.put(pass, createProgram(pass, pipeline));
        }

        shadersCreated = true;
    }

    @Nullable
    public GlProgram<IrisChunkShaderInterface> getProgramOverride(TerrainRenderPass pass) {
        if (versionCounterForSodiumShaderReload != Iris.getPipelineManager().getVersionCounterForSodiumShaderReload()) {
            versionCounterForSodiumShaderReload = Iris.getPipelineManager().getVersionCounterForSodiumShaderReload();
            deleteShaders();
        }

        WorldRenderingPipeline worldRenderingPipeline = Iris.getPipelineManager().getPipelineNullable();
        SodiumTerrainPipeline sodiumTerrainPipeline = worldRenderingPipeline != null ? worldRenderingPipeline.getSodiumTerrainPipeline() : null;

        if (sodiumTerrainPipeline == null) {
            return null;
        }

        if (!shadersCreated) {
            createShaders(sodiumTerrainPipeline);
        }

        if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return programs.get(pass.supportsFragmentDiscard() ? IrisTerrainPass.SHADOW_CUTOUT : IrisTerrainPass.SHADOW);
        } else if (pass.supportsFragmentDiscard()) {
            return programs.get(IrisTerrainPass.GBUFFER_CUTOUT);
        } else if (pass.isReverseOrder()) {
            return programs.get(IrisTerrainPass.GBUFFER_TRANSLUCENT);
        } else {
            return programs.get(IrisTerrainPass.GBUFFER_SOLID);
        }
    }

    public void deleteShaders() {
        for (GlProgram<?> program : programs.values()) {
            if (program != null) {
                program.delete();
            }
        }

        programs.clear();
        shadersCreated = false;
    }
}
