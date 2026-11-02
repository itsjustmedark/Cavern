package net.cavern.player.components;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AudioTags {
    static { Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF); } // shut the fuck up
    private AudioTags() {}

    public static void readInto(Track track) {
        try {
            AudioFile f = AudioFileIO.read(track.getFile());
            Tag tag = f.getTag();
            int seconds = f.getAudioHeader().getTrackLength();
            // Default to empty strings so it's not null
            if (tag == null) { track.setMetadata("", "", "", seconds); return; }
            track.setMetadata(tag.getFirst(FieldKey.TITLE), tag.getFirst(FieldKey.ARTIST),
                    tag.getFirst(FieldKey.ALBUM), seconds);
        } catch (Throwable t) {
            System.err.println("[Cavern] [WARNING] Couldn't read tags for " + track.getFile().getName() + ": " + t);
        }
    }

    /** Returns a square, downscaled cover or null if the file has none. */
    public static BufferedImage readCover(File file) {
        try {
            Tag tag = AudioFileIO.read(file).getTag();
            if (tag == null) return null;
            Artwork art = tag.getFirstArtwork();
            if (art == null) return null;
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(art.getBinaryData()));
            return img == null ? null : toSquare(img, CoverArt.SIZE);
        } catch (Throwable t) {
            return null;
        }
    }

    /** I think this is fairly self-explanatory, we just make sure the cover art is a square */
    private static BufferedImage toSquare(BufferedImage src, int size) {
        int side = Math.min(src.getWidth(), src.getHeight());
        int sx = (src.getWidth() - side) / 2, sy = (src.getHeight() - side) / 2;
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, size, size, sx, sy, sx + side, sy + side, null);
        g.dispose();
        return out;
    }
}
