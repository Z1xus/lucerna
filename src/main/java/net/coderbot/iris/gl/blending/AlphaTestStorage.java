package net.coderbot.iris.gl.blending;

import net.minecraft.client.render.platform.GlStateManager;

public class AlphaTestStorage {
	private static boolean originalAlphaTestEnable;
	private static AlphaTest originalAlphaTest;
	private static boolean alphaTestLocked;

	public static boolean isAlphaTestLocked() {
		return alphaTestLocked;
	}

	public static void overrideAlphaTest(AlphaTest override) {
		if (!alphaTestLocked) {
			// Only save the previous state if the alpha test wasn't already locked
			GlStateManager.Alpha alphaState = GlStateManager.ALPHA_TEST;

			originalAlphaTestEnable = alphaState.state.enabled;
			originalAlphaTest = new AlphaTest(AlphaTestFunction.fromGlId(alphaState.func).get(), alphaState.ref);
		}

		alphaTestLocked = false;

		if (override == null) {
			GlStateManager.disableAlphaTest();
		} else {
			GlStateManager.enableAlphaTest();
			GlStateManager.alphaFunc(override.getFunction().getGlId(), override.getReference());
		}

		alphaTestLocked = true;
	}

	public static void deferAlphaTestToggle(boolean enabled) {
		originalAlphaTestEnable = enabled;
	}

	public static void deferAlphaFunc(int function, float reference) {
		originalAlphaTest = new AlphaTest(AlphaTestFunction.fromGlId(function).get(), reference);
	}

	public static void restoreAlphaTest() {
		if (!alphaTestLocked) {
			return;
		}

		alphaTestLocked = false;

		if (originalAlphaTestEnable) {
			GlStateManager.enableAlphaTest();
		} else {
			GlStateManager.disableAlphaTest();
		}

		GlStateManager.alphaFunc(originalAlphaTest.getFunction().getGlId(), originalAlphaTest.getReference());
	}
}
