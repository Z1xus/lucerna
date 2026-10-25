package net.coderbot.iris.gui;

import net.coderbot.iris.Iris;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.sound.instance.SimpleSoundInstance;
import net.minecraft.locale.I18n;
import net.minecraft.resource.Identifier;
import org.lwjgl.opengl.GL11;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Class serving as abstraction and
 * centralization for common GUI
 * rendering/other code calls.
 */
public final class GuiUtil {
	public static final Identifier IRIS_WIDGETS_TEX = new Identifier("iris", "textures/gui/widgets.png");
	private static final String ELLIPSIS = "...";

	private GuiUtil() {}

	private static Minecraft client() {
		return Minecraft.getInstance();
	}

	/**
	 * Binds Iris's widgets texture to be
	 * used for succeeding draw calls.
	 */
	public static void bindIrisWidgetsTexture() {
		client().getTextureManager().bind(IRIS_WIDGETS_TEX);
	}

	/**
	 * Draws a button. Button textures must be mapped with the
	 * same coordinates as those on the vanilla widgets texture.
	 *
	 * @param x X position of the left of the button
	 * @param y Y position of the top of the button
	 * @param width Width of the button, maximum 398
	 * @param height Height of the button, maximum 20
	 * @param hovered Whether the button is being hovered over with the mouse
	 * @param disabled Whether the button should use the "disabled" texture
	 */
	public static void drawButton(int x, int y, int width, int height, boolean hovered, boolean disabled) {
		// Create variables for half of the width and height.
		// Will not be exact when width and height are odd, but
		// that case is handled within the draw calls.
		int halfWidth = width / 2;
		int halfHeight = height / 2;

		// V offset for which button texture to use
		int vOffset = disabled ? 46 : hovered ? 86 : 66;

		// Use solid white as the tint color, and enable blending
		GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		// Top left section
		GuiElement.drawTexture(x, y, 0, vOffset, halfWidth, halfHeight, 256, 256);
		// Top right section
		GuiElement.drawTexture(x + halfWidth, y, 200 - (width - halfWidth), vOffset, width - halfWidth, halfHeight, 256, 256);
		// Bottom left section
		GuiElement.drawTexture(x, y + halfHeight, 0, vOffset + (20 - (height - halfHeight)), halfWidth, height - halfHeight, 256, 256);
		// Bottom right section
		GuiElement.drawTexture(x + halfWidth, y + halfHeight, 200 - (width - halfWidth), vOffset + (20 - (height - halfHeight)), width - halfWidth, height - halfHeight, 256, 256);
	}

	/**
	 * Draws a translucent black panel
	 * with a light border.
	 *
	 * @param x The x position of the panel
	 * @param y The y position of the panel
	 * @param width The width of the panel
	 * @param height The height of the panel
	 */
	public static void drawPanel(int x, int y, int width, int height) {
		int borderColor = 0xDEDEDEDE;
		int innerColor = 0xDE000000;

		// Top border section
		GuiElement.fill(x, y, x + width, y + 1, borderColor);
		// Bottom border section
		GuiElement.fill(x, (y + height) - 1, x + width, y + height, borderColor);
		// Left border section
		GuiElement.fill(x, y + 1, x + 1, (y + height) - 1, borderColor);
		// Right border section
		GuiElement.fill((x + width) - 1, y + 1, x + width, (y + height) - 1, borderColor);
		// Inner section
		GuiElement.fill(x + 1, y + 1, (x + width) - 1, (y + height) - 1, innerColor);
	}

	/**
	 * Draws a text with a panel behind it.
	 *
	 * @param text The text to draw
	 * @param x The x position of the panel
	 * @param y The y position of the panel
	 */
	public static void drawTextPanel(TextRenderer font, String text, int x, int y) {
		drawPanel(x, y, font.getWidth(text) + 8, 16);
		font.drawWithShadow(text, x + 4, y + 4, 0xFFFFFF);
	}

	/**
	 * Shorten a text to a specific length, adding an ellipsis (...)
	 * to the end if shortened.
	 *
	 * @param font Font to use for determining the width of text
	 * @param text Text to shorten
	 * @param width Width to shorten text to
	 * @return a shortened text
	 */
	public static String shortenText(TextRenderer font, String text, int width) {
		if (font.getWidth(text) > width) {
			return font.trim(text, width - font.getWidth(ELLIPSIS)) + ELLIPSIS;
		}
		return text;
	}

	/**
	 * Looks up a translation, first in the game's languages
	 * and then in the language files of the current shader pack.
	 *
	 * @param key Translation key to look up
	 * @return the translation if present
	 */
	public static Optional<String> getTranslation(String key) {
		if (I18n.hasTranslation(key)) {
			return Optional.of(I18n.translate(key));
		}

		// Shader packs use legacy language codes, check the selected language first and then fall back to English
		return Iris.getCurrentPack().flatMap(pack -> Stream.of(client().options.language.toLowerCase(Locale.ROOT), "en_us")
				.map(pack.getLanguageMap()::getTranslations)
				.filter(Objects::nonNull)
				.map(translations -> translations.get(key))
				.filter(Objects::nonNull)
				.findFirst());
	}

	/**
	 * Creates a new translated text, if a translation
	 * is present. If not, will return the default text
	 * passed.
	 *
	 * @param defaultText Default text to use if no translation is found
	 * @param translationDesc Translation key to try and use
	 * @return the translated text if found, otherwise the default provided
	 */
	public static String translateOrDefault(String defaultText, String translationDesc) {
		return getTranslation(translationDesc).orElse(defaultText);
	}

	/**
	 * Plays the button click sound as a
	 * master sound effect.
	 *
	 * Used in non-{@code ButtonWidget} UI elements upon click
	 * or other action.
	 */
	public static void playButtonClickSound() {
		client().getSoundManager().play(SimpleSoundInstance.of(new Identifier("gui.button.press"), 1.0F));
	}

	/**
	 * A class representing a section of a
	 * texture, to be easily drawn in GUIs.
	 */
	public static class Icon {
		public static final Icon REFRESH = new Icon(12, 0, 10, 10);

		private final int u;
		private final int v;
		private final int width;
		private final int height;

		public Icon(int u, int v, int width, int height) {
			this.u = u;
			this.v = v;
			this.width = width;
			this.height = height;
		}

		/**
		 * Draws this icon to the screen at the specified coordinates.
		 *
		 * @param x The x position to draw the icon at (left)
		 * @param y The y position to draw the icon at (top)
		 */
		public void draw(int x, int y) {
			// Use solid white as the tint color, and enable blending
			GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.enableBlend();

			// Draw the texture to the screen
			GuiElement.drawTexture(x, y, u, v, width, height, 256, 256);
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}
	}
}
