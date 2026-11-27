package net.cavern.gui.toggles;

import lombok.val;
import lombok.var;
import net.cavern.utils.FontType;
import net.cavern.utils.ui.Animation.AnimationType;
import net.cavern.utils.ui.Animation;
import net.cavern.utils.Time;
import net.cavern.utils.ui.liquidglass.LiquidGlass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.cavern.Cavern;
import net.cavern.addon.api.Addon;
import net.cavern.gui.toggles.button.ToggleButton;
import net.cavern.manager.FontManager;
import net.cavern.utils.RenderingUtils;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CavernGui extends GuiScreen {

    public float x, y, width, height, scroll, lastScroll, scrollTarget = 0, scrollHeight = 28, visibleItems = 6, totalItems = 10, maxScroll = 0f;
    public Animation widthAnim = new Animation(), heightAnim = new Animation(), scrollAnim = new Animation();
    ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
    public List<ToggleButton> addons = new ArrayList<>();
    public Addon currentAddon;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        listenAnimations();
        renderBackground();
        drawAddonsAndOptions(mouseX, mouseY);
    }

    @Override
    public void initGui() {
        LiquidGlass.init();
        currentAddon = Cavern.INSTANCE.getAddonManager().getAddon("bedwars_hud");
        widthAnim.start(AnimationType.SMOOTHSTEP, 0f, 300f, 0.325f);
        heightAnim.start(AnimationType.SMOOTHSTEP, 0f, 200f, 0.325f);
        x = (float) scaledResolution.getScaledWidth() / 2 - 150;
        y = (float) scaledResolution.getScaledHeight() / 2 - 100;
        maxScroll = Math.max(0, (totalItems * scrollHeight) - (visibleItems * scrollHeight));
        scroll = 0;
        lastScroll = 0;
        var offset = 0;
        for (Addon addon : Cavern.INSTANCE.getAddonManager().getAddons()) {
            if (addon.hasOptions())
                addons.add(new ToggleButton(x + 10, y + 30 - scroll + offset, 122, 24, this,
                        addon, -1, addon.getOptions()));
            else
                addons.add(new ToggleButton(x + 10, y + 30 - scroll + offset, 122, 24, this,
                        addon, -1));
            offset += 28;
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollTarget = Math.max(0, Math.min(scrollTarget + (wheel > 0 ? -25 : 25), maxScroll));
            scrollAnim.start(AnimationType.SMOOTHSTEP, scroll, scrollTarget, 0.1f);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (ToggleButton addon : addons) {
            if (addon.addon.hovered)
                addon.onPressed(mouseX, mouseY, mouseButton);
            if (addon.addon == currentAddon)
                for (val option : addon.options) {
                    if (option.hovered)
                        option.setVal(!option.isVal());
                }
        }
    }

    private void renderBackground() {
        var title_font = FontManager.getFont(FontType.MEDIUM, 32);
        var scrollbarY = y + 27;
        var scrollbarHeight = (visibleItems * scrollHeight) - 2;
        var scrollThumbHeight = Math.max(10, scrollbarHeight * (scrollbarHeight / (totalItems * scrollHeight)));
        var scrollThumbY = scrollbarY + (scroll / maxScroll) * (scrollbarHeight - scrollThumbHeight);

        RenderingUtils.makeCropBox(x, y, (float) scaledResolution.getScaledWidth() / 2 + (width / 2), (float) scaledResolution.getScaledHeight() / 2 + (height / 2));
        LiquidGlass.drawPanel(x, y, width, height, 10, 6, 8, 1.0f, 0.039f, 0.039f, 0.058f);
        LiquidGlass.drawPanel(x + 136, y + 26, 0, height, 0, 4, 0, 1.0f, .2f, .2f, .2f);
        LiquidGlass.drawPanel(x, y + 25, width, 0, 0, 4, 0, 1.0f, .2f, .2f, .2f);
        LiquidGlass.drawPanel(x + 2, scrollbarY, 5, scrollbarHeight, 0, 4, 0, 1.0f, 0, 0, 0);
        LiquidGlass.drawPanel(x + 3, scrollThumbY + 1, 3, scrollThumbHeight - 1, 0, 4, 0, 1.0f, .2f, .2f, .2f);
        title_font.drawCenteredStringWithShadow(Cavern.CAVERN_FORMATTED, x + (width / 2), y + 5, new Color(200, 200, 200).getRGB());
        RenderingUtils.destroyCropBox();
    }

    private void drawAddonsAndOptions(int mouseX, int mouseY) {
        var font = FontManager.getFont(FontType.MEDIUM, 22);
        RenderingUtils.makeCropBox(x, y + 27, (float) scaledResolution.getScaledWidth() / 2 + (width / 2), (float) scaledResolution.getScaledHeight() / 2 + (height / 2));
        for (ToggleButton addon : addons) {
            addon.color = -1;
            addon.drawComponent(mouseX, mouseY, addon.addon.hovered);
        }
        font.drawStringWithShadow("Options for", x + 140,
                y + 30, new Color(200, 200, 200).getRGB());
        font.drawStringWithShadow(currentAddon.getDisplayName(), x + 141 + font.getWidth("Options for"),
                y + 30, Cavern.INSTANCE.getClientColorNonRGB().brighter().brighter().getRGB());
        RenderingUtils.destroyCropBox();
    }

    private void listenAnimations() {
        x = (float) scaledResolution.getScaledWidth() / 2 - (width / 2);
        y = (float) scaledResolution.getScaledHeight() / 2 - (height / 2);
        if (widthAnim.isRunning()) width = widthAnim.update(Time.delta);
        if (heightAnim.isRunning()) height = heightAnim.update(Time.delta);
        if (scrollAnim.isRunning()) scroll = scrollAnim.update(Time.delta);
    }
}
