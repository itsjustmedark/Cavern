package net.cavern.gui.keystrokes;

import lombok.var;
import net.cavern.utils.FontType;
import net.cavern.utils.ui.Animation;
import net.cavern.utils.ui.Animation.AnimationType;
import net.cavern.utils.Time;
import net.cavern.widget.LabelWidget;
import net.minecraft.client.Minecraft;
import net.cavern.Cavern;
import net.cavern.manager.FontManager;
import net.cavern.utils.RenderingUtils;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class Keystrokes {

    private final Minecraft mc = Minecraft.getMinecraft();
    public double opacityF, opacityB, opacityL, opacityR;
    private boolean wasF, wasB, wasL, wasR;
    private final Animation animF = new Animation(), animB = new Animation(), animL = new Animation(), animR = new Animation();

    public void drawKeystrokes(float x, float y) {
        listenAnimations();
        drawStrokes(x, y);
    }

    private void listenAnimations() {
        boolean pressedF = mc.gameSettings.keyBindForward.pressed;
        boolean pressedB = mc.gameSettings.keyBindBack.pressed;
        boolean pressedL = mc.gameSettings.keyBindLeft.pressed;
        boolean pressedR = mc.gameSettings.keyBindRight.pressed;

        if (pressedF != wasF) { animF.start(AnimationType.SMOOTHSTEP, (float) opacityF, pressedF ? 255 : 10, 0.26f); wasF = pressedF; }
        if (pressedB != wasB) { animB.start(AnimationType.SMOOTHSTEP, (float) opacityB, pressedB ? 255 : 10, 0.26f); wasB = pressedB; }
        if (pressedL != wasL) { animL.start(AnimationType.SMOOTHSTEP, (float) opacityL, pressedL ? 255 : 10, 0.26f); wasL = pressedL; }
        if (pressedR != wasR) { animR.start(AnimationType.SMOOTHSTEP, (float) opacityR, pressedR ? 255 : 10, 0.26f); wasR = pressedR; }

        if (animF.isRunning()) opacityF = animF.update(Time.delta);
        if (animB.isRunning()) opacityB = animB.update(Time.delta);
        if (animL.isRunning()) opacityL = animL.update(Time.delta);
        if (animR.isRunning()) opacityR = animR.update(Time.delta);
    }

    private void drawStrokes(float x, float y) {
        var color = Cavern.INSTANCE.getClientColor();
        drawBackgrounds(x, y, color);
        drawLabels(x, y, color);
    }

    private void drawBackgrounds(float x, float y, int color) {
        int colorF = new Color((int) opacityF, (int) opacityF, (int) opacityF, 120).getRGB();
        int colorL = new Color((int) opacityL, (int) opacityL, (int) opacityL, 120).getRGB();
        int colorB = new Color((int) opacityB, (int) opacityB, (int) opacityB, 120).getRGB();
        int colorR = new Color((int) opacityR, (int) opacityR, (int) opacityR, 120).getRGB();

        RenderingUtils.drawBorderedRectangle(x + 21.5f, y, x + 41.5f, y + 20, 0.5f, colorF, color);
        RenderingUtils.drawBorderedRectangle(x - 0.5f, y + 22, x + 19.5f, y + 42, 0.5f, colorL, color);
        RenderingUtils.drawBorderedRectangle(x + 21.5f, y + 22, x + 41.5f, y + 42, 0.5f, colorB, color);
        RenderingUtils.drawBorderedRectangle(x + 43.5f, y + 22, x + 63.5f, y + 42, 0.5f, colorR, color);
    }

    private void drawLabels(float x, float y, int color) {
        var settings = mc.gameSettings;
        var keyBindForward = settings.keyBindForward;
        var keyBindBack = settings.keyBindBack;
        var keyBindLeft = settings.keyBindLeft;
        var keyBindRight = settings.keyBindRight;

        var forwardKey = Keyboard.getKeyName(keyBindForward.getKeyCode());
        var backKey = Keyboard.getKeyName(keyBindBack.getKeyCode());
        var leftKey = Keyboard.getKeyName(keyBindLeft.getKeyCode());
        var rightKey = Keyboard.getKeyName(keyBindRight.getKeyCode());

        var font = FontManager.getFont(FontType.NORMAL, 17);
        var forwardLabel = new LabelWidget((int) (x + 27), (int) (y + 6.5f), 20, font, forwardKey, color);
        var leftLabel = new LabelWidget((int) (x + 6), (int) (y + 28.5f), 20, font, leftKey, color);
        var backLabel = new LabelWidget((int) (x + 28), (int) (y + 28.5f), 20, font, backKey, color);
        var rightLabel = new LabelWidget((int) (x + 50), (int) (y + 28.5f), 20, font, rightKey, color);

        forwardLabel.draw();
        leftLabel.draw();
        backLabel.draw();
        rightLabel.draw();
    }
}
