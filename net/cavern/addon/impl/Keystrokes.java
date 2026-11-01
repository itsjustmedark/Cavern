package net.cavern.addon.impl;

import lombok.var;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.cavern.addon.api.Addon;

import static net.cavern.gui.keystrokes.Keystrokes.drawKeystrokes;

public class Keystrokes extends Addon {
    public Keystrokes() {
        super("keystrokes", "Keystrokes");
    }

    @Override
    public void displayHook() {
        var sr = new ScaledResolution(mc);
        drawKeystrokes(sr.getScaledWidth() - 66, sr.getScaledHeight() - (mc.currentScreen instanceof GuiChat ? 58 : 44));
        super.displayHook();
    }
}
