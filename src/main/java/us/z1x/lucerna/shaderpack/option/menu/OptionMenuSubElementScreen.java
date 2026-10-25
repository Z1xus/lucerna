package us.z1x.lucerna.shaderpack.option.menu;

import us.z1x.lucerna.shaderpack.ShaderProperties;
import us.z1x.lucerna.shaderpack.option.ShaderPackOptions;

import java.util.List;
import java.util.Optional;

public class OptionMenuSubElementScreen extends OptionMenuElementScreen {
	public final String screenId;

	public OptionMenuSubElementScreen(String screenId, OptionMenuContainer container, ShaderProperties shaderProperties, ShaderPackOptions shaderPackOptions, List<String> elementStrings, Optional<Integer> columnCount) {
		super(container, shaderProperties, shaderPackOptions, elementStrings, columnCount);

		this.screenId = screenId;
	}
}
