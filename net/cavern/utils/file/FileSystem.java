package net.cavern.utils.file;

import com.google.gson.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class FileSystem {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path file, backup, temp;

    public FileSystem(Path file) {
        this.file = file;
        this.backup = file.resolveSibling(file.getFileName() + ".bak");
        this.temp = file.resolveSibling(file.getFileName() + ".tmp");
    }

    /** Returns the parsed config, falling back to the backup. Returns null if neither is usable. */
    public JsonObject read() {
        for (Path p : new Path[]{file, backup}) {
            if (!Files.exists(p)) continue;
            try {
                String text = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
                JsonElement el = new JsonParser().parse(text);
                if (el.isJsonObject()) return el.getAsJsonObject();
            } catch (Exception e) {
                System.err.println("[Cavern] Failed to read " + p.getFileName() + ": " + e);
            }
        }
        return null;
    }

    public void write(JsonObject root) throws IOException {
        Files.createDirectories(file.getParent());
        Files.write(temp, GSON.toJson(root).getBytes(StandardCharsets.UTF_8));

        if (Files.exists(file)) Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING);

        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}