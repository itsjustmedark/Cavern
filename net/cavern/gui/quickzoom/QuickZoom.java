package net.cavern.gui.quickzoom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

public class QuickZoom {

    public static Minecraft mc;
    public static double currentFov;
    public static double baseFov;
    public static boolean initialized;
    public static boolean zooming;
    public static double fov;
    public KeyBinding bind;

    public static void init() {
        mc = Minecraft.getMinecraft();
        currentFov = mc.gameSettings.fovSetting;
        baseFov = mc.gameSettings.fovSetting;
        initialized = false;
        zooming = false;
    }
}
