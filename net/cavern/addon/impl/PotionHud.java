package net.cavern.addon.impl;

import lombok.var;
import net.cavern.utils.FontType;
import net.cavern.widget.LabelWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.cavern.Cavern;
import net.cavern.addon.api.Addon;
import net.cavern.manager.FontManager;

import java.util.HashMap;
import java.util.Map;

public class PotionHud extends Addon {
    public PotionHud() {
        super("potion_hud", "Potion Hud");
        addLegacyId("ph");
    }
    private static final Map<Integer, String> LEVEL_SUFFIXES = new HashMap<>();

    static {
        LEVEL_SUFFIXES.put(0, "");
        LEVEL_SUFFIXES.put(1, " II");
        LEVEL_SUFFIXES.put(2, " III");
        LEVEL_SUFFIXES.put(3, " IV");
        LEVEL_SUFFIXES.put(4, " V");
    }

    @Override
    public void displayHook() {
        getAndRenderPotions();
        super.displayHook();
    }

    public void getAndRenderPotions() {
        int offset = 0;

        // Cache potion effects and level suffixes
        var activePotionEffects = mc.thePlayer.getActivePotionEffects();

        for (PotionEffect effect : activePotionEffects) {
            var potion = Potion.potionTypes[effect.getPotionID()];
            var level = I18n.format(potion.getName());
            var font = FontManager.getFont(FontType.NORMAL, 17);

            // Use Map to get the suffix
            String suffix = LEVEL_SUFFIXES.getOrDefault(effect.getAmplifier(), "");
            level += suffix;
            level += ":\247f " + Potion.getDurationString(effect);

            // Draw the potion effect
            var label = new LabelWidget(2, 120 + offset, 11, font, level, Cavern.INSTANCE.getClientColor());
            label.draw();
            offset += 11;
        }
    }
}
