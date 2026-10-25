package us.z1x.lucerna.uniforms;

import us.z1x.lucerna.gl.uniform.UniformHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;

import java.util.Objects;

import static us.z1x.lucerna.gl.uniform.UniformUpdateFrequency.PER_TICK;

public final class WorldTimeUniforms {
    private WorldTimeUniforms() {
    }

    /**
     * Makes world time uniforms available to the given program
     *
     * @param uniforms the program to make the uniforms available to
     */
    public static void addWorldTimeUniforms(UniformHolder uniforms) {
        uniforms
            .uniform1i(PER_TICK, "worldTime", WorldTimeUniforms::getWorldDayTime)
            .uniform1i(PER_TICK, "worldDay", WorldTimeUniforms::getWorldDay)
            .uniform1i(PER_TICK, "moonPhase", () -> getWorld().getMoonPhase());
    }

    static int getWorldDayTime() {
        return (int) (getWorld().getTimeOfDay() % 24000L);
    }

    private static int getWorldDay() {
        long timeOfDay = getWorld().getTimeOfDay();
        long day = timeOfDay / 24000L;

        return (int) day;
    }

    private static ClientWorld getWorld() {
        return Objects.requireNonNull(Minecraft.getInstance().world);
    }
}
