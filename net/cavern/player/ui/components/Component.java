package net.cavern.player.ui.components;

import net.cavern.player.ui.CavernPlayerGui;

public class Component {

    public double x, y, width, height;
    public CavernPlayerGui parent;
    public int color = -1;

    public Component(double x, double y, double width, double height, CavernPlayerGui parent, int color){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.parent = parent;
        this.color = color;
    }

    public void onPressed(int mouseX, int mouseY, int key){
    }

    public void onRightClick(int key){
    }

    public void drawComponent(int mouseX, int mouseY, boolean hovered){
    }
}
