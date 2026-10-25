package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector4f;

public class CapturedRenderingState {
	public static final CapturedRenderingState INSTANCE = new CapturedRenderingState();

	private static final Vector3d ZERO_VECTOR_3d = new Vector3d();

	private final Matrix4f gbufferModelView = new Matrix4f();
	private final Matrix4f gbufferProjection = new Matrix4f();
	private Vector3d fogColor;
	private float tickDelta;
	private int currentRenderedBlockEntity;
	private Runnable blockEntityIdListener = null;

	private int currentRenderedEntity = -1;
	private Runnable entityIdListener = null;

	private final Vector4f currentEntityColor = new Vector4f();
	private Runnable entityColorListener = null;

	private CapturedRenderingState() {
	}

	public Matrix4f getGbufferModelView() {
		return gbufferModelView;
	}

	public void setGbufferModelView(Matrix4f gbufferModelView) {
		this.gbufferModelView.set(gbufferModelView);
	}

	public Matrix4f getGbufferProjection() {
		return gbufferProjection;
	}

	public void setGbufferProjection(Matrix4f gbufferProjection) {
		this.gbufferProjection.set(gbufferProjection);
	}

	public Vector3d getFogColor() {
		if (Minecraft.getInstance().world == null || fogColor == null) {
			return ZERO_VECTOR_3d;
		}

		return fogColor;
	}

	public void setFogColor(float red, float green, float blue) {
		fogColor = new Vector3d(red, green, blue);
	}

	public void setTickDelta(float tickDelta) {
		this.tickDelta = tickDelta;
	}

	public float getTickDelta() {
		return tickDelta;
	}

	public void setCurrentBlockEntity(int entity) {
		this.currentRenderedBlockEntity = entity;

		if (this.blockEntityIdListener != null) {
			this.blockEntityIdListener.run();
		}
	}

	public int getCurrentRenderedBlockEntity() {
		return currentRenderedBlockEntity;
	}

	public void setCurrentEntity(int entity) {
		this.currentRenderedEntity = entity;

		if (this.entityIdListener != null) {
			this.entityIdListener.run();
		}
	}

	public void setCurrentEntityColor(float red, float green, float blue, float alpha) {
		this.currentEntityColor.set(red, green, blue, alpha);

		if (this.entityColorListener != null) {
			this.entityColorListener.run();
		}
	}

	public Vector4f getCurrentEntityColor() {
		return currentEntityColor;
	}

	public ValueUpdateNotifier getEntityColorNotifier() {
		return listener -> this.entityColorListener = listener;
	}

	public ValueUpdateNotifier getEntityIdNotifier() {
		return listener -> this.entityIdListener = listener;
	}

	public ValueUpdateNotifier getBlockEntityIdNotifier() {
		return listener -> this.blockEntityIdListener = listener;
	}

	public int getCurrentRenderedEntity() {
		return currentRenderedEntity;
	}
}
