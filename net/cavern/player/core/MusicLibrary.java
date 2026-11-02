package net.cavern.player.core;

import lombok.Getter;
import net.cavern.player.components.AudioTags;
import net.cavern.player.components.Track;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@Getter
public class MusicLibrary {
    private static final String[] EXTENSIONS = {".mp3", ".wav", ".ogg", ".flac"};
    private final File folder;
    private volatile List<Track> tracks = Collections.emptyList();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Cavern-Music-Scan");
        t.setDaemon(true);
        return t;
    });

    public MusicLibrary(File folder) { this.folder = folder; }

    public void scan() { executor.execute(this::scanNow); }

    private void scanNow() {
        try {
            folder.mkdirs();
            List<Track> found = new ArrayList<>();
            try (Stream<Path> walk = Files.walk(folder.toPath(), 4)) {
                walk.filter(Files::isRegularFile)
                        .filter(p -> isAudio(p.getFileName().toString().toLowerCase()))
                        .sorted(Comparator.comparing(p -> p.toString().toLowerCase()))
                        .forEach(p -> found.add(new Track(p.toFile())));
            }
            tracks = Collections.unmodifiableList(found);
            for (Track t : found) AudioTags.readInto(t);
        } catch (Exception e) {
            System.err.println("[Cavern] [ERROR] Music scan failed: " + e);
        }
    }

    private static boolean isAudio(String name) {
        for (String ext : EXTENSIONS) if (name.endsWith(ext)) return true;
        return false;
    }
}
