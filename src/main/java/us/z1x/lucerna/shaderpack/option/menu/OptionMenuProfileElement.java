package us.z1x.lucerna.shaderpack.option.menu;

import us.z1x.lucerna.Iris;
import us.z1x.lucerna.shaderpack.option.OptionSet;
import us.z1x.lucerna.shaderpack.option.ProfileSet;
import us.z1x.lucerna.shaderpack.option.values.MutableOptionValues;
import us.z1x.lucerna.shaderpack.option.values.OptionValues;

public class OptionMenuProfileElement extends OptionMenuElement {
    public final ProfileSet profiles;
    public final OptionSet options;

    private final OptionValues packAppliedValues;

    public OptionMenuProfileElement(ProfileSet profiles, OptionSet options, OptionValues packAppliedValues) {
        this.profiles = profiles;
        this.options = options;
        this.packAppliedValues = packAppliedValues;
    }

    /**
     * @return an {@link OptionValues} that also contains values currently
     * pending application.
     */
    public OptionValues getPendingOptionValues() {
        MutableOptionValues values = packAppliedValues.mutableCopy();
        values.addAll(Iris.getShaderPackOptionQueue());

        return values;
    }
}
