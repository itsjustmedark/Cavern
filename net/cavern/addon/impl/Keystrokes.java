package net.cavern.addon.impl;

import lombok.var;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.cavern.addon.api.Addon;

public class Keystrokes extends Addon {
    public final net.cavern.gui.keystrokes.Keystrokes keystrokes = new net.cavern.gui.keystrokes.Keystrokes();

    public Keystrokes() {
        super("keystrokes", "Keystrokes");
    }

    @Override
    public void displayHook() {
        var sr = mc.ingameGUI.scaledRes;
        keystrokes.drawKeystrokes(sr.getScaledWidth() - 66, sr.getScaledHeight() - (mc.currentScreen instanceof GuiChat ? 58 : 44));
        super.displayHook();
    }
}
