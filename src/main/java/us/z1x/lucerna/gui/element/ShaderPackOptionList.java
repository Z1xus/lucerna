package us.z1x.lucerna.gui.element;

import us.z1x.lucerna.Iris;
import us.z1x.lucerna.gui.GuiUtil;
import us.z1x.lucerna.gui.NavigationController;
import us.z1x.lucerna.gui.element.widget.AbstractElementWidget;
import us.z1x.lucerna.gui.element.widget.OptionMenuConstructor;
import us.z1x.lucerna.gui.screen.ShaderPackScreen;
import us.z1x.lucerna.shaderpack.ShaderPack;
import us.z1x.lucerna.shaderpack.option.menu.OptionMenuContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ShaderPackOptionList extends IrisObjectSelectionList<ShaderPackOptionList.BaseEntry> {
	private final List<AbstractElementWidget<?>> elementWidgets = new ArrayList<>();
	private final ShaderPackScreen screen;
	private final NavigationController navigation;
	private OptionMenuContainer container;

	public ShaderPackOptionList(ShaderPackScreen screen, NavigationController navigation, ShaderPack pack, Minecraft client, int width, int height, int top, int bottom, int left, int right) {
		super(client, width, height, top, bottom, left, right, 24);
		this.navigation = navigation;
		this.screen = screen;

		applyShaderPack(pack);
	}

	public void applyShaderPack(ShaderPack pack) {
		this.container = pack.getMenuContainer();
	}

	public void rebuild() {
		this.clearEntries();
		this.setScrollAmount(0);
		OptionMenuConstructor.constructAndApplyToScreen(this.container, this.screen, this, navigation);
	}

	public void refresh() {
		this.elementWidgets.forEach(widget -> widget.init(this.screen, this.navigation));
	}

	@Override
	public int getRowWidth() {
		return Math.min(400, width - 12);
	}

	public void addHeader(String text, boolean backButton) {
		this.addEntry(new HeaderEntry(this.screen, this.navigation, text, backButton));
	}

	public void addWidgets(int columns, List<AbstractElementWidget<?>> elements) {
		this.elementWidgets.addAll(elements);

		List<AbstractElementWidget<?>> row = new ArrayList<>();
		for (AbstractElementWidget<?> element : elements) {
			row.add(element);

			if (row.size() >= columns) {
				this.addEntry(new ElementRowEntry(screen, this.navigation, row));
				row = new ArrayList<>(); // Clearing the list would affect the row entry created above
			}
		}

		if (row.size() > 0) {
			while (row.size() < columns) {
				row.add(AbstractElementWidget.EMPTY);
			}

			this.addEntry(new ElementRowEntry(screen, this.navigation, row));
		}
	}

	public abstract static class BaseEntry extends IrisObjectSelectionList.Entry {
		protected final NavigationController navigation;

		protected BaseEntry(NavigationController navigation) {
			this.navigation = navigation;
		}
	}

	public static class HeaderEntry extends BaseEntry {
		private static final int MIN_SIDE_BUTTON_WIDTH = 42;
		private static final int BUTTON_HEIGHT = 16;

		private final String resetButtonTextInactive = Formatting.GRAY + I18n.translate("options.iris.reset");
		private final String resetButtonTextActive = Formatting.YELLOW + I18n.translate("options.iris.reset");
		private final String resetHoldShiftTooltip = Formatting.GOLD + I18n.translate("options.iris.reset.tooltip.holdShift");
		private final String resetTooltip = Formatting.RED + I18n.translate("options.iris.reset.tooltip");

		private final ShaderPackScreen screen;
		private final @Nullable IrisElementRow backButton;
		private final IrisElementRow utilityButtons = new IrisElementRow();
		private final IrisElementRow.TextButtonElement resetButton;
		private final String text;

		public HeaderEntry(ShaderPackScreen screen, NavigationController navigation, String text, boolean hasBackButton) {
			super(navigation);

			if (hasBackButton) {
				String backButtonText = "< " + Formatting.ITALIC + I18n.translate("options.iris.back");

				this.backButton = new IrisElementRow().add(
						new IrisElementRow.TextButtonElement(backButtonText, this::backButtonClicked),
						Math.max(MIN_SIDE_BUTTON_WIDTH, Minecraft.getInstance().textRenderer.getWidth(backButtonText) + 8)
				);
			} else {
				this.backButton = null;
			}

			this.resetButton = new IrisElementRow.TextButtonElement(
					resetButtonTextInactive, this::resetButtonClicked);

			this.utilityButtons
					.add(this.resetButton, Math.max(MIN_SIDE_BUTTON_WIDTH, Minecraft.getInstance().textRenderer.getWidth(resetButtonTextInactive) + 8));

			this.screen = screen;
			this.text = text;
		}

		@Override
		public void render(int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered) {
			// Draw dividing line
			fill(x - 3, (y + entryHeight) - 2, x + entryWidth, (y + entryHeight) - 1, 0x66BEBEBE);

			TextRenderer font = Minecraft.getInstance().textRenderer;

			// Draw header text
			drawCenteredString(font, text, x + (int)(entryWidth * 0.5), y + 5, 0xFFFFFF);

			GuiUtil.bindIrisWidgetsTexture();

			// Draw back button if present
			if (this.backButton != null) {
				backButton.render(x, y, BUTTON_HEIGHT, mouseX, mouseY, hovered);
			}

			boolean shiftDown = Screen.isShiftDown();

			// Set the appearance of the reset button
			this.resetButton.disabled = !shiftDown;
			this.resetButton.text = shiftDown ? resetButtonTextActive : resetButtonTextInactive;

			// Draw the utility buttons
			this.utilityButtons.renderRightAligned((x + entryWidth) - 3, y, BUTTON_HEIGHT, mouseX, mouseY, hovered);

			// Draw the reset button's tooltip
			if (this.resetButton.isHovered()) {
				String tooltip = shiftDown ? resetTooltip : resetHoldShiftTooltip;
				queueBottomRightAnchoredTooltip(mouseX, mouseY, font, tooltip);
			}
		}

		private void queueBottomRightAnchoredTooltip(int x, int y, TextRenderer font, String text) {
			ShaderPackScreen.TOP_LAYER_RENDER_QUEUE.add(() -> GuiUtil.drawTextPanel(
					font, text,
					x - (font.getWidth(text) + 10), y - 16
			));
		}

		@Override
		public boolean mouseClicked(int mouseX, int mouseY, int button) {
			boolean backButtonResult = backButton != null && backButton.mouseClicked(mouseX, mouseY, button);
			boolean utilButtonResult = utilityButtons.mouseClicked(mouseX, mouseY, button);

			return backButtonResult || utilButtonResult;
		}

		private boolean backButtonClicked(IrisElementRow.TextButtonElement button) {
			this.navigation.back();
			GuiUtil.playButtonClickSound();

			return true;
		}

		private boolean resetButtonClicked(IrisElementRow.TextButtonElement button) {
			if (Screen.isShiftDown()) {
				Iris.resetShaderPackOptionsOnNextReload();
				this.screen.applyChanges();
				GuiUtil.playButtonClickSound();

				return true;
			}

			return false;
		}
	}

	public static class ElementRowEntry extends BaseEntry {
		private final List<AbstractElementWidget<?>> widgets;
		private final ShaderPackScreen screen;

		private int cachedWidth;
		private int cachedPosX;

		public ElementRowEntry(ShaderPackScreen screen, NavigationController navigation, List<AbstractElementWidget<?>> widgets) {
			super(navigation);

			this.screen = screen;
			this.widgets = widgets;
		}

		@Override
		public void render(int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered) {
			this.cachedWidth = entryWidth;
			this.cachedPosX = x;

			// The amount of space widgets will occupy, excluding margins. Will be divided up between widgets.
			int totalWidthWithoutMargins = entryWidth - (2 * (widgets.size() - 1));

			totalWidthWithoutMargins -= 3; // Centers it for some reason

			// Width of a single widget
			float singleWidgetWidth = (float) totalWidthWithoutMargins / widgets.size();

			for (int i = 0; i < widgets.size(); i++) {
				AbstractElementWidget<?> widget = widgets.get(i);
				boolean widgetHovered = hovered && (getHoveredWidget(mouseX) == i);
				widget.render(x + (int)((singleWidgetWidth + 2) * i), y, (int) singleWidgetWidth, entryHeight + 2, mouseX, mouseY, widgetHovered);

				screen.setElementHoveredStatus(widget, widgetHovered);
			}
		}

		public int getHoveredWidget(int mouseX) {
			float positionAcrossWidget = ((float) MathHelper.clamp(mouseX - cachedPosX, 0, cachedWidth)) / cachedWidth;

			return MathHelper.clamp((int) Math.floor(widgets.size() * positionAcrossWidget), 0, widgets.size() - 1);
		}

		@Override
		public boolean mouseClicked(int mouseX, int mouseY, int button) {
			return this.widgets.get(getHoveredWidget(mouseX)).mouseClicked(mouseX, mouseY, button);
		}

		@Override
		public boolean mouseReleased(int mouseX, int mouseY, int button) {
			return this.widgets.get(getHoveredWidget(mouseX)).mouseReleased(mouseX, mouseY, button);
		}
	}
}
