package us.z1x.lucerna.gui.element.widget;

import us.z1x.lucerna.shaderpack.option.menu.OptionMenuElement;

import java.util.Optional;

public abstract class CommentedElementWidget<T extends OptionMenuElement> extends AbstractElementWidget<T> {
	public CommentedElementWidget(T element) {
		super(element);
	}

	public abstract Optional<String> getCommentTitle();

	public abstract Optional<String> getCommentBody();
}
