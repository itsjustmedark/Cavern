package net.minecraft.client.gui;

import lombok.var;
import net.cavern.utils.FontType;
import net.cavern.utils.ui.Animation;
import net.cavern.utils.Time;
import net.cavern.utils.ui.liquidglass.LiquidGlass;
import net.minecraft.client.Minecraft;
import net.cavern.utils.ui.Animation.AnimationType;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.cavern.Cavern;
import net.cavern.manager.FontManager;
import net.cavern.utils.RenderingUtils;

import java.awt.*;

public class GuiButton extends Gui
{
    protected static final ResourceLocation buttonTextures = new ResourceLocation("textures/gui/widgets.png");

    /** Button width in pixels */
    protected int width;

    /** Button height in pixels */
    protected int height;

    /** The x position of this control. */
    public int xPosition;

    /** The y position of this control. */
    public int yPosition;

    /** The string displayed on this control. */
    public String displayString;
    public int id;

    /** True if this control is enabled, false to disable. */
    public boolean enabled;

    /** Hides the button completely if false. */
    public boolean visible;
    protected boolean hovered;
    public double slide;
    public Animation slideAnim = new Animation();
    private boolean wasHovered = false;

    public GuiButton(int buttonId, int x, int y, String buttonText)
    {
        this(buttonId, x, y, 200, 20, buttonText);
    }

    public GuiButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
    {
        this.width = 200;
        this.height = 20;
        this.enabled = true;
        this.visible = true;
        this.id = buttonId;
        this.xPosition = x;
        this.yPosition = y;
        this.width = widthIn;
        this.height = heightIn;
        this.displayString = buttonText;
    }

    /**
     * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
     * this button.
     */
    protected int getHoverState(boolean mouseOver)
    {
        int i = 1;

        if (!this.enabled)
        {
            i = 0;
        }
        else if (mouseOver)
        {
            i = 2;
        }

        return i;
    }

    /**
     * Draws this button to the screen.
     */
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            FontRenderer fontrenderer = mc.fontRendererObj;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            var color = hovered ? Cavern.INSTANCE.getClientColor() : -1;
            if (hovered != wasHovered) {
                slideAnim.start(AnimationType.SMOOTHSTEP, (float) slide, hovered ? width : 0, 0.25f);
                wasHovered = hovered;
            }
            if (slideAnim.isRunning())
                slide = slideAnim.update(Time.delta);
//            RenderingUtils.drawRectangle(this.xPosition, this.yPosition, this.xPosition + width, this.yPosition + height, new Color(0, 0, 0, 50).getRGB());
            RenderingUtils.drawRectangle((float) this.xPosition, (float) this.yPosition, (float) (this.xPosition + slide),
                    this.yPosition + height, Cavern.INSTANCE.getClientColorNonRGB().brighter().getRGB());
            LiquidGlass.drawPanel(xPosition, yPosition, width, height, 1, 4, 2, .6f, 0, 0, 0);
            //            RenderingUtils.drawBlurredRect(RenderingUtils.BlurType.NORMAL, this.xPosition, this.yPosition, this.xPosition + width, this.yPosition + height, 0);
            FontManager.getFont(FontType.MEDIUM, 16).drawCenteredString(this.displayString, this.xPosition + (float) this.width / 2,
                    this.yPosition + (float) (this.height - 6) / 2, enabled ? -1 : new Color(145, 145, 145).getRGB());
        }
    }

    /**
     * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
     */
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
    {
    }

    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    public void mouseReleased(int mouseX, int mouseY)
    {
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        return this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
    }

    /**
     * Whether the mouse cursor is currently over the button.
     */
    public boolean isMouseOver()
    {
        return this.hovered;
    }

    public void drawButtonForegroundLayer(int mouseX, int mouseY)
    {
    }

    public void playPressSound(SoundHandler soundHandlerIn)
    {
        soundHandlerIn.playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    }

    public int getButtonWidth()
    {
        return this.width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }
}
