package net.cavern.gui.toggles.button;

import lombok.val;
import lombok.var;
import net.cavern.Cavern;
import net.cavern.gui.toggles.Component;
import net.cavern.addon.api.Addon;
import net.cavern.gui.toggles.CavernGui;
import net.cavern.gui.toggles.Option;
import net.cavern.manager.FontManager;
import net.cavern.utils.FontType;
import net.cavern.utils.ui.Animation;
import net.cavern.utils.ui.Animation.AnimationType;
import net.cavern.utils.CustomFontRenderer;
import net.cavern.utils.RenderingUtils;
import net.cavern.utils.Time;
import net.cavern.utils.ui.liquidglass.LiquidGlass;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ToggleButton extends Component {

    public double slide;
    public float modState;
    public Addon addon;
    public List<Option> options = new ArrayList<>();
    public int offset = 40;
    public boolean cust, wasState = false, initialized = false;
    public Animation modToggleAnim = new Animation();

    public ToggleButton(double x, double y, double width, double height, CavernGui parent, Addon addon, int color, Option... options) {
        super(x, y, width, height, parent, color);
        this.addon = addon;
        Collections.addAll(this.options, options);
        slide = 0;
    }

    public ToggleButton(double x, double y, double width, double height, CavernGui parent, Addon addon, int color, boolean cust, Option... options) {
        super(x, y, width, height, parent, color);
        this.addon = addon;
        this.cust = cust;
        Collections.addAll(this.options, options);
        slide = 0;
    }

    @Override
    public void onPressed(int mouseX, int mouseY, int key) {
        if (addon.hovered)
            if (key == 0)
                addon.setState(!addon.isState());
        if (key == 1)
            parent.currentAddon = addon;
    }

    @Override
    public void drawComponent(int mouseX, int mouseY, boolean hovered) {
        if (modToggleAnim.isRunning()) modState = modToggleAnim.update(Time.delta);
        var font = FontManager.getFont(FontType.MEDIUM, 22);
        var state = addon.isState();

        if (!initialized) {
            modState = (float) (addon.isState() ? (x + width) - 18 : (x + width) - 30);
            wasState = addon.isState();
            initialized = true;
        }

        if (state != wasState) {
            float from = modState;
            float to = (float) (state ? (x + width) - 18 : (x + width) - 30);
            modToggleAnim.start(AnimationType.SMOOTHSTEP, from, to, 0.25f);
            wasState = state;
        }

        // Button Background
//        RenderingUtils.drawRoundedRect((float) x, (float) y - parent.scroll, (float) (x + width), (float) (y + height) - parent.scroll,
//                8, new Color(10, 10, 15, 170).getRGB());
//        RenderingUtils.drawBlurredRect(RenderingUtils.BlurType.NORMAL, (float) x, (float) y - parent.scroll, (float) (x + width),
//                (float) (y + height) - parent.scroll, 8);

        LiquidGlass.drawPanel((float) x, (float) (y - parent.scroll), (float) width, (float) (height),
                8, 4, 0, 1, 0, 0, 0);

        // Label
        font.drawStringWithShadow(addon.getDisplayName(), (float) (x + 8), (float) (y + 7) - parent.scroll,
                addon.isState() ? Cavern.INSTANCE.getClientColorNonRGB().brighter().brighter().getRGB() : new Color(200, 200, 200).getRGB());

        // Slider Background
//        RenderingUtils.drawRoundedRect((float) ((x + width) - 32), (float) (y + 4) - parent.scroll, (float) ((x + width) - 4), (float)
//                ((y + height) - 4) - parent.scroll, 6, new Color(40, 40, 55, 115).getRGB());
//        RenderingUtils.drawBlurredRect(RenderingUtils.BlurType.NORMAL, (float) ((x + width) - 32), (float) (y + 4), (float) ((x + width) - 4),
//                (float) ((y + height) - 4), 6);

        LiquidGlass.drawPanel((float) (x + width) - 32, (float) ((y + 4) - parent.scroll), (float) 28, (float) (16),
                8, 2, 0, 1, 0, 0, 0);

        // Slider
//        RenderingUtils.drawRoundedRect(modState, (float) (y + 6) - parent.scroll, modState + 12,
//                (float) ((y + height) - 6) - parent.scroll, 6,
//                state ? RenderingUtils.rgba(90, 70, 124, 115) : RenderingUtils.rgba(145, 145, 165, 115));
//        RenderingUtils.drawBlurredRect(RenderingUtils.BlurType.NORMAL, modState, (float) (y + 6) - parent.scroll, modState + 12,
//                (float) ((y + height) - 6) - parent.scroll, 6);

        LiquidGlass.drawPanel((float) (modState), (float) ((y + 6) - parent.scroll), (float) 12, (float) (12),
                8, 5, 0, 1, .2f, .2f, .2f);

        var hoverY = (y + 4);
        addon.hovered = isHovered(mouseX, mouseY, (float) ((x + width) - 36), (float) hoverY - parent.scroll, 32F,
                (float) height - 4);
        int off = 12;
        for (val option : options) {
            createCheckboxes(mouseX, mouseY, font, option, (int) (parent.x + 140), (int) (parent.y + 36 + off), 120, 15);
            off += 20;
        }
    }

    public void createCheckboxes(int mouseX, int mouseY, CustomFontRenderer font, Option option, int x, int y, int width, int height) {
        if (option.getOptToggleAnim().isRunning()) option.optState = option.getOptToggleAnim().update(Time.delta);
        var state = option.isVal();

        if (option.optState == 0) {
            option.optState = option.isVal() ? 14 : 2;
            option.optWasState = option.isVal();
        }

        if (state != option.optWasState) {
            float to = option.isVal() ? 14 : 2;
            option.getOptToggleAnim().start(AnimationType.SMOOTHSTEP, option.optState, to, 0.25f);
            option.optWasState = state;
        }

        if (parent.currentAddon == addon) {
            option.hovered = isHovered(mouseX, mouseY, x, y, width, height);
            font.drawStringWithShadow(option.display, x + 32, y + 1,
                    option.isVal() ? Cavern.INSTANCE.getClientColorNonRGB().brighter().brighter().getRGB() : new Color(200, 200, 200).getRGB());

            // Background
//            LiquidGlass.beginFrame();
            LiquidGlass.drawPanel(x, y - 2, 28, 16, 8, 2, 0, 1,0, 0, 0);
//            LiquidGlass.endFrame();
//            RenderingUtils.drawRoundedRect(x, y - 2, x + 28, y + 14, 6
//                    , new Color(0, 0, 0, 100).getRGB());
//            RenderingUtils.drawBlurredRect(RenderingUtils.BlurType.NORMAL, x, y - 2, x + 28, y + 14, 6);

            // Slider
            LiquidGlass.drawPanel(x + option.optState, y, 12,  12, 8, 5, 0, 1, .2f, .2f, .2f);
//            RenderingUtils.drawRoundedRect(x + option.optState, y, x + option.optState + 12, y + 12, 6,
//                    option.val ? RenderingUtils.rgba(90, 70, 124, 115) : RenderingUtils.rgba(145, 145, 165, 115));
//            RenderingUtils.drawBlurredRect(RenderingUtils.BlurType.NORMAL, x + option.optState, y, x + option.optState + 12, y + 12, 6);
        }
    }

    public boolean isHovered(int mouseX, int mouseY, float x, float y, float width, float height) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }
}
