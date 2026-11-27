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
    private float textWidth = -1;

    @Override
    public void entityHook() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), isState());
        super.entityHook();
    }

    @Override
    public void displayHook() {
        var font = FontManager.getFont(FontType.NORMAL, 16);
        if (textWidth < 0) textWidth = font.getWidth(text);
        font.drawStringWithShadow(text, (int) (mc.ingameGUI.scaledRes.getScaledWidth() - textWidth), 2, Cavern.INSTANCE.getClientColor());
        super.displayHook();
    }
}
