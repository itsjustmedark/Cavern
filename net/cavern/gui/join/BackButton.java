package net.cavern.gui.join;

import lombok.var;
import net.cavern.Cavern;
import net.cavern.manager.FontManager;
import net.cavern.utils.FontType;
import net.cavern.utils.RenderingUtils;
import net.cavern.utils.ui.liquidglass.LiquidGlass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.awt.*;

public class BackButton extends Component {

    public GuiScreen screen;
    public double slide;
    public String text;

    public BackButton(double x, double y, double width, double height, JoinGamesGui parent, GuiScreen screen, String text, int color) {
        super(x, y, width, height, parent, color);
        this.screen = screen;
        this.text = text;
        slide = 0;
    }

    @Override
    public void onPressed(int key) {
        if (key == 0) {
            Minecraft.getMinecraft().displayGuiScreen(screen);
            System.out.println("click");
        }
    }

    @Override
    public void drawComponent(int mouseX, int mouseY, boolean hovered) {
        hovered = isHovered(mouseX, mouseY, (float) x, (float) y, (float) width, (float) height);
        var font = FontManager.getFont(FontType.NORMAL, 22);
        // Button Background
//        RenderingUtils.drawRoundedRect((float) x, (float) y, (float) (x + width), (float) (y + height),
//                8, new Color(10, 10, 15, 170).getRGB());
//        RenderingUtils.drawBlurredRect(RenderingUtils.BlurType.NORMAL, (float) x, (float) y, (float) (x + width),
//                (float) (y + height), 8);
        LiquidGlass.drawPanel((float) x, (float) y, (float) width, (float) height, 8, 2, 2, 0, 0, 0, 0);
        // Label
        font.drawCenteredStringWithShadow(text, (float) (x + (width / 2) - 6), (float) (y + 7), hovered ?
                Cavern.INSTANCE.getClientColorNonRGB().brighter().brighter().getRGB() : new Color(200, 200, 200).getRGB());
    }

    public boolean isHovered(int mouseX, int mouseY, float x, float y, float width, float height) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }
}
