package net.cavern.gui.join;

import lombok.val;
import lombok.var;
import net.cavern.Cavern;
import net.cavern.utils.FontType;
import net.cavern.utils.ui.Animation;
import net.cavern.utils.ui.Animation.AnimationType;
import net.cavern.utils.Time;
import net.cavern.utils.ui.liquidglass.LiquidGlass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.cavern.gui.toggles.CavernGui;
import net.cavern.manager.FontManager;
import net.cavern.utils.RenderingUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JoinGamesGui extends GuiScreen {
    ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
    public List<JoinButton> buttons = new ArrayList<>();
    public BackButton backButton;
    public float x, y, width, height, scroll, lastScroll, scrollTarget = 0, scrollHeight = 28, visibleItems = 6, totalItems = 12, maxScroll = 0f;
    public Animation scrollAnim = new Animation();

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (scrollAnim.isRunning()) scroll = scrollAnim.update(Time.delta);

        RenderingUtils.makeCropBox(x, y, (float) scaledResolution.getScaledWidth() / 2 + (width / 2), (float) scaledResolution.getScaledHeight() / 2 + (height / 2));
        renderBackground();
        RenderingUtils.destroyCropBox();

        RenderingUtils.makeCropBox(x, y + 27, (float) scaledResolution.getScaledWidth() / 2 + (width / 2), (float) scaledResolution.getScaledHeight() / 2 + (height / 2));
        for (val button : buttons)
            button.drawComponent(mouseX, mouseY, button.isHovered(mouseX, mouseY, (float) button.x, (float) button.y, (float) button.width, (float) button.height));
        RenderingUtils.destroyCropBox();
        backButton.drawComponent(mouseX, mouseY, backButton.isHovered(mouseX, mouseY, (float) backButton.x, (float) backButton.y, (float) backButton.width, (float) backButton.height));
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void initGui() {
        width = 300;
        height = 200;
        x = (float) scaledResolution.getScaledWidth() / 2 - 150;
        y = (float) scaledResolution.getScaledHeight() / 2 - 100;
        maxScroll = Math.max(0, (totalItems * scrollHeight) - (visibleItems * scrollHeight));
        scroll = 0;
        lastScroll = 0;
        backButton = new BackButton(x + 5, y + 30, 122, 24, this, new CavernGui(), "↩ Back to GUI...", -1);
        buttons.add(new JoinButton(x + 144, y + 30 - scroll, 148, 24, this, "/play bedwars_eight_two", "Doubles Bedwars", -1));
        buttons.add(new JoinButton(x + 144, y + 60 - scroll, 148, 24, this, "/play bedwars_four_three", "3v3 Bedwars", -1));
        buttons.add(new JoinButton(x + 144, y + 90 - scroll, 148, 24, this, "/play bedwars_four_four", "4v4 Bedwars", -1));
        buttons.add(new JoinButton(x + 144, y + 120 - scroll, 148, 24, this, "/play solo_normal", "Solo Skywars Normal", -1));
        buttons.add(new JoinButton(x + 144, y + 150 - scroll, 148, 24, this, "/play solo_insane", "Solo Skywars Insane", -1));
        buttons.add(new JoinButton(x + 144, y + 180 - scroll, 148, 24, this, "/play teams_normal", "Team Skywars Normal", -1));
        buttons.add(new JoinButton(x + 144, y + 210 - scroll, 148, 24, this, "/play teams_insane", "Team Skywars Insane", -1));
        buttons.add(new JoinButton(x + 144, y + 240 - scroll, 148, 24, this, "/play murder_classic", "Murder Classic", -1));
        buttons.add(new JoinButton(x + 144, y + 270 - scroll, 148, 24, this, "/play murder_double_up", "Murder Double Up", -1));
        buttons.add(new JoinButton(x + 144, y + 300 - scroll, 148, 24, this, "/play pit", "The Pit", -1));
        buttons.add(new JoinButton(x + 144, y + 330 - scroll, 148, 24, this, "/play sb", "Skyblock", -1));
        super.initGui();
    }

//    private void renderBackground() {
//        var title_font = FontManager.getMedFont(32);
//        var scrollbarY = y + 27;
//        var scrollbarHeight = (visibleItems * scrollHeight) + 2;
//        var scrollThumbHeight = Math.max(12, scrollbarHeight * (scrollbarHeight / (totalItems * scrollHeight)));
//        var scrollThumbY = scrollbarY + (scroll / maxScroll) * (scrollbarHeight - scrollThumbHeight);
//
//        RenderingUtils.drawRoundedRect(x, y, x + width, y + height, 10, new Color(10, 10, 15, 215).getRGB());
//        RenderingUtils.drawBlurredRect(RenderingUtils.BlurType.NORMAL, x, y, x + width, y + height, 10);
//        RenderingUtils.drawRectangle(x + 131, y + 26, x + 132, y + height, new Color(40, 40, 55, 215).getRGB());
//        RenderingUtils.drawRectangle(x, y + 25, x + width, y + 26, new Color(40, 40, 55, 215).getRGB());
//        RenderingUtils.drawRectangle(x + 134, scrollbarY, x + 139, scrollbarY + scrollbarHeight, new Color(10, 10, 20, 215).getRGB());
//        RenderingUtils.drawRectangle(x + 135, scrollThumbY + 1, x + 138, (scrollThumbY + scrollThumbHeight) - 1, new Color(40, 40, 55, 215).getRGB());
//        title_font.drawCenteredStringWithShadow("Cavern 1.1.0   | Join Games", x + (width / 2), y + 5, new Color(145, 145, 165).getRGB());
//    }

    private void renderBackground() {
        var title_font = FontManager.getFont(FontType.MEDIUM, 32);
        var scrollbarY = y + 27;
        var scrollbarHeight = (visibleItems * scrollHeight) + 2;
        var scrollThumbHeight = Math.max(10, scrollbarHeight * (scrollbarHeight / (totalItems * scrollHeight)));
        var scrollThumbY = scrollbarY + (scroll / maxScroll) * (scrollbarHeight - scrollThumbHeight);

        LiquidGlass.drawPanel(x, y, width, height, 10, 6, 8, 1.0f, 0.039f, 0.039f, 0.058f);
        LiquidGlass.drawPanel(x + 136, y + 26, 0, height, 0, 4, 0, 1.0f, .2f, .2f, .2f);
        LiquidGlass.drawPanel(x, y + 25, width, 0, 0, 4, 0, 1.0f, .2f, .2f, .2f);
        LiquidGlass.drawPanel(x + 134, scrollbarY, 5, scrollbarHeight, 0, 4, 0, 1.0f, 0, 0, 0);
        LiquidGlass.drawPanel(x + 135, scrollThumbY + 1, 3, scrollThumbHeight - 1, 0, 4, 0, 1.0f, .2f, .2f, .2f);
        title_font.drawCenteredStringWithShadow(Cavern.CAVERN_FORMATTED, x + (width / 2), y + 5, new Color(200, 200, 200).getRGB());
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (val button : buttons)
            if (isHovered(mouseX, mouseY, (float) button.x, (float) button.y - scroll, (float) button.width, (float) button.height))
                button.onPressed(mouseButton);
        if (isHovered(mouseX, mouseY, (float) backButton.x, (float) backButton.y, (float) backButton.width, (float) backButton.height))
            backButton.onPressed(mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
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
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(new CavernGui());
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    public boolean isHovered(int mouseX, int mouseY, float x, float y, float width, float height) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }
}
