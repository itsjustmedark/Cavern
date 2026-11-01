package net.cavern.gui.join;

import lombok.var;
import net.cavern.utils.FontType;
import net.cavern.utils.ui.liquidglass.LiquidGlass;
import net.minecraft.client.Minecraft;
import net.cavern.Cavern;
import net.cavern.manager.FontManager;
import net.cavern.utils.RenderingUtils;

import java.awt.*;

public class JoinButton extends Component {

    public String command, text;
    public double slide;

    public JoinButton(double x, double y, double width, double height, JoinGamesGui parent, String command, String text, int color) {
        super(x, y, width, height, parent, color);
        this.command = command;
        this.text = text;
        slide = 0;
    }

    @Override
    public void onPressed(int key) {
        Minecraft.getMinecraft().thePlayer.sendChatMessage(command);
        super.onPressed(key);
    }

    @Override
    public void drawComponent(int mouseX, int mouseY, boolean hovered) {
        hovered = isHovered(mouseX, mouseY, (float) x, (float) y - parent.scroll, (float) width, (float) height);
        var font = FontManager.getFont(FontType.NORMAL, 22);
        // Button Background
//        RenderingUtils.drawRoundedRect((float) x, (float) y - parent.scroll, (float) (x + width), (float) (y + height) - parent.scroll,
//                8, new Color(10, 10, 15, 170).getRGB());
//        RenderingUtils.drawBlurredRect(RenderingUtils.BlurType.NORMAL, (float) x, (float) y - parent.scroll, (float) (x + width),
//                (float) (y + height) - parent.scroll, 8);
        LiquidGlass.drawPanel((float) x, (float) y - parent.scroll, (float) width, (float) height, 8, 2, 2, 0, 0, 0, 0);
        // Label
        font.drawCenteredStringWithShadow(text, (float) (x + (width / 2)), (float) (y + 7) - parent.scroll, hovered ?
                Cavern.INSTANCE.getClientColorNonRGB().brighter().brighter().getRGB() : new Color(200, 200, 200).getRGB());
    }

    public boolean isHovered(int mouseX, int mouseY, float x, float y, float width, float height) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }
}
