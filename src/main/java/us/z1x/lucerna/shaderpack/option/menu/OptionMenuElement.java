package us.z1x.lucerna.shaderpack.option.menu;

import us.z1x.lucerna.shaderpack.ShaderProperties;
import us.z1x.lucerna.shaderpack.option.MergedBooleanOption;
import us.z1x.lucerna.shaderpack.option.MergedStringOption;
import us.z1x.lucerna.shaderpack.option.ShaderPackOptions;

import java.util.Map;

public abstract class OptionMenuElement {
    public static final OptionMenuElement EMPTY = new OptionMenuElement() {};

    private static final String ELEMENT_EMPTY = "<empty>";
    private static final String ELEMENT_PROFILE = "<profile>";

    public static OptionMenuElement create(String elementString, OptionMenuContainer container, ShaderProperties shaderProperties, ShaderPackOptions shaderPackOptions) throws IllegalArgumentException {
        if (ELEMENT_EMPTY.equals(elementString)) {
            return EMPTY;
        }
        // skip the profile element if there are no profiles
        if (ELEMENT_PROFILE.equals(elementString)) {
            // null drops the element instead of treating it as empty
            return container.getProfiles().size() > 0 ? new OptionMenuProfileElement(container.getProfiles(), shaderPackOptions.getOptionSet(), shaderPackOptions.getOptionValues()) : null;
        }
        if (elementString.startsWith("[") && elementString.endsWith("]")) {
            return new OptionMenuLinkElement(elementString.substring(1, elementString.length() - 1));
        }

        Map<String, MergedBooleanOption> booleanOptions = shaderPackOptions.getOptionSet().getBooleanOptions();
        Map<String, MergedStringOption> stringOptions = shaderPackOptions.getOptionSet().getStringOptions();

        if (booleanOptions.containsKey(elementString)) {
            return new OptionMenuBooleanOptionElement(elementString, container, shaderProperties, shaderPackOptions.getOptionValues(), booleanOptions.get(elementString).getOption());
        } else if (stringOptions.containsKey(elementString)) {
            return new OptionMenuStringOptionElement(elementString, container, shaderProperties, shaderPackOptions.getOptionValues(), stringOptions.get(elementString).getOption());
        }

        // the caller logs and skips it
        throw new IllegalArgumentException("Unable to resolve shader pack option menu element \"" + elementString + "\" defined in shaders.properties");
    }
}
