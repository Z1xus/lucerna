package us.z1x.lucerna.gui.option;

import us.z1x.lucerna.Iris;
import org.embeddedt.embeddium.impl.gui.framework.TextComponent;
import org.taumc.celeritas.api.options.OptionIdentifier;
import org.taumc.celeritas.api.options.control.ControlValueFormatter;
import org.taumc.celeritas.api.options.control.SliderControl;
import org.taumc.celeritas.api.options.structure.OptionGroup;
import org.taumc.celeritas.api.options.structure.OptionImpact;
import org.taumc.celeritas.api.options.structure.OptionImpl;
import org.taumc.celeritas.api.options.structure.OptionPage;
import org.taumc.celeritas.api.options.structure.OptionStorage;

import java.io.IOException;
import java.util.Collections;

public final class IrisOptionPage {
    private static final OptionStorage<Void> STORAGE = new OptionStorage<Void>() {
        @Override
        public Void getData() {
            return null;
        }

        @Override
        public void save() {
            try {
                Iris.getIrisConfig().save();
                Iris.reloadInBackground();
            } catch (IOException e) {
                Iris.logger.error("Failed to apply the shader video settings", e);
            }
        }
    };

    private IrisOptionPage() {
    }

    public static OptionPage create() {
        OptionGroup group = OptionGroup.createBuilder()
            .setId(OptionIdentifier.create(Iris.MODID, "shaders"))
            .add(OptionImpl.createBuilder(int.class, STORAGE)
                .setId(OptionIdentifier.create(Iris.MODID, "shadow_distance", int.class))
                .setName(TextComponent.translatable("options.iris.shadowDistance"))
                .setTooltip(TextComponent.translatable("options.iris.shadowDistance.tooltip"))
                .setControl(option -> new SliderControl(option, 0, 32, 1, value -> TextComponent.translatable("options.chunks", value)))
                .setBinding((data, value) -> IrisVideoSettings.shadowDistance = value, data -> IrisVideoSettings.shadowDistance)
                .setEnabledPredicate(IrisVideoSettings::isShadowDistanceSliderEnabled)
                .setImpact(OptionImpact.HIGH)
                .build())
            .add(OptionImpl.createBuilder(int.class, STORAGE)
                .setId(OptionIdentifier.create(Iris.MODID, "render_quality", int.class))
                .setName(TextComponent.translatable("options.iris.renderQuality"))
                .setTooltip(TextComponent.translatable("options.iris.renderQuality.tooltip"))
                .setControl(option -> new SliderControl(option, 25, 200, 5, ControlValueFormatter.percentage()))
                .setBinding((data, value) -> IrisVideoSettings.renderQuality = value / 100.0F, data -> Math.round(IrisVideoSettings.renderQuality * 100.0F))
                .setImpact(OptionImpact.HIGH)
                .build())
            .add(OptionImpl.createBuilder(int.class, STORAGE)
                .setId(OptionIdentifier.create(Iris.MODID, "shadow_quality", int.class))
                .setName(TextComponent.translatable("options.iris.shadowQuality"))
                .setTooltip(TextComponent.translatable("options.iris.shadowQuality.tooltip"))
                .setControl(option -> new SliderControl(option, 25, 200, 5, ControlValueFormatter.percentage()))
                .setBinding((data, value) -> IrisVideoSettings.shadowQuality = value / 100.0F, data -> Math.round(IrisVideoSettings.shadowQuality * 100.0F))
                .setImpact(OptionImpact.HIGH)
                .build())
            .build();

        return new OptionPage(OptionIdentifier.create(Iris.MODID, "shaders"), TextComponent.translatable("options.iris.shaders"), Collections.singletonList(group));
    }
}
