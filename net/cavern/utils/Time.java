package net.cavern.utils;

public class Time {
    private static long lastTime = System.nanoTime();
    private static boolean updated = false;
    public static float delta = 0f;

    public static void update() {
        if (updated) return;
        long now = System.nanoTime();
        delta = Math.min((now - lastTime) / 1_000_000_000f, 0.05f); // capped at 50ms
        lastTime = now;
        updated = true;
    }

    public static void reset() {
        updated = false;
    }
}