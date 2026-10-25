package net.coderbot.iris.gui.element;

import net.coderbot.iris.gui.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.TextRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Intended to make very simple rows of buttons easier to make
 */
public class IrisElementRow {
	private final Map<Element, Integer> elements = new HashMap<>();
	private final List<Element> orderedElements = new ArrayList<>();
	private final int spacing;
	private int x;
	private int y;
	private int width;
	private int height;

	public IrisElementRow(int spacing) {
		this.spacing = spacing;
	}

	public IrisElementRow() {
		this(1);
	}

	/**
	 * Adds an element to the right of this row.
	 *
	 * @param element The element to add
	 * @param width The width of the element in this row
	 * @return {@code this}, to be used for chaining statements
	 */
	public IrisElementRow add(Element element, int width) {
		if (!this.orderedElements.contains(element)) {
			this.orderedElements.add(element);
		}
		this.elements.put(element, width);

		this.width += width + this.spacing;

		return this;
	}

	/**
	 * Modifies the width of an element.
	 *
	 * @param element The element whose width to modify
	 * @param width The width to be assigned to the specified element
	 */
	public void setWidth(Element element, int width) {
		if (!this.elements.containsKey(element)) {
			return;
		}

		this.width -= this.elements.get(element) + 2;

		add(element, width);
	}

	/**
	 * Renders the row, with the anchor point being the top left.
	 */
	public void render(int x, int y, int height, int mouseX, int mouseY, boolean rowHovered) {
		this.x = x;
		this.y = y;
		this.height = height;

		int currentX = x;

		for (Element element : this.orderedElements) {
			int currentWidth = this.elements.get(element);

			element.render(currentX, y, currentWidth, height, mouseX, mouseY,
					rowHovered && sectionHovered(currentX, currentWidth, mouseX, mouseY));

			currentX += currentWidth + this.spacing;
		}
	}

	/**
	 * Renders the row, with the anchor point being the top right.
	 */
	public void renderRightAligned(int x, int y, int height, int mouseX, int mouseY, boolean hovered) {
		render(x - this.width, y, height, mouseX, mouseY, hovered);
	}

	private boolean sectionHovered(int sectionX, int sectionWidth, int mx, int my) {
		return mx > sectionX && mx < sectionX + sectionWidth &&
				my > this.y && my < this.y + this.height;
	}

	private Optional<Element> getHovered(int mx, int my) {
		int currentX = this.x;

		for (Element element : this.orderedElements) {
			int currentWidth = this.elements.get(element);

			if (sectionHovered(currentX, currentWidth, mx, my)) {
				return Optional.of(element);
			}

			currentX += currentWidth + this.spacing;
		}

		return Optional.empty();
	}

	public boolean mouseClicked(int mx, int my, int button) {
		return getHovered(mx, my).map(element -> element.mouseClicked(mx, my, button)).orElse(false);
	}

	public static abstract class Element {
		public boolean disabled = false;
		private boolean hovered = false;

		public void render(int x, int y, int width, int height, int mouseX, int mouseY, boolean hovered) {
			GuiUtil.bindIrisWidgetsTexture();
			GuiUtil.drawButton(x, y, width, height, hovered, this.disabled);

			this.hovered = hovered;
			this.renderLabel(x, y, width, height, mouseX, mouseY, hovered);
		}

		public abstract void renderLabel(int x, int y, int width, int height, int mouseX, int mouseY, boolean hovered);

		public boolean mouseClicked(int mx, int my, int button) {
			return false;
		}

		public boolean isHovered() {
			return hovered;
		}
	}

	public static abstract class ButtonElement<T extends ButtonElement<T>> extends Element {
		private final Function<T, Boolean> onClick;

		protected ButtonElement(Function<T, Boolean> onClick) {
			this.onClick = onClick;
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean mouseClicked(int mx, int my, int button) {
			if (this.disabled) {
				return false;
			}

			if (button == 0) {
				return this.onClick.apply((T) this);
			}

			return super.mouseClicked(mx, my, button);
		}
	}

	/**
	 * A clickable button element that uses a {@link net.coderbot.iris.gui.GuiUtil.Icon} as its label.
	 */
	public static class IconButtonElement extends ButtonElement<IconButtonElement> {
		public GuiUtil.Icon icon;

		public IconButtonElement(GuiUtil.Icon icon, Function<IconButtonElement, Boolean> onClick) {
			super(onClick);
			this.icon = icon;
		}

		@Override
		public void renderLabel(int x, int y, int width, int height, int mouseX, int mouseY, boolean hovered) {
			int iconX = x + (int)((width - this.icon.getWidth()) * 0.5);
			int iconY = y + (int)((height - this.icon.getHeight()) * 0.5);

			GuiUtil.bindIrisWidgetsTexture();
			this.icon.draw(iconX, iconY);
		}
	}

	/**
	 * A clickable button element that uses a text as its label.
	 */
	public static class TextButtonElement extends ButtonElement<TextButtonElement> {
		protected final TextRenderer font;
		public String text;

		public TextButtonElement(String text, Function<TextButtonElement, Boolean> onClick) {
			super(onClick);

			this.font = Minecraft.getInstance().textRenderer;
			this.text = text;
		}

		@Override
		public void renderLabel(int x, int y, int width, int height, int mouseX, int mouseY, boolean hovered) {
			int textX = x + (int)((width - this.font.getWidth(this.text)) * 0.5);
			int textY = y + (int)((height - 8) * 0.5);

			this.font.drawWithShadow(this.text, textX, textY, 0xFFFFFF);
		}
	}
}
