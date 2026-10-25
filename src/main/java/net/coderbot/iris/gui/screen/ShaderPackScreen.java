package net.coderbot.iris.gui.screen;

import net.coderbot.iris.Iris;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShaderPackScreen extends Screen {
	private final Screen parent;
	private final List<String> packs = new ArrayList<>();

	public ShaderPackScreen(Screen parent) {
		this.parent = parent;
	}

	@Override
	public void init() {
		packs.clear();

		try {
			packs.addAll(Iris.getShaderpacksDirectoryManager().enumerate());
		} catch (IOException e) {
			Iris.logger.error("Failed to list shader packs", e);
		}

		buttons.add(new ButtonWidget(0, width / 2 - 100, 30, 200, 20, I18n.translate("options.iris.shaderPackSelection") + ": " + (Iris.getIrisConfig().areShadersEnabled() ? "ON" : "OFF")));

		for (int i = 0; i < packs.size(); i++) {
			buttons.add(new ButtonWidget(i + 2, width / 2 - 100, 56 + i * 22, 200, 20, packs.get(i)));
		}

		buttons.add(new ButtonWidget(1, width / 2 - 100, height - 28, I18n.translate("gui.done")));
	}

	@Override
	protected void buttonClicked(ButtonWidget button) {
		try {
			if (button.id == 0) {
				Iris.getIrisConfig().setShadersEnabled(!Iris.getIrisConfig().areShadersEnabled());
			} else if (button.id == 1) {
				minecraft.openScreen(parent);
				return;
			} else {
				Iris.getIrisConfig().setShaderPackName(packs.get(button.id - 2));
				Iris.getIrisConfig().setShadersEnabled(true);
			}

			Iris.getIrisConfig().save();
			Iris.reload();
		} catch (IOException e) {
			Iris.logger.error("Failed to apply shader pack settings", e);
		}

		buttons.clear();
		init();
	}

	@Override
	public void render(int mouseX, int mouseY, float tickDelta) {
		renderBackground();
		drawCenteredString(textRenderer, Iris.MODNAME + ": " + Iris.getCurrentPackName(), width / 2, 12, 0xFFFFFF);
		super.render(mouseX, mouseY, tickDelta);
	}
}
