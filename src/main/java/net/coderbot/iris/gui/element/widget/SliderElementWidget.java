package net.coderbot.iris.gui.element.widget;

import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuStringOptionElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Formatting;
import net.minecraft.util.math.MathHelper;

public class SliderElementWidget extends StringElementWidget {
	private static final int PREVIEW_SLIDER_WIDTH = 4;
	private static final int ACTIVE_SLIDER_WIDTH = 6;

	private boolean mouseDown = false;

	public SliderElementWidget(OptionMenuStringOptionElement element) {
		super(element);
	}

	@Override
	public void render(int x, int y, int width, int height, int mouseX, int mouseY, boolean hovered) {
		this.updateRenderParams(width, 35);

		if (!hovered) {
			this.renderOptionWithValue(x, y, width, height, false, (float)valueIndex / (valueCount - 1), PREVIEW_SLIDER_WIDTH);
		} else {
			this.renderSlider(x, y, width, height);
		}

		if (Screen.isShiftDown()) {
			renderTooltip(Formatting.GREEN + I18n.translate("options.iris.setToDefault"), mouseX, mouseY, hovered);
		} else if (!this.screen.isDisplayingComment()) {
			renderTooltip(this.unmodifiedLabel, mouseX, mouseY, hovered);
		}

		if (this.mouseDown) {
			// Release if the mouse went off the slider
			if (!hovered) {
				this.onReleased();
			}

			whileDragging(x, width, mouseX);
		}
	}

	private void renderSlider(int x, int y, int width, int height) {
		GuiUtil.bindIrisWidgetsTexture();

		// Draw background button
		GuiUtil.drawButton(x, y, width, height, false, false);
		// Draw slider area
		GuiUtil.drawButton(x + 2, y + 2, width - 4, height - 4, false, true);

		// Range of x values the slider can occupy
		int sliderSpace = (width - 8) - ACTIVE_SLIDER_WIDTH;
		// Position of slider
		int sliderPos = (x + 4) + (int)(((float)valueIndex / (valueCount - 1)) * sliderSpace);
		// Draw slider
		GuiUtil.drawButton(sliderPos, y + 4, ACTIVE_SLIDER_WIDTH, height - 8, this.mouseDown, false);

		// Draw value label
		TextRenderer font = Minecraft.getInstance().textRenderer;
		font.drawWithShadow(this.valueLabel, (int)(x + (width * 0.5)) - (int)(font.getWidth(this.valueLabel) * 0.5), y + 7, 0xFFFFFF);
	}

	private void whileDragging(int x, int width, int mouseX) {
		float mousePositionAcrossWidget = MathHelper.clamp((float)(mouseX - (x + 4)) / (width - 8), 0, 1);

		int newValueIndex = Math.min(valueCount - 1, (int)(mousePositionAcrossWidget * valueCount));

		if (valueIndex != newValueIndex) {
			this.valueIndex = newValueIndex;

			this.updateLabels();
		}
	}

	private void onReleased() {
		mouseDown = false;

		this.queue();
		this.navigation.refresh();

		GuiUtil.playButtonClickSound();
	}

	@Override
	public boolean mouseClicked(int mx, int my, int button) {
		if (button == 0) {
			if (Screen.isShiftDown()) {
				if (this.applyOriginalValue()) {
					this.navigation.refresh();
				}
				GuiUtil.playButtonClickSound();

				return true;
			}

			mouseDown = true;
			GuiUtil.playButtonClickSound();

			return true;
		}

		// Do not use base widget's button click behavior
		return false;
	}

	@Override
	public boolean mouseReleased(int mx, int my, int button) {
		if (button == 0) {
			this.onReleased();

			return true;
		}
		return super.mouseReleased(mx, my, button);
	}
}
