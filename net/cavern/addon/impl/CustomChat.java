package net.cavern.addon.impl;

import net.cavern.addon.api.Addon;

public class CustomChat extends Addon {
    public CustomChat() {
        super("custom_chat", "Custom Chat");
        addLegacyId("cchat");
    }
}
