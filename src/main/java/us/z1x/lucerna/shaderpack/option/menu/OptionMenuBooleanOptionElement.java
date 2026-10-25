package us.z1x.lucerna.shaderpack.option.menu;

import us.z1x.lucerna.shaderpack.ShaderProperties;
import us.z1x.lucerna.shaderpack.option.BooleanOption;
import us.z1x.lucerna.shaderpack.option.values.OptionValues;

public class OptionMenuBooleanOptionElement extends OptionMenuOptionElement {
	public final BooleanOption option;

	public OptionMenuBooleanOptionElement(String elementString, OptionMenuContainer container, ShaderProperties shaderProperties, OptionValues values, BooleanOption option) {
		super(elementString, container, shaderProperties, values);
		this.option = option;
	}
}
