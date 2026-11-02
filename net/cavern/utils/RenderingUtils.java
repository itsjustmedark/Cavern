package net.cavern.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import net.cavern.gui.blur.impl.BlurShader;
import net.cavern.gui.blur.impl.KawaseBlur;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class RenderingUtils {

    private static long lastTime = System.currentTimeMillis();
    public static double rainbowState;

    public static void drawRectangle(float startX, float startY, float endX, float endY, int color) {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT |
                GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_LINE_BIT |
                GL11.GL_TEXTURE_BIT);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);

        glColor(color);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(startX, endY);
        GL11.glVertex2f(endX, endY);
        GL11.glVertex2f(endX, startY);
        GL11.glVertex2f(startX, startY);
        GL11.glEnd();

        GL11.glPopAttrib();
    }

    public static void drawRoundedRect(float x, float y, float x2, float y2, float radius, int color) {
        if (x > x2) { float t = x; x = x2; x2 = t; }
        if (y > y2) { float t = y; y = y2; y2 = t; }

        float width = x2 - x;
        float height = y2 - y;
        if (width <= 0 || height <= 0) return;

        enableGL();
        glColor(color);

        // Center
        drawQuad(x + radius, y + radius, x2 - radius, y2 - radius);

        // Top
        drawQuad(x + radius, y, x2 - radius, y + radius);

        // Bottom
        drawQuad(x + radius, y2 - radius, x2 - radius, y2);

        // Left
        drawQuad(x, y + radius, x + radius, y2 - radius);

        // Right
        drawQuad(x2 - radius, y + radius, x2, y2 - radius);

        int segments = Math.max(16, (int)(radius * 3f)); // smoother corners

        // Corners
        drawArc(x + radius,     y + radius,     radius, 180, 270, segments); // top-left
        drawArc(x2 - radius,    y + radius,     radius, 270, 360, segments); // top-right
        drawArc(x2 - radius,    y2 - radius,    radius,   0,  90, segments); // bottom-right
        drawArc(x + radius,     y2 - radius,    radius,  90, 180, segments); // bottom-left
        glColor(color);
        disableGL();
    }

    private static void drawArc(float cx, float cy, float r, float startDeg, float endDeg, int segments) {
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);

        GL11.glVertex2f(cx, cy);

        for (int i = 0; i <= segments; i++) {
            double angle = Math.toRadians(startDeg + (i * (endDeg - startDeg) / segments));
            GL11.glVertex2f((float) (cx + Math.cos(angle) * r), (float) (cy + Math.sin(angle) * r));
        }

        GL11.glEnd();
    }

    private static void drawQuad(float x1, float y1, float x2, float y2) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x2, y1);
        GL11.glVertex2f(x2, y2);
        GL11.glVertex2f(x1, y2);
        GL11.glEnd();
    }

    private static void enableGL() {
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL13.GL_MULTISAMPLE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static void disableGL() {
        GL11.glDisable(GL13.GL_SAMPLE_ALPHA_TO_COVERAGE);
        GL11.glDisable(GL13.GL_MULTISAMPLE);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    public static void drawBorderedRectangle(float startX, float startY, float endX, float endY, float width, int color, int borderColor) {
        drawRectangle(startX, startY, endX, endY, color);
        drawRectangle(startX + 0.5f - width, startY, startX + 0.5f, endY, borderColor);
        drawRectangle(startX + 0.5f - width, startY + 0.5f - width, endX - 0.5f + width, startY + 0.5f, borderColor);
        drawRectangle(endX - 0.5f, startY, endX - 0.5f + width, endY, borderColor);
        drawRectangle(startX + 0.5f - width, endY - 0.5f, endX - 0.5f + width, endY - 0.5f + width, borderColor);
    }

    public static void drawBorderCorneredRectangle(float startX, float startY, float endX, float endY, float width, int color, int borderColor) {
        drawRectangle(startX, startY, endX, endY, color);
        drawRectangle(startX + 2.5f - width, startY + 2, startX + 2.5f, endY - 2, borderColor);
        drawRectangle(startX + 2.5f - width, startY + 2.5f - width, endX - 2.5f + width, startY + 2.5f, borderColor);
        drawRectangle(endX - 2.5f, startY + 2, endX - 2.5f + width, endY - 2, borderColor);
        drawRectangle(startX + 2.5f - width, endY - 2.5f, endX - 2.5f + width, endY - 2.5f + width, borderColor);
    }

    public static void drawImg(ResourceLocation loc, double posX, double posY, double width, double height) {
        GlStateManager.pushMatrix();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        Minecraft.getMinecraft().getTextureManager().bindTexture(loc);
        float f = 1.0F / (float) width;
        float f1 = 1.0F / (float) height;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(posX, posY + height, 0.0D).tex(0.0F * f, (0.0F + (float) height) * f1).endVertex();
        worldrenderer.pos(posX + width, posY + height, 0.0D).tex((0.0F + (float) width) * f, (0.0F + (float) height) * f1).endVertex();
        worldrenderer.pos(posX + width, posY, 0.0D).tex((0.0F + (float) width) * f, 0.0F * f1).endVertex();
        worldrenderer.pos(posX, posY, 0.0D).tex(0.0F * f, 0.0F * f1).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
    }

    public static Color getRainbow(final int delay) {
        rainbowState = Math.ceil((double) ((System.currentTimeMillis() + delay) / 8L));
        rainbowState %= 270.0;
        return Color.getHSBColor((float) (rainbowState / 270.0), 0.4f, 1f);
    }

    public static int rgba(int r, int b, int g, int a) {
        return new Color(r, b, g, a).getRGB();
    }

    public static void updateDelta() {
        lastTime = System.currentTimeMillis();
    }

    public static void scissorBox(int x, int y, int width, int height) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int factor = sr.getScaleFactor();
        glEnable(GL_SCISSOR_TEST);
        glScissor(x * factor, (sr.getScaledHeight() - (y + height)) * factor, ((x + width) - x) * factor, ((y + height) - y) * factor);
    }

    public static void cropBox(float x, float y, float width, float height) {
        ScaledResolution scale = new ScaledResolution(Minecraft.getMinecraft());
        int factor = scale.getScaleFactor();
        glScissor((int) (x * factor), (int) ((scale.getScaledHeight() - height) * factor), (int) ((width - x) * factor), (int) ((height - y) * factor));
    }

    public static void makeCropBox(float left, float top, float right, float bottom) {
        glPushMatrix();
        glEnable(GL_SCISSOR_TEST);
        cropBox(left, top, right, bottom);
    }

    public static void destroyCropBox() {
        glDisable(GL_SCISSOR_TEST);
        glPopMatrix();
    }

    public static void scale(float x, float y, float scale) {
        Gui.drawRect(0, 0, 0, 0, 0);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, 1);
        GlStateManager.translate(-x, -y, 0);
    }

    public static void resetColor() {
        GlStateManager.color(1, 1, 1, 1);
    }

    public static void bindTexture(int texture) {
        glBindTexture(GL_TEXTURE_2D, texture);
    }

    public static Framebuffer createFramebuffer(Framebuffer framebuffer, boolean depth) {
        if (framebuffer == null || framebuffer.framebufferWidth != Minecraft.getMinecraft().displayWidth || framebuffer.framebufferHeight != Minecraft.getMinecraft().displayHeight) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight, depth);
        }
        return framebuffer;
    }

    public static void glColor(int color) {
        float alpha = ((color >> 24) & 0xFF) / 255f;
        float red = ((color >> 16) & 0xFF) / 255f;
        float green = ((color >> 8) & 0xFF) / 255f;
        float blue = ((color) & 0xFF) / 255f;

        GL11.glColor4f(red, green, blue, alpha);
    }

    public static void drawBlurredRect(BlurType type, double x, double y, double x1, double y1, float radius) {
        switch (type) {
            case KAWASE:
                StencilUtility.initStencilToWrite();
                enableGL2D();
                if (radius > 0)
                    drawRoundedRect((float) x, (float) y, (float) x1, (float) y1, radius, -1);
                else
                    Gui.drawRect(x, y, x1, y1, -1);
                disableGL2D();
                StencilUtility.readStencilBuffer(1);
                KawaseBlur.renderBlur(1, 8);
                StencilUtility.uninitStencilBuffer();
                break;
            case NORMAL:
                StencilUtility.initStencilToWrite();
                enableGL2D();
                drawRoundedRect((float) x, (float) y, (float) x1, (float) y1, radius, -1);
                disableGL2D();
                StencilUtility.readStencilBuffer(1);
                BlurShader.renderBlur(6);
                StencilUtility.uninitStencilBuffer();
                break;
        }
    }

    public static void enableGL2D() {
        glDisable(2929);
        glEnable(3042);
        glDisable(3553);
        glBlendFunc(770, 771);
        glDepthMask(true);
        glEnable(2848);
        glHint(3154, 4354);
        glHint(3155, 4354);
    }

    public static void disableGL2D() {
        glEnable(3553);
        glDisable(3042);
        glEnable(2929);
        glDisable(2848);
        glHint(3154, 4352);
        glHint(3155, 4352);
    }

    public enum BlurType {
        KAWASE, NORMAL
    }
}
