package net.coderbot.iris.pipeline;

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.celeritas.IrisTerrainPass;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.AlphaTestOverride;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.blending.BufferBlendOverride;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.ProgramImages;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.pipeline.transform.PatchShaderType;
import net.coderbot.iris.pipeline.transform.TransformPatcher;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shaderpack.loading.ProgramId;
import net.coderbot.iris.shaderpack.texture.TextureStage;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.builtin.BuiltinReplacementUniforms;
import net.coderbot.iris.uniforms.custom.CustomUniforms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Patches the terrain programs of a shader pack so that they read Celeritas chunk vertices, for Argentum's terrain
 * renderer.
 */
public class SodiumTerrainPipeline {
	private static final float CUTOUT_ALPHA_REFERENCE = 0.1F;

	public static final class PassInfo {
		private final Map<PatchShaderType, String> sources = new EnumMap<>(PatchShaderType.class);
		private GlFramebuffer framebuffer;
		private BlendModeOverride blendModeOverride;
		private List<BufferBlendOverride> bufferOverrides = Collections.emptyList();
		private float alphaReference;

		public Map<PatchShaderType, String> sources() {
			return sources;
		}

		public GlFramebuffer framebuffer() {
			return framebuffer;
		}

		public BlendModeOverride blendModeOverride() {
			return blendModeOverride;
		}

		public List<BufferBlendOverride> bufferOverrides() {
			return bufferOverrides;
		}

		public float alphaReference() {
			return alphaReference;
		}
	}

	private final WorldRenderingPipeline parent;
	private final CustomUniforms customUniforms;
	private final ProgramSet programSet;
	private final Map<IrisTerrainPass, PassInfo> passInfoMap = new EnumMap<>(IrisTerrainPass.class);
	private final Map<IrisTerrainPass, Optional<ProgramSource>> programSources = new EnumMap<>(IrisTerrainPass.class);
	private final PassInfo entityPassInfo = new PassInfo();
	private final PassInfo shadowEntityPassInfo = new PassInfo();

	private final IntFunction<ProgramSamplers> createTerrainSamplers;
	private final IntFunction<ProgramSamplers> createShadowSamplers;

	private final IntFunction<ProgramImages> createTerrainImages;
	private final IntFunction<ProgramImages> createShadowImages;

	public SodiumTerrainPipeline(WorldRenderingPipeline parent,
								 ProgramSet programSet, IntFunction<ProgramSamplers> createTerrainSamplers,
								 IntFunction<ProgramSamplers> createShadowSamplers,
								 IntFunction<ProgramImages> createTerrainImages,
								 IntFunction<ProgramImages> createShadowImages,
								 RenderTargets targets, ImmutableSet<Integer> flippedAfterPrepare,
								 ImmutableSet<Integer> flippedAfterTranslucent, Supplier<GlFramebuffer> shadowFramebuffer,
								 CustomUniforms customUniforms) {
		this.parent = Objects.requireNonNull(parent);
		this.customUniforms = customUniforms;
		this.programSet = programSet;

		Optional<ProgramSource> terrainSource = first(programSet.getGbuffersTerrain(), programSet.getGbuffersTexturedLit(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());

		programSources.put(IrisTerrainPass.GBUFFER_SOLID, terrainSource);
		programSources.put(IrisTerrainPass.GBUFFER_CUTOUT, terrainSource);
		programSources.put(IrisTerrainPass.GBUFFER_TRANSLUCENT, first(programSet.getGbuffersWater(), terrainSource));
		programSources.put(IrisTerrainPass.SHADOW, programSet.getShadow());
		programSources.put(IrisTerrainPass.SHADOW_CUTOUT, programSet.getShadow());

		for (IrisTerrainPass pass : IrisTerrainPass.values()) {
			PassInfo info = new PassInfo();
			passInfoMap.put(pass, info);

			if (pass.isShadow()) {
				if (createShadowSamplers != null) {
					info.framebuffer = shadowFramebuffer.get();
				}
			} else {
				ImmutableSet<Integer> flipped = pass == IrisTerrainPass.GBUFFER_TRANSLUCENT ? flippedAfterTranslucent : flippedAfterPrepare;
				int[] drawBuffers = programSources.get(pass).map(source -> source.getDirectives().getDrawBuffers()).orElse(new int[] {0});
				info.framebuffer = targets.createGbufferFramebuffer(flipped, drawBuffers);
			}
		}

		Optional<ProgramSource> entitySource = first(programSet.getGbuffersEntities(), programSet.getGbuffersTexturedLit(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		int[] entityDrawBuffers = entitySource.map(source -> source.getDirectives().getDrawBuffers()).orElse(new int[] {0});
		entityPassInfo.framebuffer = targets.createGbufferFramebuffer(flippedAfterPrepare, entityDrawBuffers);
		Map<String, String> textureRenames = programSet.getPack().getCustomTexturePatching(TextureStage.GBUFFERS_AND_SHADOW);
		patchEntityShaders(entityPassInfo, entitySource, ProgramId.Entities, textureRenames);

		if (createShadowSamplers != null) {
			shadowEntityPassInfo.framebuffer = shadowFramebuffer.get();
			patchEntityShaders(shadowEntityPassInfo, programSet.getShadow(), ProgramId.Shadow, textureRenames);
		}

		this.createTerrainSamplers = createTerrainSamplers;
		this.createShadowSamplers = createShadowSamplers;
		this.createTerrainImages = createTerrainImages;
		this.createShadowImages = createShadowImages;

		patchShaders();
	}

	private void patchShaders() {
		for (IrisTerrainPass pass : IrisTerrainPass.values()) {
			PassInfo info = passInfoMap.get(pass);
			ProgramId programId = switch (pass) {
				case GBUFFER_TRANSLUCENT -> ProgramId.Water;
				case SHADOW, SHADOW_CUTOUT -> ProgramId.Shadow;
				default -> ProgramId.Terrain;
			};
			boolean cutout = pass == IrisTerrainPass.GBUFFER_CUTOUT || pass == IrisTerrainPass.SHADOW_CUTOUT;

			programSources.get(pass).ifPresent(sources -> {
				info.blendModeOverride = sources.getDirectives().getBlendModeOverride().orElse(programId.getBlendModeOverride());
				info.bufferOverrides = new ArrayList<>();
				sources.getDirectives().getBufferBlendOverrides().forEach(information -> {
					int index = Ints.indexOf(sources.getDirectives().getDrawBuffers(), information.getIndex());
					if (index > -1) {
						info.bufferOverrides.add(new BufferBlendOverride(index, information.getBlendMode()));
					}
				});

				AlphaTest alphaTest = sources.getDirectives().getAlphaTestOverride().map(AlphaTestOverride::getAlphaTest).orElse(null);
				info.alphaReference = cutout ? (alphaTest != null ? alphaTest.getReference() : CUTOUT_ALPHA_REFERENCE) : 0.0F;

				Map<PatchShaderType, String> result = TransformPatcher.patchSodiumTerrain(
					sources.getVertexSource().orElse(null),
					sources.getGeometrySource().orElse(null),
					sources.getFragmentSource().orElse(null),
					programSet.getPack().getCustomTexturePatching(TextureStage.GBUFFERS_AND_SHADOW));

				info.sources.putAll(result);

				PatchedShaderPrinter.debugPatchedShaders(sources.getName() + "_celeritas_" + pass.name().toLowerCase(Locale.ROOT),
					result.get(PatchShaderType.VERTEX), result.get(PatchShaderType.GEOMETRY), result.get(PatchShaderType.FRAGMENT));
			});
		}
	}

	private static void patchEntityShaders(PassInfo info, Optional<ProgramSource> source, ProgramId programId, Map<String, String> textureRenames) {
		source.filter(sources -> !sources.getGeometrySource().isPresent()).ifPresent(sources -> {
			info.blendModeOverride = sources.getDirectives().getBlendModeOverride().orElse(programId.getBlendModeOverride());
			info.bufferOverrides = new ArrayList<>();
			sources.getDirectives().getBufferBlendOverrides().forEach(information -> {
				int index = Ints.indexOf(sources.getDirectives().getDrawBuffers(), information.getIndex());
				if (index > -1) {
					info.bufferOverrides.add(new BufferBlendOverride(index, information.getBlendMode()));
				}
			});

			try {
				Map<PatchShaderType, String> result = TransformPatcher.patchInstancedEntity(sources.getVertexSource().orElse(null), sources.getFragmentSource().orElse(null), textureRenames);
				info.sources.putAll(result);
				PatchedShaderPrinter.debugPatchedShaders(sources.getName() + "_instanced", result.get(PatchShaderType.VERTEX), null, result.get(PatchShaderType.FRAGMENT));
			} catch (RuntimeException e) {
				// argentum draws these entities with display lists instead
				Iris.logger.warn("Failed to patch " + sources.getName() + " for instanced entities", e);
			}
		});
	}

	public PassInfo getEntityPassInfo(boolean shadow) {
		return shadow ? shadowEntityPassInfo : entityPassInfo;
	}

	public ProgramUniforms initUniforms(int programId) {
		ProgramUniforms.Builder uniforms = ProgramUniforms.builder("<celeritas terrain>", programId);

		CommonUniforms.addCommonUniforms(uniforms, programSet.getPack().getIdMap(), programSet.getPackDirectives(), parent.getFrameUpdateNotifier());
		customUniforms.assignTo(uniforms);
		BuiltinReplacementUniforms.addBuiltinReplacementUniforms(uniforms);

		return uniforms.buildUniforms();
	}

	public boolean hasShadowPass() {
		return createShadowSamplers != null;
	}

	public PassInfo getPassInfo(IrisTerrainPass pass) {
		return passInfoMap.get(pass);
	}

	public ProgramSamplers initTerrainSamplers(int programId) {
		return createTerrainSamplers.apply(programId);
	}

	public ProgramSamplers initShadowSamplers(int programId) {
		return createShadowSamplers.apply(programId);
	}

	public ProgramImages initTerrainImages(int programId) {
		return createTerrainImages.apply(programId);
	}

	public ProgramImages initShadowImages(int programId) {
		return createShadowImages.apply(programId);
	}

	@SafeVarargs
	private static <T> Optional<T> first(Optional<T>... candidates) {
		for (Optional<T> candidate : candidates) {
			if (candidate.isPresent()) {
				return candidate;
			}
		}

		return Optional.empty();
	}
}
