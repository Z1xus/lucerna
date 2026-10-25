package net.coderbot.iris.compat.argentum;

import dev.rdh.argentum.impl.Argentum;
import dev.rdh.argentum.impl.config.ArgentumConfig;

// these argentum renderers draw with their own programs, so they are off while a shader pack is loaded
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
			disable(Argentum.CONFIG);
		}
	}

	private static void disable(ArgentumConfig config) {
		entityInstancing = config.entityInstancing;
		nameTagBatching = config.nameTagBatching;
		fasterClouds = config.fasterClouds;
		fasterWeather = config.fasterWeather;
		fasterEndPortals = config.fasterEndPortals;
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
