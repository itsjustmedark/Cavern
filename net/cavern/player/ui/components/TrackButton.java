package net.cavern.player.ui.components;

import lombok.var;
import net.cavern.Cavern;
import net.cavern.manager.FontManager;
import net.cavern.player.components.Track;
import net.cavern.player.ui.CavernPlayerGui;
import net.cavern.utils.FontType;
import net.cavern.utils.RenderingUtils;
import net.cavern.utils.ui.liquidglass.LiquidGlass;

import java.awt.Color;

public class TrackButton extends Component {
    public Track track;

    private final CavernPlayerGui gui;
    private final double offX, offY, boxW, boxH; // offsets are relative to the panel's top-left
    private final int baseColor;

    public TrackButton(double x, double y, double width, double height, CavernPlayerGui parent, Track track, int color) {
        super(x, y, width, height, parent, color);
        this.gui = parent;
        this.track = track;
        this.offX = x;
        this.offY = y;
        this.boxW = width;
        this.boxH = height;
        this.baseColor = color;
    }

    // Position is worked out every frame, so the open animation and scrolling just work
    public float absX() { return (float) (gui.x + offX); }
    public float absY() { return (float) (gui.y + offY - gui.scroll); }

    public boolean isMouseOver(int mx, int my) {
        float bx = absX(), by = absY();
        return mx >= bx && mx <= bx + boxW && my >= by && my <= by + boxH;
    }

    @Override
    public void drawComponent(int mouseX, int mouseY, boolean hovered) {
        float bx = absX(), by = absY(), bw = (float) boxW, bh = (float) boxH;

        // skip rows that are scrolled out of view
        float top = gui.y + 27, bottom = top + gui.visibleItems * gui.scrollHeight;
        if (by + bh < top || by > bottom) return;

        Track current = gui.getPlayer().getCurrent();
        boolean playing = current != null && current.getFile().equals(track.getFile());

        Color accent = Cavern.INSTANCE.getClientColorNonRGB();
        int bg = playing ? (hovered ? new Color(accent.getRed() - 20, accent.getGreen() - 20, accent.getBlue() - 20, 255).getRGB()
                : new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 255).getRGB())
                : hovered ? new Color(accent.getRed() - 20, accent.getGreen() - 20,
                accent.getBlue() - 20, 255).getRGB() : Color.TRANSLUCENT;
        RenderingUtils.drawRoundedRect(bx, by, bx + bw, by + bh, 8, bg);
        LiquidGlass.drawPanel(bx, by, bw, bh, 8, 4, 3, .6f, 0, 0, 0);

        var title = FontManager.getFont(FontType.MEDIUM, 18);
        var small = FontManager.getFont(FontType.MEDIUM, 14);
        float maxW = bw - 12;

        int titleColor = playing ? accent.brighter().brighter().getRGB() : baseColor;
        String artist = track.getArtist().isEmpty() ? "Unknown Artist" : track.getArtist();

        title.drawStringWithShadow(CavernPlayerGui.trim(track.getTitle(), maxW, title::getWidth), bx + 6, by + 3, titleColor);
        small.drawStringWithShadow(CavernPlayerGui.trim(artist, maxW, small::getWidth), bx + 6, by + 14,
                new Color(150, 150, 165).getRGB());
    }

    @Override
    public void onPressed(int mouseX, int mouseY, int key) {
        super.onPressed(mouseX, mouseY, key);
        if (key == 0) gui.getPlayer().play(track);
    }
}