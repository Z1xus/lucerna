package net.coderbot.iris.compat.argentum;

import dev.rdh.argentum.impl.Argentum;
import dev.rdh.argentum.impl.config.ArgentumConfig;

/**
 * Turns off the Argentum renderers that draw world geometry with their own programs while a shader pack is loaded,
 * so that the geometry goes through the shader pack's gbuffer programs instead.
 */
public final class ArgentumCompat {
	private static boolean applied;
	private static boolean entityInstancing;
	private static boolean nameTagBatching;
	private static boolean fasterClouds;
	private static boolean fasterWeather;
	private static boolean fasterEndPortals;

	private ArgentumCompat() {
	}

	public static void setShadersActive(boolean active) {
		ArgentumConfig config = Argentum.CONFIG;

		if (active == applied || config == null) {
			return;
		}

		if (active) {
			entityInstancing = config.entityInstancing;
			nameTagBatching = config.nameTagBatching;
			fasterClouds = config.fasterClouds;
			fasterWeather = config.fasterWeather;
			fasterEndPortals = config.fasterEndPortals;
			disable(config);
		} else {
			restore(config);
		}

		applied = active;
	}

	public static void beforeSave() {
		if (applied) {
			restore(Argentum.CONFIG);
		}
	}

	public static void afterSave() {
		if (applied) {
			ArgentumConfig config = Argentum.CONFIG;
			entityInstancing = config.entityInstancing;
			nameTagBatching = config.nameTagBatching;
			fasterClouds = config.fasterClouds;
			fasterWeather = config.fasterWeather;
			fasterEndPortals = config.fasterEndPortals;
			disable(config);
		}
	}

	private static void disable(ArgentumConfig config) {
		config.entityInstancing = false;
		config.nameTagBatching = false;
		config.fasterClouds = false;
		config.fasterWeather = false;
		config.fasterEndPortals = false;
	}

	private static void restore(ArgentumConfig config) {
		config.entityInstancing = entityInstancing;
		config.nameTagBatching = nameTagBatching;
		config.fasterClouds = fasterClouds;
		config.fasterWeather = fasterWeather;
		config.fasterEndPortals = fasterEndPortals;
	}
}
