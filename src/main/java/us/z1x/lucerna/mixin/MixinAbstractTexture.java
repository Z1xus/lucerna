package us.z1x.lucerna.mixin;

import us.z1x.lucerna.texture.TextureTracker;
import net.minecraft.client.render.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractTexture.class)
public class MixinAbstractTexture {
    @Inject(method = "getGlId", at = @At("RETURN"))
    private void lucerna$trackTexture(CallbackInfoReturnable<Integer> cir) {
        TextureTracker.INSTANCE.trackTexture(cir.getReturnValueI(), (AbstractTexture) (Object) this);
    }
}
