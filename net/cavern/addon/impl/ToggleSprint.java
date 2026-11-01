package net.cavern.addon.impl;

import lombok.var;
import net.cavern.utils.FontType;
import net.cavern.widget.LabelWidget;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.cavern.Cavern;
import net.cavern.addon.api.Addon;
import net.cavern.manager.FontManager;

public class ToggleSprint extends Addon {
    public ToggleSprint() {
        super("toggle_sprint", "Toggle Sprint");
        addLegacyId("togglesprint");
    }
    public String text = "Sprinting \247f(Toggled)";

    @Override
    public void entityHook() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), isState());
        super.entityHook();
    }

    @Override
    public void displayHook() {
        var scaledResolution = new ScaledResolution(mc);
        var font = FontManager.getFont(FontType.NORMAL, 16);
        LabelWidget label = new LabelWidget((int) (scaledResolution.getScaledWidth() - font.getWidth(text)), 2, 10, font, text, Cavern.INSTANCE.getClientColor());
        label.draw(scaledResolution);
        super.displayHook();
    }
}
