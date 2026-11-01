package net.cavern.addon.impl;

import net.cavern.addon.api.Addon;
import net.cavern.gui.join.JoinGamesGui;
import net.cavern.gui.toggles.CavernGui;

public class JoinGames extends Addon {
    public JoinGames() {
        super("join_games", "Join Games");
        addLegacyId("jg");
    }

    @Override
    public void displayHook() {
        if (mc.currentScreen instanceof CavernGui)
            mc.displayGuiScreen(new JoinGamesGui());
        setState(false);
        super.displayHook();
    }
}
