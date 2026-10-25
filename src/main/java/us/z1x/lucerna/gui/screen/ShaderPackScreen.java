package us.z1x.lucerna.gui.screen;

import us.z1x.lucerna.Iris;
import us.z1x.lucerna.gui.GuiUtil;
import us.z1x.lucerna.gui.NavigationController;
import us.z1x.lucerna.gui.element.IrisObjectSelectionList;
import us.z1x.lucerna.gui.element.ShaderPackOptionList;
import us.z1x.lucerna.gui.element.ShaderPackSelectionList;
import us.z1x.lucerna.gui.element.widget.AbstractElementWidget;
import us.z1x.lucerna.gui.element.widget.CommentedElementWidget;
import us.z1x.lucerna.shaderpack.ShaderPack;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Formatting;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ShaderPackScreen extends Screen implements HudHideable {
	/**
	 * Queue rendering to happen on top of all elements. Useful for tooltips or dialogs.
	 */
	public static final Set<Runnable> TOP_LAYER_RENDER_QUEUE = new HashSet<>();

	private static final int COMMENT_PANEL_WIDTH = 314;

	private static final int DONE_BUTTON = 0;
	private static final int APPLY_BUTTON = 1;
	private static final int CANCEL_BUTTON = 2;
	private static final int OPEN_FOLDER_BUTTON = 3;
	private static final int SCREEN_SWITCH_BUTTON = 4;
	private static final int HIDE_GUI_BUTTON = 5;

	private final Screen parent;
	private final String irisText;

	private ShaderPackSelectionList shaderPackList;

	private @Nullable ShaderPackOptionList shaderOptionList = null;
	private @Nullable NavigationController navigation = null;
	private ButtonWidget screenSwitchButton;
	private ButtonWidget hideGuiButton;

	private @Nullable AbstractElementWidget<?> hoveredElement = null;
	private Optional<String> hoveredElementCommentTitle = Optional.empty();
	private List<String> hoveredElementCommentBody = new ArrayList<>();
	private int hoveredElementCommentTimer = 0;

	private boolean optionMenuOpen = false;

	private boolean dropChanges = false;
	private String developmentText;

	private boolean guiHidden = false;
	private int guiButtonHoverTimer = 0;

	public ShaderPackScreen(Screen parent) {
		this.parent = parent;

		String irisName = Iris.MODNAME + " " + Iris.getVersion();

		if (irisName.contains("-development-environment")) {
			this.developmentText = Formatting.GOLD + "Development Environment";
			irisName = irisName.replace("-development-environment", "");
		}

		this.irisText = Formatting.GRAY + irisName;

		refreshForChangedPack();
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		if (this.minecraft.world == null) {
			this.renderBackground();
		} else if (!this.guiHidden) {
			this.fillGradient(0, 0, width, height, 0x4F232323, 0x4F232323);
		}

		if (!this.guiHidden) {
			getActiveList().render(mouseX, mouseY, delta);
		}

		super.render(mouseX, mouseY, delta);

		if (!this.guiHidden) {
			drawCenteredString(this.textRenderer, I18n.translate("options.iris.shaderPackSelection.title"), (int) (this.width * 0.5), 8, 0xFFFFFF);

			String subtitle = I18n.translate(optionMenuOpen ? "pack.iris.configure.title" : "pack.iris.select.title");

			drawCenteredString(this.textRenderer, Formatting.GRAY + "" + Formatting.ITALIC + subtitle, (int) (this.width * 0.5), 21, 0xFFFFFF);

			// Draw the comment panel
			if (this.isDisplayingComment()) {
				// Determine panel height and position
				int panelHeight = Math.max(50, 18 + (this.hoveredElementCommentBody.size() * 10));
				int x = (int) (0.5 * this.width) - 157;
				int y = this.height - (panelHeight + 4);
				// Draw panel
				GuiUtil.drawPanel(x, y, COMMENT_PANEL_WIDTH, panelHeight);
				// Draw text
				this.textRenderer.drawWithShadow(this.hoveredElementCommentTitle.orElse(""), x + 4, y + 4, 0xFFFFFF);
				for (int i = 0; i < this.hoveredElementCommentBody.size(); i++) {
					this.textRenderer.drawWithShadow(this.hoveredElementCommentBody.get(i), x + 4, (y + 16) + (i * 10), 0xFFFFFF);
				}
			}
		}

		if (this.hideGuiButton != null && this.hideGuiButton.isHovered() && this.guiButtonHoverTimer >= 10) {
			String showOrHide = I18n.translate(this.guiHidden ? "options.iris.gui.show" : "options.iris.gui.hide");

			TOP_LAYER_RENDER_QUEUE.add(() -> this.renderTooltip(showOrHide, mouseX, mouseY));
		}

		if (this.developmentText != null) {
			this.textRenderer.drawWithShadow(developmentText, 2, this.height - 10, 0xFFFFFF);
			this.textRenderer.drawWithShadow(irisText, 2, this.height - 20, 0xFFFFFF);
		} else {
			this.textRenderer.drawWithShadow(irisText, 2, this.height - 10, 0xFFFFFF);
		}

		// Render everything queued to render last
		for (Runnable render : TOP_LAYER_RENDER_QUEUE) {
			render.run();
		}
		TOP_LAYER_RENDER_QUEUE.clear();
	}

	@Override
	public void init() {
		int bottomCenter = this.width / 2 - 50;
		int topCenter = this.width / 2 - 76;
		boolean inWorld = this.minecraft.world != null;

		this.shaderPackList = new ShaderPackSelectionList(this, this.minecraft, this.width, this.height, 32, this.height - 58, 0, this.width);

		if (Iris.getCurrentPack().isPresent() && this.navigation != null) {
			ShaderPack currentPack = Iris.getCurrentPack().get();

			this.shaderOptionList = new ShaderPackOptionList(this, this.navigation, currentPack, this.minecraft, this.width, this.height, 32, this.height - 58, 0, this.width);
			this.navigation.setActiveOptionList(this.shaderOptionList);

			this.shaderOptionList.rebuild();
		} else {
			optionMenuOpen = false;
			this.shaderOptionList = null;
		}

		if (inWorld) {
			this.shaderPackList.setRenderBackground(false);
			if (shaderOptionList != null) {
				this.shaderOptionList.setRenderBackground(false);
			}
		}

		this.buttons.clear();
		this.screenSwitchButton = null;
		this.hideGuiButton = null;

		if (!this.guiHidden) {
			this.buttons.add(new ButtonWidget(DONE_BUTTON, bottomCenter + 104, this.height - 27, 100, 20,
				I18n.translate("gui.done")));

			this.buttons.add(new ButtonWidget(APPLY_BUTTON, bottomCenter, this.height - 27, 100, 20,
				I18n.translate("options.iris.apply")));

			this.buttons.add(new ButtonWidget(CANCEL_BUTTON, bottomCenter - 104, this.height - 27, 100, 20,
				I18n.translate("gui.cancel")));

			this.buttons.add(new ButtonWidget(OPEN_FOLDER_BUTTON, topCenter - 78, this.height - 51, 152, 20,
				I18n.translate("options.iris.openShaderPackFolder")));

			this.screenSwitchButton = new ButtonWidget(SCREEN_SWITCH_BUTTON, topCenter + 78, this.height - 51, 152, 20,
				I18n.translate("options.iris.shaderPackList"));
			this.buttons.add(this.screenSwitchButton);

			refreshScreenSwitchButton();
		}

		if (inWorld) {
			float endOfLastButton = this.width / 2.0f + 154.0f;
			float freeSpace = this.width - endOfLastButton;
			int x;
			if (freeSpace > 100.0f) {
				x = this.width - 50;
			} else if (freeSpace < 20.0f) {
				x = this.width - 20;
			} else {
				x = (int) (endOfLastButton + (freeSpace / 2.0f)) - 10;
			}

			this.hideGuiButton = new ButtonWidget(HIDE_GUI_BUTTON, x, this.height - 39, 20, 20, "") {
				@Override
				public void render(Minecraft minecraft, int mouseX, int mouseY) {
					this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

					GuiUtil.bindIrisWidgetsTexture();
					GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
					GlStateManager.enableBlend();
					GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					this.drawTexture(this.x, this.y, guiHidden ? 20 : 0, this.hovered ? 166 : 146, this.width, this.height);
				}
			};
			this.buttons.add(this.hideGuiButton);
		}

		// NB: Don't let comment remain when exiting options screen
		// https://github.com/IrisShaders/Iris/issues/1494
		this.hoveredElement = null;
		this.hoveredElementCommentTimer = 0;
	}

	private IrisObjectSelectionList<?> getActiveList() {
		if (optionMenuOpen && this.shaderOptionList != null) {
			return this.shaderOptionList;
		}

		return this.shaderPackList;
	}

	@Override
	protected void buttonClicked(ButtonWidget button) {
		switch (button.id) {
			case DONE_BUTTON:
				onClose();
				break;
			case APPLY_BUTTON:
				this.applyChanges();
				break;
			case CANCEL_BUTTON:
				this.dropChangesAndClose();
				break;
			case OPEN_FOLDER_BUTTON:
				openShaderPackFolder();
				break;
			case SCREEN_SWITCH_BUTTON:
				this.optionMenuOpen = !this.optionMenuOpen;

				// UX: Apply changes before switching screens to avoid unintuitive behavior
				//
				// Not doing this leads to unintuitive behavior, since selecting a pack in the
				// list (but not applying) would open the settings for the previous pack, rather
				// than opening the settings for the selected (but not applied) pack.
				this.applyChanges();

				this.init();
				break;
			case HIDE_GUI_BUTTON:
				this.guiHidden = !this.guiHidden;
				this.init();
				break;
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		super.mouseClicked(mouseX, mouseY, button);

		if (!this.guiHidden) {
			getActiveList().mouseClicked(mouseX, mouseY, button);
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int button) {
		super.mouseReleased(mouseX, mouseY, button);

		if (!this.guiHidden) {
			getActiveList().mouseReleased(mouseX, mouseY, button);
		}
	}

	@Override
	public void handleMouse() {
		super.handleMouse();

		if (!this.guiHidden) {
			getActiveList().handleMouse();
		}
	}

	public void refreshForChangedPack() {
		if (Iris.getCurrentPack().isPresent()) {
			ShaderPack currentPack = Iris.getCurrentPack().get();

			this.navigation = new NavigationController(currentPack.getMenuContainer());

			if (this.shaderOptionList != null) {
				this.shaderOptionList.applyShaderPack(currentPack);
				this.shaderOptionList.rebuild();
			}
		} else {
			this.navigation = null;
		}

		refreshScreenSwitchButton();
	}

	public void refreshScreenSwitchButton() {
		if (this.screenSwitchButton != null) {
			this.screenSwitchButton.message = I18n.translate(optionMenuOpen ? "options.iris.shaderPackList" : "options.iris.shaderPackSettings");
			this.screenSwitchButton.active = optionMenuOpen || shaderPackList.getTopButtonRow().shadersEnabled;
		}
	}

	@Override
	public void tick() {
		super.tick();

		if (this.hoveredElement != null) {
			this.hoveredElementCommentTimer++;
		} else {
			this.hoveredElementCommentTimer = 0;
		}

		if (this.hideGuiButton != null && this.hideGuiButton.isHovered()) {
			this.guiButtonHoverTimer++;
		} else {
			this.guiButtonHoverTimer = 0;
		}
	}

	@Override
	protected void keyPressed(char chr, int key) {
		if (key == Keyboard.KEY_ESCAPE) {
			if (this.guiHidden) {
				this.guiHidden = false;
				this.init();
			} else if (this.navigation != null && this.navigation.hasHistory()) {
				this.navigation.back();
			} else if (this.optionMenuOpen) {
				this.optionMenuOpen = false;
				this.init();
			} else {
				onClose();
			}
		}
	}

	public void onClose() {
		if (!dropChanges) {
			applyChanges();
		} else {
			discardChanges();
		}

		this.minecraft.openScreen(parent);
	}

	private void dropChangesAndClose() {
		dropChanges = true;
		onClose();
	}

	public void applyChanges() {
		ShaderPackSelectionList.BaseEntry base = this.shaderPackList.getSelected();

		if (!(base instanceof ShaderPackSelectionList.ShaderPackEntry)) {
			return;
		}

		ShaderPackSelectionList.ShaderPackEntry entry = (ShaderPackSelectionList.ShaderPackEntry)base;
		this.shaderPackList.setApplied(entry);

		String name = entry.getPackName();

		// If the pack is being changed, clear pending options from the previous pack to
		// avoid possible undefined behavior from applying one pack's options to another pack
		if (!name.equals(Iris.getCurrentPackName())) {
			Iris.clearShaderPackOptionQueue();
		}

		boolean enabled = this.shaderPackList.getTopButtonRow().shadersEnabled;

		String previousPackName = Iris.getIrisConfig().getShaderPackName().orElse(null);
		boolean previousShadersEnabled = Iris.getIrisConfig().areShadersEnabled();

		// Only reload if the pack would be different from before, or shaders were toggled, or options were changed, or if we're about to reset options.
		if (!name.equals(previousPackName) || enabled != previousShadersEnabled || !Iris.getShaderPackOptionQueue().isEmpty() || Iris.shouldResetShaderPackOptionsOnNextReload()) {
			Iris.getIrisConfig().setShaderPackName(name);
			IrisApi.getInstance().getConfig().setShadersEnabledAndApply(enabled);
		}

		refreshForChangedPack();
	}

	private void discardChanges() {
		Iris.clearShaderPackOptionQueue();
	}

	private void openShaderPackFolder() {
		Sys.openURL(Iris.getShaderpacksDirectoryManager().getDirectoryUri().toString());
	}

	// Let the screen know if an element is hovered or not, allowing for accurately updating which element is hovered
	public void setElementHoveredStatus(AbstractElementWidget<?> widget, boolean hovered) {
		if (hovered && widget != this.hoveredElement) {
			this.hoveredElement = widget;

			if (widget instanceof CommentedElementWidget) {
				this.hoveredElementCommentTitle = ((CommentedElementWidget<?>) widget).getCommentTitle();

				Optional<String> commentBody = ((CommentedElementWidget<?>) widget).getCommentBody();
				if (!commentBody.isPresent()) {
					this.hoveredElementCommentBody.clear();
				} else {
					String rawCommentBody = commentBody.get();

					// Strip any trailing "."s
					if (rawCommentBody.endsWith(".")) {
						rawCommentBody = rawCommentBody.substring(0, rawCommentBody.length() - 1);
					}
					// Split comment body into lines by separator ". " and line wrap
					this.hoveredElementCommentBody = new ArrayList<>();
					for (String text : rawCommentBody.split("\\. [ ]*")) {
						this.hoveredElementCommentBody.addAll(this.textRenderer.split(text, COMMENT_PANEL_WIDTH - 8));
					}
				}
			} else {
				this.hoveredElementCommentTitle = Optional.empty();
				this.hoveredElementCommentBody.clear();
			}

			this.hoveredElementCommentTimer = 0;
		} else if (!hovered && widget == this.hoveredElement) {
			this.hoveredElement = null;
			this.hoveredElementCommentTitle = Optional.empty();
			this.hoveredElementCommentBody.clear();
			this.hoveredElementCommentTimer = 0;
		}
	}

	public boolean isDisplayingComment() {
		return this.hoveredElementCommentTimer > 20 &&
				this.hoveredElementCommentTitle.isPresent() &&
				!this.hoveredElementCommentBody.isEmpty();
	}
}
