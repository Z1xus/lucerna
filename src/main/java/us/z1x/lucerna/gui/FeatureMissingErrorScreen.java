package us.z1x.lucerna.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;

import java.util.List;

public class FeatureMissingErrorScreen extends Screen {
	private final Screen parent;
	private final String title;
	private final String message;
	private List<String> lines;

	public FeatureMissingErrorScreen(Screen parent, String title, String message) {
		this.parent = parent;
		this.title = title;
		this.message = message;
	}

	@Override
	public void init() {
		lines = textRenderer.split(message, width - 50);
		buttons.add(new ButtonWidget(0, width / 2 - 100, Math.min(height / 2 + lines.size() * textRenderer.fontHeight / 2 + 20, height - 30), I18n.translate("gui.back")));
	}

	@Override
	protected void buttonClicked(ButtonWidget button) {
		minecraft.openScreen(parent);
	}

	@Override
	public void render(int mouseX, int mouseY, float tickDelta) {
		renderBackground();
		drawCenteredString(textRenderer, title, width / 2, height / 2 - lines.size() * textRenderer.fontHeight / 2 - 20, 0xFFFFFF);

		int y = height / 2 - lines.size() * textRenderer.fontHeight / 2;

		for (String line : lines) {
			drawCenteredString(textRenderer, line, width / 2, y, 0xFFFFFF);
			y += textRenderer.fontHeight;
		}

		super.render(mouseX, mouseY, tickDelta);
	}
}
