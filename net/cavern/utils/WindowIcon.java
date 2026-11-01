package net.cavern.utils;

import org.lwjgl.opengl.Display;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class WindowIcon {

    public static void apply() {
        try {
            Display.setIcon(new ByteBuffer[] {
                    load("/assets/minecraft/Images/icon_16x16.png"),
                    load("/assets/minecraft/Images/icon_32x32.png")
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ByteBuffer load(String path) throws IOException {
        try (InputStream in = WindowIcon.class.getResourceAsStream(path)) {
            if (in == null) throw new IOException("Missing icon: " + path);

            BufferedImage img = ImageIO.read(in);
            int w = img.getWidth(), h = img.getHeight();
            int[] pixels = img.getRGB(0, 0, w, h, null, 0, w);

            ByteBuffer buf = ByteBuffer.allocateDirect(w * h * 4);
            for (int p : pixels) {
                buf.put((byte) ((p >> 16) & 0xFF)); // R
                buf.put((byte) ((p >> 8) & 0xFF));  // G
                buf.put((byte) (p & 0xFF));         // B
                buf.put((byte) ((p >> 24) & 0xFF)); // A
            }
            buf.flip();
            return buf;
        }
    }
}