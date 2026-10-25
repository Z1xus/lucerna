package net.coderbot.iris.pipeline;

import net.coderbot.iris.mixin.GameRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.LivingEntity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

public class HandRenderer {
	public static final HandRenderer INSTANCE = new HandRenderer();

	public static final float DEPTH = 0.125F;

	private boolean ACTIVE;

	private boolean canRender(Minecraft minecraft) {
		Entity camera = minecraft.getCamera();

		return ((GameRendererAccessor) minecraft.gameRenderer).lucerna$shouldRenderHand()
			&& minecraft.options.perspective == 0
			&& !(camera instanceof LivingEntity && ((LivingEntity) camera).isSleeping())
			&& !minecraft.options.hideGui
			&& !minecraft.interactionManager.hidesGui();
	}

	public void renderSolid(float tickDelta, WorldRenderingPipeline pipeline) {
		Minecraft minecraft = Minecraft.getInstance();

		if (!canRender(minecraft)) {
			return;
		}

		GameRenderer gameRenderer = minecraft.gameRenderer;
		GameRendererAccessor accessor = (GameRendererAccessor) gameRenderer;

		ACTIVE = true;

		pipeline.setPhase(WorldRenderingPhase.HAND_SOLID);

		minecraft.profiler.push("iris_hand");

		// We need to scale the projection by 0.125 so the hand doesn't clip through blocks.
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();
		GlStateManager.scalef(1.0F, 1.0F, DEPTH);
		Project.gluPerspective(accessor.lucerna$getFov(tickDelta, false), (float) minecraft.width / minecraft.height, 0.05F, accessor.lucerna$getRenderDistance() * 2.0F);

		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();

		accessor.lucerna$applyHurtCam(tickDelta);

		if (minecraft.options.viewBobbing) {
			accessor.lucerna$applyViewBobbing(tickDelta);
		}

		gameRenderer.enableLightMap();
		gameRenderer.itemInHandRenderer.renderInFirstPerson(tickDelta);
		gameRenderer.disableLightMap();

		GlStateManager.popMatrix();
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.popMatrix();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);

		minecraft.profiler.pop();

		pipeline.setPhase(WorldRenderingPhase.NONE);

		ACTIVE = false;
	}

	public boolean isActive() {
		return ACTIVE;
	}
}
