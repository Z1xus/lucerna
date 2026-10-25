package us.z1x.lucerna.shaderpack.option.values;

import us.z1x.lucerna.Iris;
import us.z1x.lucerna.shaderpack.OptionalBoolean;
import us.z1x.lucerna.shaderpack.option.OptionSet;

import java.util.Optional;

public interface OptionValues {
    OptionalBoolean getBooleanValue(String name);
    Optional<String> getStringValue(String name);

    default boolean getBooleanValueOrDefault(String name) {
        return getBooleanValue(name).orElseGet(() -> {
            if (!getOptionSet().getBooleanOptions().containsKey(name)) {
                Iris.logger.warn("Tried to get boolean value for unknown option: " + name + ", defaulting to true!");
                return true;
            }
            return getOptionSet().getBooleanOptions().get(name).getOption().getDefaultValue();
        });
    }

    default String getStringValueOrDefault(String name) {
        return getStringValue(name).orElseGet(() -> getOptionSet().getStringOptions().get(name).getOption().getDefaultValue());
    }

    int getOptionsChanged();

    MutableOptionValues mutableCopy();
    ImmutableOptionValues toImmutable();
    OptionSet getOptionSet();
}
