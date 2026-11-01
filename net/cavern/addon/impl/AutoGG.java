package net.cavern.addon.impl;

import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.cavern.addon.api.Addon;
import net.cavern.gui.toggles.Option;
import net.cavern.utils.MathUtils;

public class AutoGG extends Addon {
    public AutoGG() {
        super("auto_gg", "Auto GG");
        addLegacyId("agg");
    }

    public static Option fun = new Option("Fun Messages", false);

    @Override
    public void packetHook(Packet packet) {
        if (packet instanceof S02PacketChat)
            if (((S02PacketChat) packet).getChatComponent().getUnformattedText().contains("Reward Summary"))
                mc.thePlayer.sendChatMessage(fun.val ? funMessages() : "gg");
    }

    public String funMessages(){
        String[] messages = {"Damn well played mate", "GG I'll get ya next time", "Aw i totally had you, gg",
                "GG well played", "GG good fight mate", "GG good fight man"};
        return messages[MathUtils.randomInt(0, 5)];
    }
}
