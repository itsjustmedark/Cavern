package net.cavern.player.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoverArt {
    public static final int SIZE = 128;

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Object lock = new Object();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Cavern-Music-Cover");
        t.setDaemon(true);
        return t;
    });

    private int generation;
    private BufferedImage pending;
    private boolean clearRequested;

    private DynamicTexture texture;     // render thread only
    private ResourceLocation location;  // render thread only

    public void load(File file) {
        final int gen = reset();
        executor.execute(() -> {
            BufferedImage img = AudioTags.readCover(file);
            synchronized (lock) { if (gen == generation) pending = img; }
        });
    }

    public void clear() { reset(); }

    private int reset() {
        synchronized (lock) { pending = null; clearRequested = true; return ++generation; }
    }

    public ResourceLocation get() {
        BufferedImage img;
        boolean clear;
        synchronized (lock) {
            img = pending; pending = null;
            clear = clearRequested; clearRequested = false;
        }
        if (clear || img != null) release();
        if (img != null) {
            texture = new DynamicTexture(img);
            location = mc.getTextureManager().getDynamicTextureLocation("cavern_cover", texture);
            GlStateManager.bindTexture(texture.getGlTextureId());
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        }
        return location;
    }

    private void release() {
        if (location != null) mc.getTextureManager().deleteTexture(location);
        location = null;
        texture = null;
    }
}
