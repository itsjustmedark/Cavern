package net.cavern.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

public class GlowRenderer {

    private static Framebuffer fbo1;
    private static Framebuffer fbo2;
    private static int blurShaderProgram = -1;
    private static long startTime = System.currentTimeMillis();

    // Call once during init or first use
    public static void initIfNeeded(int width, int height) {
        if (fbo1 == null || fbo1.framebufferWidth != width || fbo1.framebufferHeight != height) {
            if (fbo1 != null) fbo1.deleteFramebuffer();
            if (fbo2 != null) fbo2.deleteFramebuffer();
            fbo1 = new Framebuffer(width, height, false);
            fbo2 = new Framebuffer(width, height, false);
        }
        if (blurShaderProgram == -1) {
            blurShaderProgram = createBlurShader();
        }
    }

    /**
     * Renders a textured quad with a purple glow behind it.
     *
     * @param textureId  GL texture ID of your logo
     * @param x          screen X position
     * @param y          screen Y position
     * @param width      draw width
     * @param height     draw height
     * @param glowRadius blur radius (try 8-24)
     * @param animated   whether the glow pulses
     */
    public static void drawWithGlow(int textureId, float x, float y, float width, float height,
                                    int glowRadius, boolean animated) {
        Minecraft mc = Minecraft.getMinecraft();
        int screenW = mc.displayWidth;
        int screenH = mc.displayHeight;

        initIfNeeded(screenW, screenH);

        float intensity = 1.0f;
        if (animated) {
            float elapsed = (System.currentTimeMillis() - startTime) / 1000f;
            intensity = 0.65f + 0.35f * (float) Math.sin(elapsed * 1.8f);
        }

        // Pass 1: render logo into fbo1
        fbo1.bindFramebuffer(true);
        GL11.glClearColor(0, 0, 0, 0);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        drawQuad(x, y, width, height, screenW, screenH);
        fbo1.unbindFramebuffer();

        // Pass 2: horizontal blur fbo1 -> fbo2
        fbo2.bindFramebuffer(true);
        GL11.glClearColor(0, 0, 0, 0);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        GL20.glUseProgram(blurShaderProgram);
        GL20.glUniform1i(GL20.glGetUniformLocation(blurShaderProgram, "image"), 0);
        GL20.glUniform1i(GL20.glGetUniformLocation(blurShaderProgram, "horizontal"), 1);
        GL20.glUniform1i(GL20.glGetUniformLocation(blurShaderProgram, "radius"), glowRadius);
        GL20.glUniform1f(GL20.glGetUniformLocation(blurShaderProgram, "intensity"), intensity);
        GL20.glUniform2f(GL20.glGetUniformLocation(blurShaderProgram, "texelSize"), 1f / screenW, 1f / screenH);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo1.framebufferTexture);
        drawFullscreenQuad();
        fbo2.unbindFramebuffer();

        // Pass 3: vertical blur fbo2 -> fbo1
        fbo1.bindFramebuffer(true);
        GL11.glClearColor(0, 0, 0, 0);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        GL20.glUniform1i(GL20.glGetUniformLocation(blurShaderProgram, "horizontal"), 0);
        GL20.glUniform2f(GL20.glGetUniformLocation(blurShaderProgram, "texelSize"), 1f / screenW, 1f / screenH);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo2.framebufferTexture);
        drawFullscreenQuad();
        GL20.glUseProgram(0);
        fbo1.unbindFramebuffer();

        // Composite: additive glow behind original
        GlStateManager.enableBlend();
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE); // additive
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo1.framebufferTexture);
        drawQuad(0, 0, screenW, screenH, screenW, screenH);

        // Draw original logo on top
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        drawQuad(x, y, width, height, screenW, screenH);

        GlStateManager.disableBlend();
    }

    private static void drawQuad(float x, float y, float w, float h, int screenW, int screenH) {
        // Convert screen coords to NDC
        float x0 = (x / screenW) * 2f - 1f;
        float y0 = 1f - (y / screenH) * 2f;
        float x1 = ((x + w) / screenW) * 2f - 1f;
        float y1 = 1f - ((y + h) / screenH) * 2f;

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 1); GL11.glVertex2f(x0, y0);
        GL11.glTexCoord2f(1, 1); GL11.glVertex2f(x1, y0);
        GL11.glTexCoord2f(1, 0); GL11.glVertex2f(x1, y1);
        GL11.glTexCoord2f(0, 0); GL11.glVertex2f(x0, y1);
        GL11.glEnd();
    }

    private static void drawFullscreenQuad() {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0); GL11.glVertex2f(-1, -1);
        GL11.glTexCoord2f(1, 0); GL11.glVertex2f( 1, -1);
        GL11.glTexCoord2f(1, 1); GL11.glVertex2f( 1,  1);
        GL11.glTexCoord2f(0, 1); GL11.glVertex2f(-1,  1);
        GL11.glEnd();
    }

    private static int createBlurShader() {
        String vert =
                "#version 120\n" +
                        "varying vec2 TexCoords;\n" +
                        "void main() {\n" +
                        "    gl_Position = gl_Vertex;\n" +
                        "    TexCoords = gl_MultiTexCoord0.xy;\n" +
                        "}\n";

        String frag =
                "#version 120\n" +
                        "varying vec2 TexCoords;\n" +
                        "uniform sampler2D image;\n" +
                        "uniform bool horizontal;\n" +
                        "uniform int radius;\n" +
                        "uniform float intensity;\n" +
                        "uniform vec2 texelSize;\n" +
                        // Purple tint: rgb(90, 70, 124) normalized
                        "const vec3 glowColor = vec3(0.353, 0.275, 0.486);\n" +
                        "void main() {\n" +
                        "    vec2 texOffset = texelSize;\n" +
                        "    vec4 result = vec4(0.0);\n" +
                        "    float totalWeight = 0.0;\n" +
                        "    for (int i = -radius; i <= radius; i++) {\n" +
                        "        float weight = exp(-float(i * i) / (2.0 * float(radius) * float(radius)));\n" +
                        "        vec2 offset = horizontal\n" +
                        "            ? vec2(texOffset.x * float(i), 0.0)\n" +
                        "            : vec2(0.0, texOffset.y * float(i));\n" +
                        "        result += texture2D(image, TexCoords + offset) * weight;\n" +
                        "        totalWeight += weight;\n" +
                        "    }\n" +
                        "    result /= totalWeight;\n" +
                        "    gl_FragColor = vec4(result.rgb * glowColor * intensity, result.a * intensity);\n" +
                        "}\n";

        int vertShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertShader, vert);
        GL20.glCompileShader(vertShader);

        int fragShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragShader, frag);
        GL20.glCompileShader(fragShader);

        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertShader);
        GL20.glAttachShader(program, fragShader);
        GL20.glLinkProgram(program);

        GL20.glDeleteShader(vertShader);
        GL20.glDeleteShader(fragShader);

        return program;
    }

    // Called when shutting down / switching screens
    public static void cleanup() {
        if (fbo1 != null) { fbo1.deleteFramebuffer(); fbo1 = null; }
        if (fbo2 != null) { fbo2.deleteFramebuffer(); fbo2 = null; }
        if (blurShaderProgram != -1) { GL20.glDeleteProgram(blurShaderProgram); blurShaderProgram = -1; }
    }
}