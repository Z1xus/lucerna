package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.pipeline.DeferredWorldRenderingPipeline;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.BufferUploader;
import net.minecraft.client.render.vertex.VertexFormat;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

@Mixin(BufferUploader.class)
public class MixinBufferUploader {
	// per vertex: mc_midTexCoord (vec2) and at_tangent (vec4)
	@Unique
	private static final int EXTRA_STRIDE = 24;
	@Unique
	private static ByteBuffer iris$extra = MemoryUtil.memAlloc(EXTRA_STRIDE * 4096);
	@Unique
	private static boolean iris$extraBound;
	@Unique
	private static final Vector3f[] P = { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
	@Unique
	private static final float[] U = new float[4];
	@Unique
	private static final float[] V = new float[4];
	@Unique
	private static final Vector3f EDGE1 = new Vector3f();
	@Unique
	private static final Vector3f EDGE2 = new Vector3f();
	@Unique
	private static final Vector3f TANGENT = new Vector3f();
	@Unique
	private static final Vector3f BITANGENT = new Vector3f();
	@Unique
	private static final Vector3f NORMAL = new Vector3f();

	@Inject(method = "end", at = @At("HEAD"))
	private void iris$beforeDraw(BufferBuilder builder, CallbackInfo ci) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline == null) {
			return;
		}

		// glUseProgram would be recorded into the display list and replayed in every later pass
		if (!CapturedRenderingState.INSTANCE.isCompilingDisplayList()) {
			pipeline.syncProgram();
		}

		if (pipeline instanceof DeferredWorldRenderingPipeline && builder.getDrawMode() == GL11C.GL_QUADS
				&& builder.getVertexCount() >= 4 && builder.getFormat().hasUv(0)) {
			iris$bindExtendedAttributes(builder);
		}
	}

	@Inject(method = "end", at = @At("RETURN"))
	private void iris$afterDraw(BufferBuilder builder, CallbackInfo ci) {
		if (iris$extraBound) {
			iris$extraBound = false;
			GL20C.glDisableVertexAttribArray(12);
			GL20C.glDisableVertexAttribArray(13);
			IrisRenderSystem.vertexAttrib4f(12, 0.0F, 0.0F, 0.0F, 1.0F);
			IrisRenderSystem.vertexAttrib4f(13, 1.0F, 0.0F, 0.0F, 1.0F);
		}
	}

	@Unique
	private static void iris$bindExtendedAttributes(BufferBuilder builder) {
		VertexFormat format = builder.getFormat();
		int stride = format.getVertexSize();
		int uv = format.getUvOffset(0);
		int vertices = builder.getVertexCount() & ~3;
		ByteBuffer src = builder.getBuffer();

		if (iris$extra.capacity() < vertices * EXTRA_STRIDE) {
			iris$extra = MemoryUtil.memRealloc(iris$extra, vertices * EXTRA_STRIDE * 2);
		}

		ByteBuffer out = iris$extra;
		Vector3f[] p = P;
		float[] u = U;
		float[] v = V;
		Vector3f edge1 = EDGE1;
		Vector3f edge2 = EDGE2;
		Vector3f tangent = TANGENT;
		Vector3f bitangent = BITANGENT;
		Vector3f normal = NORMAL;

		for (int quad = 0; quad < vertices; quad += 4) {
			float midU = 0.0F;
			float midV = 0.0F;

			for (int i = 0; i < 4; i++) {
				int base = (quad + i) * stride;
				p[i].set(src.getFloat(base), src.getFloat(base + 4), src.getFloat(base + 8));
				u[i] = src.getFloat(base + uv);
				v[i] = src.getFloat(base + uv + 4);
				midU += u[i] * 0.25F;
				midV += v[i] * 0.25F;
			}

			// same tangent math as iris NormalHelper.computeTangent
			p[1].sub(p[0], edge1);
			p[2].sub(p[0], edge2);
			float du1 = u[1] - u[0];
			float dv1 = v[1] - v[0];
			float du2 = u[2] - u[0];
			float dv2 = v[2] - v[0];
			float det = du1 * dv2 - du2 * dv1;
			float f = det == 0.0F ? 1.0F : 1.0F / det;

			edge1.mul(dv2 * f, tangent).fma(-dv1 * f, edge2);
			edge2.mul(du1 * f, bitangent).fma(-du2 * f, edge1);
			p[2].sub(p[0], normal).cross(p[3].x - p[1].x, p[3].y - p[1].y, p[3].z - p[1].z);

			float w = 1.0F;

			if (tangent.lengthSquared() > 0.0F && normal.lengthSquared() > 0.0F) {
				tangent.normalize();
				w = normal.cross(tangent).dot(bitangent) < 0.0F ? -1.0F : 1.0F;
			} else {
				tangent.set(1.0F, 0.0F, 0.0F);
			}

			for (int i = 0; i < 4; i++) {
				int base = (quad + i) * EXTRA_STRIDE;
				out.putFloat(base, midU).putFloat(base + 4, midV)
					.putFloat(base + 8, tangent.x).putFloat(base + 12, tangent.y).putFloat(base + 16, tangent.z).putFloat(base + 20, w);
			}
		}

		long address = MemoryUtil.memAddress(out, 0);
		GL20C.nglVertexAttribPointer(12, 2, GL11C.GL_FLOAT, false, EXTRA_STRIDE, address);
		GL20C.nglVertexAttribPointer(13, 4, GL11C.GL_FLOAT, false, EXTRA_STRIDE, address + 8);
		GL20C.glEnableVertexAttribArray(12);
		GL20C.glEnableVertexAttribArray(13);
		iris$extraBound = true;
	}
}
