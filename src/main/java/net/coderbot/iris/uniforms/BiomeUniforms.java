package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_TICK;

public class BiomeUniforms {
	private static final Minecraft client = Minecraft.getInstance();

	public static void addBiomeUniforms(UniformHolder uniforms) {
		uniforms
			.uniform1i(PER_TICK, "biome", () -> {
				Biome biome = getBiome();
				return biome == null ? 0 : biome.id;
			})
			.uniform1i(PER_TICK, "biome_precipitation", () -> {
				Biome biome = getBiome();

				if (biome == null) {
					return 0;
				}

				return biome.isSnowy() ? 2 : biome.isRainy() ? 1 : 0;
			})
			.uniform1f(PER_TICK, "rainfall", () -> {
				Biome biome = getBiome();
				return biome == null ? 0.0F : biome.getDownfall();
			})
			.uniform1f(PER_TICK, "temperature", () -> {
				Biome biome = getBiome();
				return biome == null ? 0.0F : biome.temperature;
			});
	}

	private static Biome getBiome() {
		if (client.world == null || client.player == null) {
			return null;
		}

		return client.world.getBiome(new BlockPos(client.player));
	}
}
