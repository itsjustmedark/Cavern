package net.cavern.manager;

import lombok.var;
import net.cavern.utils.CustomFontRenderer;
import net.cavern.utils.FontType;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class FontManager {
    private static final String FONT_LOCATION = "/assets/minecraft/font/arial.ttf", FONT_LOCATION_MED = "/assets/minecraft/font/arialmed.ttf";
    private static final Map<Integer, CustomFontRenderer> regular = new ConcurrentHashMap<>();
    private static final Map<Integer, CustomFontRenderer> medium = new ConcurrentHashMap<>();

    public static CustomFontRenderer getFont(FontType fontType, int size) {
        return regular.computeIfAbsent(size, s -> create(getFontType(fontType), s));
    }

    private static String getFontType(FontType fontType) {
        var ft = "not initialized";
        if  (fontType == FontType.NORMAL) ft = FONT_LOCATION;
        if  (fontType == FontType.MEDIUM) ft = FONT_LOCATION_MED;
        // Defaulting to FONT_LOCATION just to avoid crashing if shit hits the fan.
        if (fontType != FontType.NORMAL && fontType != FontType.MEDIUM) ft = FONT_LOCATION;
        return ft;
    }

    private static CustomFontRenderer create(String path, int size) {
        try (InputStream is = FontManager.class.getResourceAsStream(path)) {
            if (is == null) throw new IOException("Missing font resource: " + path);
            Font awtFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.PLAIN, size);
            return new CustomFontRenderer(awtFont);
        } catch (IOException | FontFormatException e) {
            // Failing loudly here beats returning null: computeIfAbsent won't cache a null,
            // and the caller would NPE somewhere unrelated on the next draw.
            throw new IllegalStateException("Could not load font " + path + " @" + size, e);
        }
    }
}