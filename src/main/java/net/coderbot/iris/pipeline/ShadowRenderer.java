package net.coderbot.iris.pipeline;

import com.google.common.collect.ImmutableList;
import dev.rdh.argentum.impl.ext.WorldRendererExtension;
import dev.rdh.argentum.impl.render.entity.instancing.EntityInstancing;
import dev.rdh.argentum.impl.render.terrain.ArgentumWorldRenderer;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gui.option.IrisVideoSettings;
import net.coderbot.iris.mixin.argentum.ArgentumWorldRendererAccessor;
import net.coderbot.iris.shaderpack.OptionalBoolean;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.shaderpack.PackShadowDirectives;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shadow.ShadowMatrices;
import net.coderbot.iris.shadows.ShadowRenderTargets;
import net.coderbot.iris.shadows.frustum.BoxCuller;
import net.coderbot.iris.shadows.frustum.CullEverythingFrustum;
import net.coderbot.iris.shadows.frustum.FrustumHolder;
import net.coderbot.iris.shadows.frustum.advanced.AdvancedShadowCullingFrustum;
import net.coderbot.iris.shadows.frustum.fallback.BoxCullingFrustum;
import net.coderbot.iris.shadows.frustum.fallback.NonCullingFrustum;
import net.coderbot.iris.uniforms.CameraUniforms;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.CelestialUniforms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Culler;
import net.minecraft.client.render.block.BlockLayer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.util.profiler.Profiler;
import org.embeddedt.embeddium.impl.gl.device.CommandList;
import org.embeddedt.embeddium.impl.gl.device.RenderDevice;
import org.embeddedt.embeddium.impl.render.chunk.ChunkRenderMatrices;
import org.embeddedt.embeddium.impl.render.viewport.Viewport;
import org.embeddedt.embeddium.impl.render.viewport.ViewportProvider;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBTextureSwizzle;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class ShadowRenderer {
	public static boolean ACTIVE = false;
	// ModelQuadFacing bits drawn by the current shadow terrain pass, all of them for sorted passes
	public static int faceMask = 0x7F;
	private static int lightFaceMask = 0x7F;
	private final float halfPlaneLength;
	private final float renderDistanceMultiplier;
	private final float entityShadowDistanceMultiplier;
	private final int resolution;
	private final float intervalSize;
	private final Float fov;
	private final ShadowRenderTargets targets;
	private final OptionalBoolean packCullingState;
	private boolean packHasVoxelization;
	private final boolean shouldRenderTerrain;
	private final boolean shouldRenderTranslucent;
	private final boolean shouldRenderEntities;
	private final boolean shouldRenderPlayer;
	private final boolean shouldRenderBlockEntities;
	private final float sunPathRotation;
	private final List<MipmapPass> mipmapPasses = new ArrayList<>();
	private final String debugStringOverall;
	private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
	private FrustumHolder terrainFrustumHolder;
	private FrustumHolder entityFrustumHolder;
	private String debugStringTerrain = "(unavailable)";
	private int renderedShadowEntities = 0;
	private int renderedShadowBlockEntities = 0;
	private final Profiler profiler;

	public ShadowRenderer(ProgramSource shadow, PackDirectives directives,
						  ShadowRenderTargets shadowRenderTargets) {

		this.profiler = Minecraft.getInstance().profiler;

		final PackShadowDirectives shadowDirectives = directives.getShadowDirectives();

		this.halfPlaneLength = shadowDirectives.getDistance();
		this.renderDistanceMultiplier = shadowDirectives.getDistanceRenderMul();
		this.entityShadowDistanceMultiplier = shadowDirectives.getEntityShadowDistanceMul();
		this.resolution = shadowDirectives.getResolution();
		this.intervalSize = shadowDirectives.getIntervalSize();
		this.shouldRenderTerrain = shadowDirectives.shouldRenderTerrain();
		this.shouldRenderTranslucent = shadowDirectives.shouldRenderTranslucent();
		this.shouldRenderEntities = shadowDirectives.shouldRenderEntities();
		this.shouldRenderPlayer = shadowDirectives.shouldRenderPlayer();
		this.shouldRenderBlockEntities = shadowDirectives.shouldRenderBlockEntities();

		debugStringOverall = "half plane = " + halfPlaneLength + " meters @ " + resolution + "x" + resolution;

		this.terrainFrustumHolder = new FrustumHolder();
		this.entityFrustumHolder = new FrustumHolder();

		this.fov = shadowDirectives.getFov();
		this.targets = shadowRenderTargets;

		if (shadow != null) {
			// Assume that the shader pack is doing voxelization if a geometry shader is detected.
			// Also assume voxelization if image load / store is detected.
			this.packHasVoxelization = shadow.getGeometrySource().isPresent();
			this.packCullingState = shadowDirectives.getCullingState();
		} else {
			this.packHasVoxelization = false;
			this.packCullingState = OptionalBoolean.DEFAULT;
		}

		this.sunPathRotation = directives.getSunPathRotation();

		configureSamplingSettings(shadowDirectives);
	}

	public void setUsesImages(boolean usesImages) {
		this.packHasVoxelization = packHasVoxelization || usesImages;
	}

	private void configureSamplingSettings(PackShadowDirectives shadowDirectives) {
		final ImmutableList<PackShadowDirectives.DepthSamplingSettings> depthSamplingSettings =
			shadowDirectives.getDepthSamplingSettings();

		final ImmutableList<PackShadowDirectives.SamplingSettings> colorSamplingSettings =
			shadowDirectives.getColorSamplingSettings();

		GlStateManager.activeTexture(GL20C.GL_TEXTURE4);

		configureDepthSampler(targets.getDepthTexture().getTextureId(), depthSamplingSettings.get(0));

		configureDepthSampler(targets.getDepthTextureNoTranslucents().getTextureId(), depthSamplingSettings.get(1));

		for (int i = 0; i < colorSamplingSettings.size(); i++) {
			int glTextureId = targets.get(i).getMainTexture();

			configureSampler(glTextureId, colorSamplingSettings.get(i));
		}

		GlStateManager.activeTexture(GL20C.GL_TEXTURE0);
	}

	private void configureDepthSampler(int glTextureId, PackShadowDirectives.DepthSamplingSettings settings) {
		if (settings.getHardwareFiltering()) {
			// We have to do this or else shadow hardware filtering breaks entirely!
			IrisRenderSystem.texParameteri(glTextureId, GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_COMPARE_MODE, GL30C.GL_COMPARE_REF_TO_TEXTURE);
		}

		// Workaround for issues with old shader packs like Chocapic v4.
		// They expected the driver to put the depth value in z, but it's supposed to only
		// be available in r. So we set up the swizzle to fix that.
		IrisRenderSystem.texParameteriv(glTextureId, GL20C.GL_TEXTURE_2D, ARBTextureSwizzle.GL_TEXTURE_SWIZZLE_RGBA,
			new int[] { GL30C.GL_RED, GL30C.GL_RED, GL30C.GL_RED, GL30C.GL_ONE });

		configureSampler(glTextureId, settings);
	}

	private void configureSampler(int glTextureId, PackShadowDirectives.SamplingSettings settings) {
		if (settings.getMipmap()) {
			int filteringMode = settings.getNearest() ? GL20C.GL_NEAREST_MIPMAP_NEAREST : GL20C.GL_LINEAR_MIPMAP_LINEAR;
			mipmapPasses.add(new MipmapPass(glTextureId, filteringMode));
		}

		if (!settings.getNearest()) {
			// Make sure that things are smoothed
			IrisRenderSystem.texParameteri(glTextureId, GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_LINEAR);
			IrisRenderSystem.texParameteri(glTextureId, GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MAG_FILTER, GL20C.GL_LINEAR);
		} else {
			IrisRenderSystem.texParameteri(glTextureId, GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_NEAREST);
			IrisRenderSystem.texParameteri(glTextureId, GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MAG_FILTER, GL20C.GL_NEAREST);
		}
	}

	private void generateMipmaps() {
		GlStateManager.activeTexture(GL20C.GL_TEXTURE4);

		for (MipmapPass mipmapPass : mipmapPasses) {
			setupMipmappingForTexture(mipmapPass.getTexture(), mipmapPass.getTargetFilteringMode());
		}

		GlStateManager.activeTexture(GL20C.GL_TEXTURE0);
	}

	private void setupMipmappingForTexture(int texture, int filteringMode) {
		IrisRenderSystem.generateMipmaps(texture, GL20C.GL_TEXTURE_2D);
		IrisRenderSystem.texParameteri(texture, GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, filteringMode);
	}

	private FrustumHolder createShadowFrustum(float renderMultiplier, FrustumHolder holder) {
		// TODO: Cull entities / block entities with Advanced Frustum Culling even if voxelization is detected.
		String distanceInfo;
		String cullingInfo;
		if ((packCullingState == OptionalBoolean.FALSE || packHasVoxelization) && packCullingState != OptionalBoolean.TRUE) {
			double distance = halfPlaneLength * renderMultiplier;

			String reason;

			if (packCullingState == OptionalBoolean.FALSE) {
				reason = "(set by shader pack)";
			} else {
				reason = "(voxelization detected)";
			}

			if (distance <= 0 || distance > Minecraft.getInstance().options.viewDistance * 16) {
				distanceInfo = Minecraft.getInstance().options.viewDistance * 16
					+ " blocks (capped by normal render distance)";
				cullingInfo = "disabled " + reason;
				return holder.setInfo(new NonCullingFrustum(), distanceInfo, cullingInfo);
			} else {
				distanceInfo = distance + " blocks (set by shader pack)";
				cullingInfo = "distance only " + reason;
				BoxCuller boxCuller = new BoxCuller(distance);
				holder.setInfo(new BoxCullingFrustum(boxCuller), distanceInfo, cullingInfo);
			}
		} else {
			BoxCuller boxCuller;

			double distance = halfPlaneLength * renderMultiplier;
			String setter = "(set by shader pack)";

			if (renderMultiplier < 0) {
				distance = IrisVideoSettings.shadowDistance * 16;
				setter = "(set by user)";
			}

			if (distance >= Minecraft.getInstance().options.viewDistance * 16) {
				distanceInfo = Minecraft.getInstance().options.viewDistance * 16
					+ " blocks (capped by normal render distance)";
				boxCuller = null;
			} else {
				distanceInfo = distance + " blocks " + setter;

				if (distance == 0.0) {
					cullingInfo = "no shadows rendered";
					holder.setInfo(new CullEverythingFrustum(), distanceInfo, cullingInfo);
				}

				boxCuller = new BoxCuller(distance);
			}

			cullingInfo = "Advanced Frustum Culling enabled";

			Vector4f shadowLightPosition = new CelestialUniforms(sunPathRotation).getShadowLightPositionInWorldSpace();

			Vector3f shadowLightVectorFromOrigin =
				new Vector3f(shadowLightPosition.x(), shadowLightPosition.y(), shadowLightPosition.z());

			shadowLightVectorFromOrigin.normalize();

			return holder.setInfo(new AdvancedShadowCullingFrustum(new Matrix4f(CapturedRenderingState.INSTANCE.getGbufferModelView()),
				new Matrix4f(CapturedRenderingState.INSTANCE.getGbufferProjection()), shadowLightVectorFromOrigin, boxCuller), distanceInfo, cullingInfo);

		}

		return holder;
	}

	private void loadMatrix(int mode, Matrix4f matrix) {
		GlStateManager.matrixMode(mode);
		GlStateManager.pushMatrix();
		matrix.get(matrixBuffer);
		GL11.glLoadMatrixf(matrixBuffer);
	}

	private void restoreMatrix(int mode) {
		GlStateManager.matrixMode(mode);
		GlStateManager.popMatrix();
	}

	// terrain only needs the faces turned toward the light: the shadow map keeps the surface nearest the light, and for
	// closed blocks that is never a face turned away from it. the vector points toward the light in world space
	private static int facesTowardLight(float x, float y, float z) {
		final float edge = 1.0E-3F;
		int mask = 1 << 6;
		mask |= x > -edge ? 1 : 0;
		mask |= y > -edge ? 1 << 1 : 0;
		mask |= z > -edge ? 1 << 2 : 0;
		mask |= x < edge ? 1 << 3 : 0;
		mask |= y < edge ? 1 << 4 : 0;
		mask |= z < edge ? 1 << 5 : 0;
		return mask;
	}

	public static void beginTerrainPass(boolean sorted) {
		faceMask = sorted ? 0x7F : lightFaceMask;
	}

	public void renderShadows(WorldRenderer worldRenderer, Entity camera, Culler playerCuller) {
		if (IrisVideoSettings.getOverriddenShadowDistance(IrisVideoSettings.shadowDistance) == 0) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		ArgentumWorldRenderer renderer = ((WorldRendererExtension) worldRenderer).argentum$getWorldRenderer();
		float tickDelta = CapturedRenderingState.INSTANCE.getTickDelta();

		profiler.push("shadows");
		ACTIVE = true;

		GlStateManager.viewport(0, 0, resolution, resolution);

		Matrix4f modelView = ShadowMatrices.createShadowModelView(this.sunPathRotation, this.intervalSize);
		lightFaceMask = facesTowardLight(-modelView.m20(), -modelView.m21(), -modelView.m22());

		profiler.push("terrain_setup");

		terrainFrustumHolder = createShadowFrustum(renderDistanceMultiplier, terrainFrustumHolder);

		// Determine the player camera position
		Vector3d cameraPos = CameraUniforms.getUnshiftedCameraPosition();

		double cameraX = cameraPos.x;
		double cameraY = cameraPos.y;
		double cameraZ = cameraPos.z;

		// Center the frustum on the player camera position
		terrainFrustumHolder.getFrustum().prepare(cameraX, cameraY, cameraZ);

		Viewport playerViewport = ((ViewportProvider) playerCuller).sodium$createViewport();
		Viewport shadowViewport = ((ViewportProvider) terrainFrustumHolder.getFrustum()).sodium$createViewport();

		if (!renderer.getRenderSectionManager().hasShadowPass()) {
			// the section manager predates the shader pack, it gains a shadow pass on the next reload
			client.worldRenderer.reload();
		}

		RenderDevice.enterManagedCode();

		try {
			renderer.setupShadowTerrain(playerViewport, shadowViewport, ArgentumWorldRenderer.captureCameraState(tickDelta),
				0, client.player.isSpectator());
		} finally {
			RenderDevice.exitManagedCode();
		}

		profiler.swap("terrain");

		// Set up our orthographic projection matrix and load it into the legacy matrix stack
		Matrix4f projection = new Matrix4f().set(this.fov != null
			? ShadowMatrices.createPerspectiveMatrix(this.fov)
			: ShadowMatrices.createOrthoMatrix(halfPlaneLength));

		loadMatrix(GL11.GL_PROJECTION, projection);
		loadMatrix(GL11.GL_MODELVIEW, modelView);

		ChunkRenderMatrices playerMatrices = ((ArgentumWorldRendererAccessor) renderer).lucerna$getMatrices();
		((ArgentumWorldRendererAccessor) renderer).lucerna$setMatrices(new ChunkRenderMatrices(projection, modelView));

		// Disable backface culling
		// This partially works around an issue where if the front face of a mountain isn't visible, it casts no
		// shadow.
		//
		// However, it only partially resolves issues of light leaking into caves.
		GlStateManager.disableCull();

		// Render all opaque terrain unless pack requests not to
		if (shouldRenderTerrain) {
			worldRenderer.render(BlockLayer.SOLID, tickDelta, 0, camera);
			worldRenderer.render(BlockLayer.CUTOUT_MIPPED, tickDelta, 0, camera);
			worldRenderer.render(BlockLayer.CUTOUT, tickDelta, 0, camera);
		}

		// Reset our viewport in case Argentum overrode it
		GlStateManager.viewport(0, 0, resolution, resolution);

		profiler.swap("entities");

		// Create a constrained shadow frustum for entities to avoid rendering faraway entities in the shadow pass,
		// if the shader pack has requested it. Otherwise, use the same frustum as for terrain.
		boolean hasEntityFrustum = false;

		if (entityShadowDistanceMultiplier == 1.0F || entityShadowDistanceMultiplier < 0.0F) {
			entityFrustumHolder.setInfo(terrainFrustumHolder.getFrustum(), terrainFrustumHolder.getDistanceInfo(), terrainFrustumHolder.getCullingInfo());
		} else {
			hasEntityFrustum = true;
			entityFrustumHolder = createShadowFrustum(renderDistanceMultiplier * entityShadowDistanceMultiplier, entityFrustumHolder);
		}

		Culler entityShadowFrustum = entityFrustumHolder.getFrustum();
		entityShadowFrustum.prepare(cameraX, cameraY, cameraZ);

		EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();

		BlockEntityRenderDispatcher.INSTANCE.prepare(client.world, client.getTextureManager(), client.textRenderer, camera, tickDelta);
		dispatcher.prepare(client.world, client.textRenderer, camera, client.targetEntity, client.options, tickDelta);
		BlockEntityRenderDispatcher.offsetX = cameraX;
		BlockEntityRenderDispatcher.offsetY = cameraY;
		BlockEntityRenderDispatcher.offsetZ = cameraZ;
		dispatcher.setCameraPos(cameraX, cameraY, cameraZ);

		client.gameRenderer.enableLightMap();

		// argentum instances the entity models, flushed while the shadow matrices are still loaded
		EntityInstancing instancing = EntityInstancing.current();
		boolean instanced = instancing != null && instancing.resumeBatch();

		if (shouldRenderEntities) {
			renderEntities(dispatcher, entityShadowFrustum, cameraX, cameraY, cameraZ, tickDelta);
		} else if (shouldRenderPlayer) {
			renderPlayerEntity(dispatcher, entityShadowFrustum, cameraX, cameraY, cameraZ, tickDelta);
		}

		if (instanced) {
			RenderDevice.enterManagedCode();

			try (CommandList commandList = RenderDevice.INSTANCE.createCommandList()) {
				instancing.flush(commandList);
			} finally {
				RenderDevice.exitManagedCode();
			}
		}

		profiler.swap("blockentities");

		if (shouldRenderBlockEntities) {
			renderedShadowBlockEntities = renderer.renderBlockEntities(tickDelta);
		}

		client.gameRenderer.disableLightMap();

		// NB: Don't try to draw the translucent parts of entities afterwards in the shadow pass. It'll cause problems since some
		// shader packs assume that everything drawn afterwards is actually translucent and should cast a colored
		// shadow...
		copyPreTranslucentDepth();

		profiler.swap("translucent terrain");

		if (shouldRenderTerrain && shouldRenderTranslucent) {
			worldRenderer.render(BlockLayer.TRANSLUCENT, tickDelta, 0, camera);
		}

		debugStringTerrain = renderer.getChunksDebugString();

		profiler.swap("generate mipmaps");

		generateMipmaps();

		profiler.swap("restore gl state");

		((ArgentumWorldRendererAccessor) renderer).lucerna$setMatrices(playerMatrices);

		GlStateManager.enableCull();

		restoreMatrix(GL11.GL_MODELVIEW);
		restoreMatrix(GL11.GL_PROJECTION);
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);

		GlStateManager.viewport(0, 0, IrisVideoSettings.scaleRenderSize(client.getRenderTarget().width), IrisVideoSettings.scaleRenderSize(client.getRenderTarget().height));

		ACTIVE = false;
		profiler.pop();
		profiler.pop();
	}

	private void copyPreTranslucentDepth() {
		profiler.swap("translucent depth copy");

		targets.copyPreTranslucentDepth();
	}

	private void renderEntities(EntityRenderDispatcher dispatcher, Culler frustum, double cameraX, double cameraY, double cameraZ, float tickDelta) {
		int shadowEntities = 0;

		for (Entity entity : Minecraft.getInstance().world.getEntities()) {
			if (!dispatcher.shouldRender(entity, frustum, cameraX, cameraY, cameraZ) || entity instanceof PlayerEntity && ((PlayerEntity) entity).isSpectator()) {
				continue;
			}

			dispatcher.render(entity, tickDelta);
			shadowEntities++;
		}

		renderedShadowEntities = shadowEntities;
	}

	private void renderPlayerEntity(EntityRenderDispatcher dispatcher, Culler frustum, double cameraX, double cameraY, double cameraZ, float tickDelta) {
		PlayerEntity player = Minecraft.getInstance().player;

		if (!dispatcher.shouldRender(player, frustum, cameraX, cameraY, cameraZ) || player.isSpectator()) {
			return;
		}

		int shadowEntities = 0;

		if (player.rider != null) {
			dispatcher.render(player.rider, tickDelta);
			shadowEntities++;
		}

		if (player.vehicle != null) {
			dispatcher.render(player.vehicle, tickDelta);
			shadowEntities++;
		}

		dispatcher.render(player, tickDelta);
		shadowEntities++;

		renderedShadowEntities = shadowEntities;
	}

	public void addDebugText(List<String> messages) {
		messages.add("[" + Iris.MODNAME + "] Shadow Maps: " + debugStringOverall);
		messages.add("[" + Iris.MODNAME + "] Shadow Distance Terrain: " + terrainFrustumHolder.getDistanceInfo() + " Entity: " + entityFrustumHolder.getDistanceInfo());
		messages.add("[" + Iris.MODNAME + "] Shadow Culling Terrain: " + terrainFrustumHolder.getCullingInfo() + " Entity: " + entityFrustumHolder.getCullingInfo());
		messages.add("[" + Iris.MODNAME + "] Shadow Terrain: " + debugStringTerrain
			+ (shouldRenderTerrain ? "" : " (no terrain) ") + (shouldRenderTranslucent ? "" : "(no translucent)"));
		messages.add("[" + Iris.MODNAME + "] Shadow Entities: " + getEntitiesDebugString());
		messages.add("[" + Iris.MODNAME + "] Shadow Block Entities: " + getBlockEntitiesDebugString());
	}

	private String getEntitiesDebugString() {
		return (shouldRenderEntities || shouldRenderPlayer) ? (renderedShadowEntities + "/" + Minecraft.getInstance().world.getEntities().size()) : "disabled by pack";
	}

	private String getBlockEntitiesDebugString() {
		return shouldRenderBlockEntities ? (renderedShadowBlockEntities + "/" + Minecraft.getInstance().world.blockEntities.size()) : "disabled by pack";
	}

	private static class MipmapPass {
		private final int texture;
		private final int targetFilteringMode;

		public MipmapPass(int texture, int targetFilteringMode) {
			this.texture = texture;
			this.targetFilteringMode = targetFilteringMode;
		}

		public int getTexture() {
			return texture;
		}

		public int getTargetFilteringMode() {
			return targetFilteringMode;
		}
	}
}
