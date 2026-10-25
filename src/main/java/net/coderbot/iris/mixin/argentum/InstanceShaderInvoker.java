package net.coderbot.iris.mixin.argentum;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "dev.rdh.argentum.impl.render.entity.instancing.InstanceShader", remap = false)
public interface InstanceShaderInvoker {
	@Invoker("initialize")
	void lucerna$initialize();
}
