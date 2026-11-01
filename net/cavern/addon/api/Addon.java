package net.cavern.addon.api;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.cavern.Cavern;
import net.cavern.gui.toggles.Option;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class Addon {
    private final List<String> legacyIds = new ArrayList<>();
    private final List<Option> options = new ArrayList<>();
    public Minecraft mc = Minecraft.getMinecraft();
    // Identifier must be unique
    private String identifier, displayName;
    @Setter
    private boolean state;
    // Public so it can function with the custom gui
    public boolean hovered;

    public Addon (String identifier, String displayName){
        this.identifier = identifier;
        this.displayName = displayName;
    }

    public void setState(boolean state) {
        if (this.state == state) return;
        this.state = state;
        Cavern.INSTANCE.getFileManager().markDirty(); // swap for however you access your main instance
    }

    protected void addOption(Option... opts) {
        for (Option option : opts) {
            for (Option existing : options)
                if (existing.getId().equals(option.getId()))
                    throw new IllegalStateException("Duplicate option id '" + option.getId() + "' in " + identifier);
            options.add(option);
        }
    }

    // This is for my own sanity, IM LAZY OKAY
    protected void addLegacyId(String... ids) {
        legacyIds.addAll(Arrays.asList(ids));
    }

    public void saveSettings(JsonObject out) {}
    public void loadSettings(JsonObject in) {}

    public void displayHook(){}
    public void entityHook(){}
    public void packetHook(Packet packet){}
}
