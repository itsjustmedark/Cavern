package net.cavern.gui.blur.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import net.cavern.utils.RenderingUtils;
import net.cavern.utils.ShaderUtil;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class KawaseBlur {

    public static ShaderUtil kawaseDown = new ShaderUtil("fragment/kawaseDown.frag");
    public static ShaderUtil kawaseUp = new ShaderUtil("fragment/kawaseUp.frag");

    public static Framebuffer framebuffer = new Framebuffer(1, 1, false);


    public static void setupUniforms(float offset) {
        kawaseDown.setUniformf("offset", offset, offset);
        kawaseUp.setUniformf("offset", offset, offset);
    }

    private static int currentIterations;

    private static final List<Framebuffer> framebufferList = new ArrayList<>();

    private static void initFramebuffers(float iterations) {
        for (Framebuffer framebuffer : framebufferList) {
            framebuffer.deleteFramebuffer();
        }
        framebufferList.clear();

        framebufferList.add(RenderingUtils.createFramebuffer(framebuffer, true));


        for (int i = 1; i <= iterations; i++) {
            Framebuffer framebuffer = new Framebuffer(Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight, false);
            framebuffer.setFramebufferFilter(GL11.GL_LINEAR);
            framebufferList.add(RenderingUtils.createFramebuffer(framebuffer, true));
        }
    }


    public static void renderBlur(int iterations, int offset) {
        if (currentIterations != iterations) {
            initFramebuffers(iterations);
            currentIterations = iterations;
        }

        renderFBO(framebufferList.get(1), Minecraft.getMinecraft().getFramebuffer().framebufferTexture, kawaseDown, offset);

        //Downsample
        for (int i = 1; i < iterations; i++) {
            renderFBO(framebufferList.get(i + 1), framebufferList.get(i).framebufferTexture, kawaseDown, offset);
        }

        //Upsample
        for (int i = iterations; i > 1; i--) {
            renderFBO(framebufferList.get(i - 1), framebufferList.get(i).framebufferTexture, kawaseUp, offset);
        }


        Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);

        RenderingUtils.bindTexture(framebufferList.get(1).framebufferTexture);
        kawaseUp.init();
        kawaseUp.setUniformf("offset", offset, offset);
        kawaseUp.setUniformf("halfpixel", 0.5f / Minecraft.getMinecraft().displayWidth, 0.5f / Minecraft.getMinecraft().displayHeight);
        kawaseUp.setUniformi("inTexture", 0);
        ShaderUtil.drawQuads();
        kawaseUp.unload();

    }

    private static void renderFBO(Framebuffer framebuffer, int framebufferTexture, ShaderUtil shader, float offset) {
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);
        shader.init();
        RenderingUtils.bindTexture(framebufferTexture);
        shader.setUniformf("offset", offset, offset);
        shader.setUniformi("inTexture", 0);
        shader.setUniformf("halfpixel", 0.5f / Minecraft.getMinecraft().displayWidth, 0.5f / Minecraft.getMinecraft().displayHeight);
        ShaderUtil.drawQuads();
        shader.unload();
        framebuffer.unbindFramebuffer();
    }
}