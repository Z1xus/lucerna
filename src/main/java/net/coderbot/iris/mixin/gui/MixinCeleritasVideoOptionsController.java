package net.coderbot.iris.mixin.gui;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.option.IrisOptionPage;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.minecraft.client.Minecraft;
import org.embeddedt.embeddium.impl.gui.CeleritasVideoOptionsController;
import org.embeddedt.embeddium.impl.gui.frame.tab.Tab;
import org.embeddedt.embeddium.impl.gui.framework.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.taumc.celeritas.api.options.OptionIdentifier;
import org.taumc.celeritas.api.options.structure.OptionPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adds the shader video settings page and a "Shader Packs..." tab, which opens the shader pack screen instead of
 * selecting a page, to the Celeritas video settings that Argentum shows.
 */
@Mixin(value = CeleritasVideoOptionsController.class, remap = false)
public class MixinCeleritasVideoOptionsController {
	@ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
	private static List<OptionPage> lucerna$addOptionPage(List<OptionPage> pages) {
		List<OptionPage> withShaders = new ArrayList<>(pages);
		withShaders.add(IrisOptionPage.create());

		return withShaders;
	}

	@Inject(method = "createExtraTabs", at = @At("HEAD"))
	private void lucerna$addShaderPackTab(Map<String, List<Tab<?>>> tabs, CallbackInfo ci) {
		tabs.computeIfAbsent(Iris.MODID, id -> new ArrayList<>()).add(Tab.createBuilder()
				.setId(OptionIdentifier.create(Iris.MODID, "shader_packs"))
				.setTitle(TextComponent.translatable("options.iris.shaderPackSelection"))
				.setOnSelectFunction(() -> {
					Minecraft minecraft = Minecraft.getInstance();
					minecraft.openScreen(new ShaderPackScreen(minecraft.screen));

					return false;
				})
				.build());
	}
}
