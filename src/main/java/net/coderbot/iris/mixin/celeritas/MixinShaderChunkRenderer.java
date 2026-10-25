package net.coderbot.iris.mixin.celeritas;

import net.coderbot.iris.compat.celeritas.IrisChunkProgramOverrides;
import org.embeddedt.embeddium.impl.gl.device.CommandList;
import org.embeddedt.embeddium.impl.gl.shader.GlProgram;
import org.embeddedt.embeddium.impl.render.chunk.ShaderChunkRenderer;
import org.embeddedt.embeddium.impl.render.chunk.shader.ChunkShaderInterface;
import org.embeddedt.embeddium.impl.render.chunk.shader.ChunkShaderOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Overrides the chunk programs in {@link ShaderChunkRenderer} with the shader pack's terrain programs.
 */
@Mixin(value = ShaderChunkRenderer.class, remap = false)
public class MixinShaderChunkRenderer {
	@Unique
	private final IrisChunkProgramOverrides iris$overrides = new IrisChunkProgramOverrides();

	@Inject(method = "compileProgram", at = @At("HEAD"), cancellable = true)
	@SuppressWarnings("unchecked")
	private void iris$useOverride(ChunkShaderOptions options, CallbackInfoReturnable<GlProgram<ChunkShaderInterface>> cir) {
		var override = iris$overrides.getProgramOverride(options.pass());

		if (override != null) {
			cir.setReturnValue((GlProgram<ChunkShaderInterface>) (GlProgram<?>) override);
		}
	}

	@Inject(method = "delete", at = @At("HEAD"))
	private void iris$onDelete(CommandList commandList, CallbackInfo ci) {
		iris$overrides.deleteShaders();
	}
}
