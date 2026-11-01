package net.cavern.utils.ui.liquidglass;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

public class LiquidGlass {
    // Keep these in sync with the real filenames (classpath lookups are case-sensitive in a jar).
    private static final String VERT_PATH = "/assets/minecraft/shaders/vertex/liquidGlass.vert";
    private static final String FRAG_PATH = "/assets/minecraft/shaders/fragment/liquidGlass.frag";

    private static int program;
    private static int sceneTexture;

    private static int textureWidth;
    private static int textureHeight;

    private static int uScene;
    private static int uResolution;
    private static int uGuiResolution;
    private static int uTime;

    private static int uRect;
    private static int uRadius;
    private static int uBlur;
    private static int uDistortion;
    private static int uOpacity;
    private static int uTint;

    private static boolean initialized;
    private static boolean failed;      // set if shader setup threw, so we don't retry + spam every frame
    private static boolean frameActive;

    private static int previousProgram;

    /**
     * Safe to call more than once. If Minecraft's GL context doesn't exist yet it just returns,
     * and the first beginFrame() after that will initialize.
     */
    public static void init() {
        if (initialized || failed) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.displayWidth <= 0 || mc.displayHeight <= 0) return;

        try {
            program = createProgram(VERT_PATH, FRAG_PATH);

            uScene = GL20.glGetUniformLocation(program, "sceneTexture");
            uResolution = GL20.glGetUniformLocation(program, "resolution");
            uGuiResolution = GL20.glGetUniformLocation(program, "guiResolution");
            uTime = GL20.glGetUniformLocation(program, "time");

            uRect = GL20.glGetUniformLocation(program, "rect");
            uRadius = GL20.glGetUniformLocation(program, "radius");
            uBlur = GL20.glGetUniformLocation(program, "blur");
            uDistortion = GL20.glGetUniformLocation(program, "distortion");
            uOpacity = GL20.glGetUniformLocation(program, "opacity");
            uTint = GL20.glGetUniformLocation(program, "tint");

            resize(mc.displayWidth, mc.displayHeight);

            initialized = true;
        } catch (RuntimeException e) {
            // Previously this threw every frame (or got swallowed upstream). Log once and stop.
            failed = true;
            System.err.println("[LiquidGlass] Initialization failed, glass disabled:");
            e.printStackTrace();
        }
    }

    /**
     * (Re)creates the scene texture when the window size changes.
     * No FBO is needed: we only copy the screen into this texture with glCopyTexSubImage2D.
     * Binding is saved and restored so Minecraft's own state is untouched.
     */
    public static void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        if (width == textureWidth && height == textureHeight && sceneTexture != 0) return;

        int previousBinding = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

        destroyTexture();

        textureWidth = width;
        textureHeight = height;

        sceneTexture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, sceneTexture);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8,
                width, height, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
                (ByteBuffer) null
        );

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousBinding);
    }

    /**
     * Starts a glass frame: captures the already-rendered screen ONCE, then sets up GL state.
     * Draw as many panels as you like, then call endFrame().
     * If you call drawPanel() without a frame active, it will do begin/end for you.
     */
    public static void beginFrame() {
        if (frameActive) return;

        if (!initialized) init();
        if (!initialized) return;

        Minecraft mc = Minecraft.getMinecraft();
        resize(mc.displayWidth, mc.displayHeight);
        if (sceneTexture == 0) return;

        // Push FIRST so everything below (including the texture bind) is restored by endFrame().
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        // Capture the currently bound framebuffer into the scene texture.
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, sceneTexture);
        GL11.glCopyTexSubImage2D(
                GL11.GL_TEXTURE_2D, 0,
                0, 0, 0, 0,
                textureWidth, textureHeight
        );

        // Raw GL calls on purpose: GlStateManager caches state, and glPopAttrib would
        // restore real GL state behind its back, leaving the cache wrong.
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_ALPHA_TEST);

        GL11.glEnable(GL11.GL_BLEND);
        GL14.glBlendFuncSeparate(
                GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA
        );

        GL11.glEnable(GL11.GL_TEXTURE_2D);

        previousProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        GL20.glUseProgram(program);

        // Per-frame uniforms.
        ScaledResolution sr = new ScaledResolution(mc);
        GL20.glUniform1i(uScene, 0);
        GL20.glUniform2f(uResolution, textureWidth, textureHeight);
        GL20.glUniform2f(uGuiResolution, sr.getScaledWidth(), sr.getScaledHeight());
        GL20.glUniform1f(uTime, (System.nanoTime() / 1_000_000_000.0F) % 1000.0F);

        frameActive = true;
    }

    /**
     * Draws a rounded liquid-glass panel in GuiScreen coordinates.
     *
     * x/y        top-left in GUI coordinates
     * radius     corner radius in GUI pixels
     * blur       blur kernel size in screen pixels
     * distortion refraction strength
     * opacity    how strongly the tint shows (0..1)
     * r,g,b      tint, 0..1
     */
    public static void drawPanel(
            float x, float y, float width, float height,
            float radius, float blur, float distortion, float opacity,
            float r, float g, float b
    ) {
        boolean ownsFrame = !frameActive;

        if (ownsFrame) beginFrame();
        if (!frameActive) return; // init failed; nothing was pushed, so nothing to pop

        GL20.glUniform4f(uRect, x, y, width, height);
        GL20.glUniform1f(uRadius, Math.max(0.0F, Math.min(radius, Math.min(width, height) * 0.5F)));
        GL20.glUniform1f(uBlur, Math.max(0.0F, blur));
        GL20.glUniform1f(uDistortion, Math.max(0.0F, distortion));
        GL20.glUniform1f(uOpacity, Math.max(0.0F, Math.min(1.0F, opacity)));
        GL20.glUniform3f(uTint, r, g, b);

        // The vertex shader maps this unit quad to the whole screen (ignoring matrices);
        // the fragment shader discards everything outside the rounded rect.
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(0.0F, 0.0F);
        GL11.glVertex2f(1.0F, 0.0F);
        GL11.glVertex2f(1.0F, 1.0F);
        GL11.glVertex2f(0.0F, 1.0F);
        GL11.glEnd();

        if (ownsFrame) endFrame();
    }

    /** Convenient premade panel. */
    public static void drawPanel(float x, float y, float width, float height, float radius) {
        drawPanel(
                x, y, width, height,
                radius,
                2.0F,
                1.0F,
                0.70F,
                0.82F, 0.93F, 1.0F
        );
    }

    /** Ends the glass frame and restores the previous OpenGL state. */
    public static void endFrame() {
        if (!frameActive) return;

        GL20.glUseProgram(previousProgram);

        GL11.glPopAttrib();

        frameActive = false;
    }

    /** Call on shutdown for explicit GL cleanup. */
    public static void destroy() {
        frameActive = false;

        if (program != 0) {
            GL20.glDeleteProgram(program);
            program = 0;
        }

        destroyTexture();

        initialized = false;
        failed = false;
    }

    private static void destroyTexture() {
        if (sceneTexture != 0) {
            GL11.glDeleteTextures(sceneTexture);
            sceneTexture = 0;
        }
        textureWidth = 0;
        textureHeight = 0;
    }

    private static int createProgram(String vertexPath, String fragmentPath) {
        int vertex = compile(GL20.GL_VERTEX_SHADER, vertexPath);
        int fragment = compile(GL20.GL_FRAGMENT_SHADER, fragmentPath);

        int p = GL20.glCreateProgram();

        GL20.glAttachShader(p, vertex);
        GL20.glAttachShader(p, fragment);
        GL20.glLinkProgram(p);

        if (GL20.glGetProgrami(p, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            String log = GL20.glGetProgramInfoLog(p, 8192);
            GL20.glDeleteProgram(p);
            throw new RuntimeException("UI liquid glass shader link failed:\n" + log);
        }

        GL20.glDeleteShader(vertex);
        GL20.glDeleteShader(fragment);

        return p;
    }

    private static int compile(int type, String path) {
        String source = readResource(path);

        int shader = GL20.glCreateShader(type);

        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);

        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(shader, 8192);
            GL20.glDeleteShader(shader);
            throw new RuntimeException("UI liquid glass shader compile failed: " + path + "\n" + log);
        }

        return shader;
    }

    private static String readResource(String path) {
        InputStream stream = LiquidGlass.class.getResourceAsStream(path);

        if (stream == null) {
            throw new RuntimeException("Missing shader resource: " + path);
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            StringBuilder result = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) result.append(line).append('\n');

            reader.close();
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException("Could not read shader: " + path, e);
        }
    }
}