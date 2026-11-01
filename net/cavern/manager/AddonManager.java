package net.cavern.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.var;
import net.cavern.addon.api.Addon;
import net.cavern.addon.impl.*;
import net.cavern.gui.toggles.Option;
import net.cavern.utils.file.Configurable;

import java.util.*;

public class AddonManager implements Configurable {
    // LinkedHashMap keeps registration order so the gui list order stays the same. never again.
    private final Map<String, Addon> byId = new LinkedHashMap<>();
    private final Map<Class<? extends Addon>, Addon> byClass = new LinkedHashMap<>();

    public AddonManager() {
        insertAddon(new InfoHud());
        insertAddon(new Keystrokes());
        insertAddon(new ToggleSprint());
        insertAddon(new BedwarsHud());
        insertAddon(new AutoGG());
        insertAddon(new ItemPhysics());
        insertAddon(new JoinGames());
        insertAddon(new PotionHud());
        insertAddon(new CustomHotbar());
        insertAddon(new CustomChat());
    }

    public void insertAddon(Addon addon) {
        if (byId.putIfAbsent(addon.getIdentifier(), addon) != null)
            throw new IllegalStateException("[CAVERN] [ERROR]: DUPLICATE ADDON | ID: " + addon.getIdentifier());
        byClass.put(addon.getClass(), addon);
    }

    public Collection<Addon> getAddons() {
        return Collections.unmodifiableCollection(byId.values());
    }

    public Addon getAddon(String identifier) {
        var a = byId.get(identifier);
        if (a == null) throw new IllegalArgumentException("[CAVERN] [ERROR]: UNKNOWN ADDON | ID: " + identifier);
        return a;
    }

    // Please for the love of god never have to use this, but I'm having it JUST in case.
    @SuppressWarnings("unchecked")
    public <T extends Addon> T getAddon(Class<T> type) {
        return (T) byClass.get(type);
    }

    @Override
    public void saveConfig(JsonObject out) {
        for (Addon addon : byId.values()) {
            JsonObject o = new JsonObject();
            o.addProperty("enabled", addon.isState());
            if (!addon.getOptions().isEmpty()) {
                JsonObject opts = new JsonObject();
                for (Option opt : addon.getOptions()) opts.addProperty(opt.getId(), opt.val);
                o.add("options", opts);
            }
            JsonObject settings = new JsonObject();
            addon.saveSettings(settings);
            if (!settings.entrySet().isEmpty()) o.add("settings", settings);
            out.add(addon.getIdentifier(), o);
        }
    }

    @Override
    public void loadConfig(JsonObject in) {
        for (Addon addon : byId.values()) {
            // first try the current identifier
            JsonElement el = in.get(addon.getIdentifier());

            // fall back to legacy identifiers pre 1.3.0
            if (el == null) {
                for (String legacy : addon.getLegacyIds()) {
                    el = in.get(legacy);
                    if (el != null) break;
                }
            }
            if (el == null || !el.isJsonObject()) continue; // new addon, keep defaults
            JsonObject o = el.getAsJsonObject();
            if (o.has("enabled")) addon.setState(o.get("enabled").getAsBoolean());
            if (o.has("options") && o.get("options").isJsonObject()) {
                JsonObject opts = o.getAsJsonObject("options");
                for (Option opt : addon.getOptions()) {
                    JsonElement v = opts.get(opt.getId());
                    if (v != null && v.isJsonPrimitive()) {
                        opt.val = v.getAsBoolean();
                        opt.optState = opt.val ? 1f : 0f;
                    }
                }
            }
            if (o.has("settings") && o.get("settings").isJsonObject()) {
                try { addon.loadSettings(o.getAsJsonObject("settings")); }
                catch (Exception e) { System.err.println("[Cavern] Bad settings for " + addon.getIdentifier()); }
            }
        }
    }
}
