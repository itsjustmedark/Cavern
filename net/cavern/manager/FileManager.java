package net.cavern.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.cavern.utils.file.Configurable;
import net.cavern.utils.file.FileSystem;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class FileManager {

    public static final int VERSION = 1;
    private static final long SAVE_DELAY_MS = 2000;

    private final FileSystem file;
    private final Map<String, Configurable> sections = new LinkedHashMap<>();
    private JsonObject loadedSections = new JsonObject(); // keeps sections we don't own (unknown/removed addons etc.)

    private boolean dirty;
    private long dirtySince;

    public FileManager(Path path) {
        this.file = new FileSystem(path);
    }

    public void register(String key, Configurable section) {
        if (sections.putIfAbsent(key, section) != null)
            throw new IllegalStateException("Duplicate config section: " + key);
    }

    /** Call once after everything is registered. */
    public void load() {
        JsonObject root = file.read();
        if (root == null) return; // first launch, defaults stay

        // if (root.get("version").getAsInt() < VERSION) migrate(root);  <- future migrations go here

        if (root.has("sections") && root.get("sections").isJsonObject())
            loadedSections = root.getAsJsonObject("sections");

        for (Map.Entry<String, Configurable> e : sections.entrySet()) {
            JsonElement el = loadedSections.get(e.getKey());
            if (el == null || !el.isJsonObject()) continue;
            try {
                e.getValue().loadConfig(el.getAsJsonObject());
            } catch (Exception ex) {
                System.err.println("[Cavern] Failed to load section '" + e.getKey() + "': " + ex);
            }
        }
    }

    public synchronized void save() {
        JsonObject out = new JsonObject();
        for (Map.Entry<String, JsonElement> e : loadedSections.entrySet()) // keep unknown sections
            out.add(e.getKey(), e.getValue());

        for (Map.Entry<String, Configurable> e : sections.entrySet()) {
            try {
                JsonObject section = new JsonObject();
                e.getValue().saveConfig(section);
                out.add(e.getKey(), section);
            } catch (Exception ex) {
                System.err.println("[Cavern] Failed to save section '" + e.getKey() + "': " + ex);
            }
        }

        JsonObject root = new JsonObject();
        root.addProperty("version", VERSION);
        root.add("sections", out);

        try {
            file.write(root);
            loadedSections = out;
            dirty = false;
        } catch (IOException ex) {
            System.err.println("[Cavern] Failed to write config: " + ex);
        }
    }

    public synchronized void markDirty() {
        if (!dirty) dirtySince = System.currentTimeMillis();
        dirty = true;
    }

    /** Call every frame/tick (e.g. from your displayHook loop). */
    public synchronized void tick() {
        if (dirty && System.currentTimeMillis() - dirtySince >= SAVE_DELAY_MS) save();
    }
}
