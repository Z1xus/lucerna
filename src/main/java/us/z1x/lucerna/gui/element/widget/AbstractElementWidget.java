package us.z1x.lucerna.gui.element.widget;

import us.z1x.lucerna.gui.NavigationController;
import us.z1x.lucerna.gui.screen.ShaderPackScreen;
import us.z1x.lucerna.shaderpack.option.menu.OptionMenuElement;

public abstract class AbstractElementWidget<T extends OptionMenuElement> {
	protected final T element;

	public static final AbstractElementWidget<OptionMenuElement> EMPTY = new AbstractElementWidget<OptionMenuElement>(null) {
		@Override
		public void render(int x, int y, int width, int height, int mouseX, int mouseY, boolean hovered) {}
	};

	public AbstractElementWidget(T element) {
		this.element = element;
	}

	public void init(ShaderPackScreen screen, NavigationController navigation) {}

	public abstract void render(int x, int y, int width, int height, int mouseX, int mouseY, boolean hovered);

	public boolean mouseClicked(int mx, int my, int button) {
		return false;
	}

	public boolean mouseReleased(int mx, int my, int button) {
		return false;
	}
}
