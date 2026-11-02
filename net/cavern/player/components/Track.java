package net.cavern.player.components;

import lombok.Getter;
import java.io.File;

@Getter
public class Track {
    private final File file;
    private volatile String title, artist = "", album = "";
    private volatile int durationSeconds;

    public Track(File file) {
        this.file = file;
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        this.title = dot > 0 ? name.substring(0, dot) : name; // filename until tags load
    }

    public void setMetadata(String title, String artist, String album, int seconds) {
        if (title != null && !title.trim().isEmpty()) this.title = title.trim();
        this.artist = artist == null ? "" : artist.trim();
        this.album = album == null ? "" : album.trim();
        this.durationSeconds = Math.max(0, seconds);
    }
}
