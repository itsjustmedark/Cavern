package net.cavern.widget;

import net.cavern.utils.CustomFontRenderer;
import net.cavern.utils.RenderingUtils;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class LabelWidget extends Widget {
    public CustomFontRenderer font;
    public int textColor;
    public String text;

    public LabelWidget(int x, int y, int height, Color background, CustomFontRenderer font, String text, int textColor) {
        super(x, y, height, background);
        this.font = font;
        this.text = text;
        this.textColor = textColor;
    }

    public LabelWidget(int x, int y, int height, CustomFontRenderer font, String text, int textColor) {
        super(x, y, height);
        this.font = font;
        this.text = text;
        this.textColor = textColor;
    }

    @Override
    public void draw(ScaledResolution scaledResolution) {
        super.draw(scaledResolution);
        if (background != null)
            RenderingUtils.drawRectangle(x, y, x + font.getWidth(text) + 2, y + height, background.getRGB());
        font.drawStringWithShadow(text, x, y, textColor);
    }

    @Override
    public void draw() {
        super.draw();
        if (background != null)
            RenderingUtils.drawRectangle(x, y, x + font.getWidth(text) + 2, y + height, background.getRGB());
        font.drawStringWithShadow(text, x, y, textColor);
    }
}
