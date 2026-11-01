package net.cavern.addon.impl;

import net.cavern.addon.api.Addon;

public class CustomHotbar extends Addon {
    public CustomHotbar() {
        super("custom_hotbar", "Custom Hotbar");
        addLegacyId("ch");
    }
}
