package net.cavern.widget;

import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class Widget {
    public int x, y, height;
    public Color background;

    public Widget(int x, int y, int height, Color background) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.background = background;
    }

    public Widget(int x, int y, int height) {
        this.x = x;
        this.y = y;
        this.height = height;
    }

    public void draw(ScaledResolution scaledResolution) {}
    public void draw() {}
}
