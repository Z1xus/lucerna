package us.z1x.lucerna.shaderpack.option.menu;

import us.z1x.lucerna.shaderpack.ShaderProperties;
import us.z1x.lucerna.shaderpack.option.StringOption;
import us.z1x.lucerna.shaderpack.option.values.OptionValues;

public class OptionMenuStringOptionElement extends OptionMenuOptionElement {
	public final StringOption option;

	public OptionMenuStringOptionElement(String elementString, OptionMenuContainer container, ShaderProperties shaderProperties, OptionValues values, StringOption option) {
		super(elementString, container, shaderProperties, values);
		this.option = option;
	}
}
