package net.cavern.player.ui;

import lombok.Getter;
import lombok.var;
import net.cavern.Cavern;
import net.cavern.manager.FontManager;
import net.cavern.player.components.CoverArt;
import net.cavern.player.core.CavernPlayer;
import net.cavern.player.components.Track;
import net.cavern.player.ui.components.TrackButton;
import net.cavern.utils.FontType;
import net.cavern.utils.RenderingUtils;
import net.cavern.utils.Time;
import net.cavern.utils.ui.Animation;
import net.cavern.utils.ui.Animation.AnimationType;
import net.cavern.utils.ui.liquidglass.LiquidGlass;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;

public class CavernPlayerGui extends GuiScreen {
    private static final float PANEL_W = 380, PANEL_H = 200;
    private static final float BTN_H = 20, BTN_SIDE_W = 28, BTN_PLAY_W = 36, BTN_GAP = 8, BTN_SHUF_W = 44;

    public float x, y, width, height, scroll, scrollTarget = 0, scrollHeight = 28, visibleItems = 6, totalItems = 0, maxScroll = 0f;
    public Animation widthAnim = new Animation(), heightAnim = new Animation(), scrollAnim = new Animation();
    public List<TrackButton> trackButtons = new ArrayList<>();

    @Getter
    private final CavernPlayer player = Cavern.INSTANCE.getPlayer();

    private boolean draggingVolume, draggingSeek;
    private int lastMouseX, lastMouseY;
    private List<Track> shownTracks;

    // right-hand layout, recomputed every frame
    private float rx, rw, barY, btnY, prevX, playX, nextX, volX, volY, volW, rescanX, rescanY, seekFrac, smoothPosMs;

    @Override
    public void initGui() {
        super.initGui();
        widthAnim.start(AnimationType.SMOOTHSTEP, 0f, PANEL_W, 0.325f);
        heightAnim.start(AnimationType.SMOOTHSTEP, 0f, PANEL_H, 0.325f);
        scroll = 0;
        scrollTarget = 0;
        shownTracks = null; // force the list to rebuild
        draggingVolume = false;
        draggingSeek = false;
        volTw = new Tween(); seekTw = new Tween(); volHoverTw = new Tween(); seekHoverTw = new Tween();
        lastTrack = null;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        ScaledResolution sr = new ScaledResolution(mc);

        if (widthAnim.isRunning()) width = widthAnim.update(Time.delta);
        if (heightAnim.isRunning()) height = heightAnim.update(Time.delta);
        x = sr.getScaledWidth() / 2f - width / 2;
        y = sr.getScaledHeight() / 2f - height / 2;
        if (scrollAnim.isRunning()) scroll = scrollAnim.update(Time.delta);

        syncTrackList();
        layout();

        float right = sr.getScaledWidth() / 2f + width / 2, bottom = sr.getScaledHeight() / 2f + height / 2;

        RenderingUtils.makeCropBox(x, y, right, bottom);
        renderBackground();
        drawRescan();
        RenderingUtils.destroyCropBox();

        RenderingUtils.makeCropBox(x, y + 27, right, bottom);
        drawList(mouseX, mouseY);
        drawNowPlaying(mouseX, mouseY);
        RenderingUtils.destroyCropBox();
    }

    // ------------------------------------------------------------------ layout

    /** The library swaps in a new list when a scan finishes, so a reference check is enough. */
    private void syncTrackList() {
        List<Track> tracks = player.getLibrary().getTracks();
        if (tracks == shownTracks) return;
        shownTracks = tracks;

        trackButtons.clear();
        for (int i = 0; i < tracks.size(); i++)
            trackButtons.add(new TrackButton(10, 30 + i * scrollHeight, 122, 24, this, tracks.get(i), -1));

        totalItems = tracks.size();
        maxScroll = Math.max(0, (totalItems - visibleItems) * scrollHeight);
        scrollTarget = Math.min(scrollTarget, maxScroll);
        scroll = Math.min(scroll, maxScroll);
    }

    private void layout() {
        rx = x + 146;
        rw = width - 146 - 10;
        barY = y + 112;
        btnY = y + 138;
        float total = BTN_SIDE_W * 2 + BTN_PLAY_W + BTN_GAP * 2;
        prevX = rx + (rw - total) / 2;
        playX = prevX + BTN_SIDE_W + BTN_GAP;
        nextX = playX + BTN_PLAY_W + BTN_GAP;
        volY = y + 176;
        volX = rx + 26;
        volW = rw - 26;
        rescanX = x + width - 58;
        rescanY = y + 7;
    }

    // ----------------------------------------------------------------- drawing

    private void renderBackground() {
        var titleFont = FontManager.getFont(FontType.MEDIUM, 32);
        float scrollbarY = y + 27;
        float scrollbarHeight = (visibleItems * scrollHeight) - 2;

        LiquidGlass.drawPanel(x, y, width, height, 10, 6, 8, 1.0f, 0.039f, 0.039f, 0.058f);
        LiquidGlass.drawPanel(x + 136, y + 26, 0, height, 0, 4, 0, 1.0f, .2f, .2f, .2f);
        LiquidGlass.drawPanel(x, y + 25, width, 0, 0, 4, 0, 1.0f, .2f, .2f, .2f);

        if (maxScroll > 0) { // no scrollbar when everything fits (also avoids dividing by zero)
            float thumbHeight = Math.max(10, scrollbarHeight * (visibleItems * scrollHeight / (totalItems * scrollHeight)));
            float thumbY = scrollbarY + (scroll / maxScroll) * (scrollbarHeight - thumbHeight);
            LiquidGlass.drawPanel(x + 2, scrollbarY, 5, scrollbarHeight, 0, 4, 0, 1.0f, 0, 0, 0);
            LiquidGlass.drawPanel(x + 3, thumbY + 1, 3, thumbHeight - 1, 0, 4, 0, 1.0f, .2f, .2f, .2f);
        }
        titleFont.drawCenteredStringWithShadow("Cavern Player", x + width / 2, y + 5, new Color(200, 200, 200).getRGB());
    }

    private void drawRescan() {
        var small = FontManager.getFont(FontType.MEDIUM, 14);
        boolean hov = over(lastMouseX, lastMouseY, rescanX, rescanY, 48, 14);
        RenderingUtils.drawRoundedRect(rescanX, rescanY, rescanX + 48, rescanY + 14, 5,
                new Color(255, 255, 255, hov ? 40 : 18).getRGB());
        small.drawCenteredStringWithShadow("Rescan", rescanX + 24, rescanY + 3, new Color(200, 200, 200).getRGB());
    }

    private void drawList(int mouseX, int mouseY) {
        if (trackButtons.isEmpty()) {
            var small = FontManager.getFont(FontType.MEDIUM, 14);
            int grey = new Color(150, 150, 165).getRGB();
            small.drawCenteredStringWithShadow("No music found", x + 68, y + 70, grey);
            small.drawCenteredStringWithShadow("Drop files in cavern/Music", x + 68, y + 84, grey);
            small.drawCenteredStringWithShadow("then press Rescan", x + 68, y + 98, grey);
            return;
        }
        for (TrackButton b : trackButtons)
            b.drawComponent(mouseX, mouseY, inList(mouseX, mouseY) && b.isMouseOver(mouseX, mouseY));
    }

    private void drawNowPlaying(int mouseX, int mouseY) {
        var jumbo = FontManager.getFont(FontType.MEDIUM, 34);
        var big = FontManager.getFont(FontType.MEDIUM, 22);
        var mid = FontManager.getFont(FontType.MEDIUM, 18);
        var small = FontManager.getFont(FontType.MEDIUM, 14);
        int white = -1;
        int grey = new Color(170, 170, 180).getRGB();
        int dim = new Color(120, 120, 135).getRGB();
        Color ac = Cavern.INSTANCE.getClientColorNonRGB();
        int accent = ac.getRGB();
        int barBg = new Color(60, 60, 75).getRGB();

        Track t = player.getCurrent();
        float dt = Math.min(0.1f, (float) Time.delta);

        // cover (get() must run on the render thread; it's fine to call every frame)
        float cs = 72, cy = y + 32;
        ResourceLocation cover = player.getCover().get();
        if (t != null && cover != null) {
            GlStateManager.enableTexture2D();
            GlStateManager.color(1f, 1f, 1f, 1f);
            mc.getTextureManager().bindTexture(cover);
            Gui.drawScaledCustomSizeModalRect((int) rx, (int) cy, 0, 0, CoverArt.SIZE, CoverArt.SIZE,
                    (int) cs, (int) cs, CoverArt.SIZE, CoverArt.SIZE);
        } else {
            RenderingUtils.drawRoundedRect(rx, cy, rx + cs, cy + cs, 6, new Color(255, 255, 255, 14).getRGB());
        }

        // text
        float tx = rx + cs + 8, maxW = rx + rw - tx;
        if (t == null) {
            big.drawStringWithShadow("Nothing playing", tx, cy + 2, grey);
            small.drawStringWithShadow(trackButtons.isEmpty() ? "No music found" : "Pick a track on the left", tx, cy + 20, dim);
        } else {
            String artist = t.getArtist().isEmpty() ? "Unknown Artist" : t.getArtist();
            big.drawStringWithShadow(trim(t.getTitle(), maxW, big::getWidth), tx, cy + 2, white);
            mid.drawStringWithShadow(trim(artist, maxW, mid::getWidth), tx, cy + 18, grey);
            if (!t.getAlbum().isEmpty())
                small.drawStringWithShadow(trim(t.getAlbum(), maxW, small::getWidth), tx, cy + 32, dim);
        }

        String err = player.getLastError();
        if (!err.isEmpty())
            small.drawStringWithShadow(trim(err, rw, small::getWidth), rx, y + 104, new Color(255, 110, 110).getRGB());

        // progress
        long dur = player.getDurationMs();
        long real = player.getPositionMs();
        boolean isPlaying = player.getState() == CavernPlayer.State.PLAYING;

        if (t != lastTrack) {                       // new track: no sweep
            smoothPosMs = real;
            lastTrack = t;
        } else if (!draggingSeek) {
            if (isPlaying) smoothPosMs += dt * 1000f;           // advance every frame
            // im lazy
            float errr = real - smoothPosMs;
            if (Math.abs(errr) > 500) smoothPosMs = real;        // seek or jump: snap
            else smoothPosMs += errr * Math.min(1f, dt * 8f);    // gently absorb drift
        }
        if (dur > 0) smoothPosMs = Math.min(smoothPosMs, dur);

        long pos = draggingSeek ? (long) (seekFrac * dur) : (long) smoothPosMs;
        float frac = dur > 0 ? Math.min(1f, (float) pos / dur) : 0f;

        // while dragging, glide toward the cursor; otherwise follow playback exactly
        float seekShown;
        if (draggingSeek) seekShown = seekTw.go(frac, 0.06f, dt);
        else { seekTw.snap(frac); seekShown = frac; }

        boolean seekOver = t != null && dur > 0 && over(mouseX, mouseY, rx, barY - 4, rw, 11);
        float seekHover = seekHoverTw.go((draggingSeek || seekOver) ? 1f : 0f, 0.12f, dt);

        float sh = 3f + 2f * seekHover;
        float sTop = barY + 1.5f - sh / 2f;
        RenderingUtils.drawRectangle(rx, sTop, rx + rw * seekShown, sTop + sh, accent);
        LiquidGlass.drawPanel(rx - 1, sTop - 1, rw + 2, sh + 2.5f, 0, 0, 0, 0, 0, 0, 0);
        if (seekHover > 0.02f) {
            float kr = 1.5f + 2.5f * seekHover, sx = rx + rw * seekShown;
            LiquidGlass.drawPanel(sx - kr, barY + 2 - kr, kr * 2, kr * 2, kr, 4, 0, 1f, 0.25f, 0, 0.9f);
        }
        small.drawStringWithShadow(time(pos), rx, barY + 7, grey);
        String total = dur > 0 ? time(dur) : "--:--";
        small.drawStringWithShadow(total, rx + rw - (float) small.getWidth(total), barY + 7, grey);


        // transport buttons
        boolean playing = player.getState() == CavernPlayer.State.PLAYING;
        float cyB = btnY + BTN_H / 2;

        drawButton(prevX, btnY, BTN_SIDE_W, BTN_H, over(mouseX, mouseY, prevX, btnY, BTN_SIDE_W, BTN_H), 0);
        float cx = prevX + BTN_SIDE_W / 2;
        RenderingUtils.drawRectangle(cx - 5, cyB - 4, cx - 3, cyB + 4, white);
        triangle(cx + 4, cyB - 4, cx + 4, cyB + 4, cx - 3, cyB, white);

        boolean playHov = over(mouseX, mouseY, playX, btnY, BTN_PLAY_W, BTN_H);
        drawButton(playX, btnY, BTN_PLAY_W, BTN_H, playHov, new Color(ac.getRed(), ac.getGreen(), ac.getBlue(), playHov ? 130 : 95).getRGB());
        cx = playX + BTN_PLAY_W / 2;
        if (playing) {
            RenderingUtils.drawRectangle(cx - 4, cyB - 5, cx - 1, cyB + 5, white);
            RenderingUtils.drawRectangle(cx + 1, cyB - 5, cx + 4, cyB + 5, white);
        } else {
            triangle(cx - 3, cyB - 5, cx - 3, cyB + 5, cx + 5, cyB, white);
        }

        drawButton(nextX, btnY, BTN_SIDE_W, BTN_H, over(mouseX, mouseY, nextX, btnY, BTN_SIDE_W, BTN_H), 0);
        cx = nextX + BTN_SIDE_W / 2;
        triangle(cx - 4, cyB - 4, cx - 4, cyB + 4, cx + 3, cyB, white);
        RenderingUtils.drawRectangle(cx + 3, cyB - 4, cx + 5, cyB + 4, white);

        boolean shufOn = player.isShuffle();
        boolean shufHov = over(mouseX, mouseY, rx, btnY, BTN_SHUF_W, BTN_H);
        drawButton(rx + 28, btnY, BTN_SHUF_W / 2, BTN_H, shufHov,
                shufOn ? new Color(ac.getRed(), ac.getGreen(), ac.getBlue(), shufHov ? 130 : 95).getRGB() : 0);
        jumbo.drawCenteredStringWithShadow("⤮", rx + BTN_SHUF_W / 2f + 11, btnY - 1, white);

        // volume
        float volShown = volTw.go(player.getVolume(), draggingVolume ? 0.05f : 0.15f, dt);
        boolean volOver = over(mouseX, mouseY, volX - 4, volY - 4, volW + 8, 14);
        float volHover = volHoverTw.go((draggingVolume || volOver) ? 1f : 0f, 0.12f, dt);

        small.drawStringWithShadow("Vol", rx, volY - 1, grey);
        float vh = 3f + 2f * volHover, vTop = volY + 3.5f - vh / 2f;
        RenderingUtils.drawRectangle(volX, vTop + 0.5f, volX + volW * volShown, vTop + vh, accent);
        LiquidGlass.drawPanel(volX - 1, vTop - 1, volW + 2, vh + 2.5f, 0, 0, 0, 0, 0, 0, 0);
        float kx = volX + volW * volShown, kr = 2f + 1.5f * volHover;
        LiquidGlass.drawPanel(kx - kr, volY + 4 - kr, kr * 2, kr * 2, kr, 4, 0, 1f, 0.25f, 0, 0.9f);
    }

    private void drawButton(float bx, float by, float bw, float bh, boolean hovered, int tint) {
        int base = tint != 0 ? tint : new Color(255, 255, 255, hovered ? 100 : 69).getRGB();
        RenderingUtils.drawRoundedRect(bx, by, bx + bw, by + bh, 5, base);
        LiquidGlass.drawPanel(bx, by, bw, bh, 5, 0, 2, 0.6f, 0, 0 ,0);
    }

    private static void triangle(float x1, float y1, float x2, float y2, float x3, float y3, int color) {
        float a = (color >> 24 & 255) / 255f, r = (color >> 16 & 255) / 255f,
                g = (color >> 8 & 255) / 255f, b = (color & 255) / 255f;
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(r, g, b, a);
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x2, y2);
        GL11.glVertex2f(x3, y3);
        GL11.glEnd();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    // ------------------------------------------------------------------- input

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton != 0) return;

        if (player.getCurrent() != null && player.getDurationMs() > 0
                && over(mouseX, mouseY, rx, barY - 4, rw, 11)) {
            draggingSeek = true;
            seekFrac = Math.max(0f, Math.min(1f, (mouseX - rx) / rw));
        } else if (over(mouseX, mouseY, prevX, btnY, BTN_SIDE_W, BTN_H)) player.previous();

        if (inList(mouseX, mouseY)) {
            for (TrackButton b : trackButtons)
                if (b.isMouseOver(mouseX, mouseY)) {
                    b.onPressed(mouseX, mouseY, mouseButton);
                    return;
                }
            return;
        }

        if (over(mouseX, mouseY, prevX, btnY, BTN_SIDE_W, BTN_H)) player.previous();
        else if (over(mouseX, mouseY, playX, btnY, BTN_PLAY_W, BTN_H)) player.togglePause();
        else if (over(mouseX, mouseY, nextX, btnY, BTN_SIDE_W, BTN_H)) player.next();
        else if (over(mouseX, mouseY, rx, btnY, BTN_SHUF_W, BTN_H)) player.toggleShuffle();
        else if (over(mouseX, mouseY, volX - 4, volY - 4, volW + 8, 14)) {
            draggingVolume = true;
            setVolumeFromMouse(mouseX);
        } else if (over(mouseX, mouseY, rescanX, rescanY, 48, 14)) player.getLibrary().scan();
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (draggingVolume) setVolumeFromMouse(mouseX);
        if (draggingSeek) seekFrac = Math.max(0f, Math.min(1f, (mouseX - rx) / rw));
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (draggingSeek) {
            draggingSeek = false;
            player.seek((long) (seekFrac * player.getDurationMs()));
        }
        draggingVolume = false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_SPACE) player.togglePause();
        else super.keyTyped(typedChar, keyCode); // ESC closes
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0 && maxScroll > 0 && lastMouseX < x + 136) {
            scrollTarget = Math.max(0, Math.min(scrollTarget + (wheel > 0 ? -scrollHeight : scrollHeight), maxScroll));
            scrollAnim.start(AnimationType.SMOOTHSTEP, scroll, scrollTarget, 0.1f);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // ----------------------------------------------------------------- helpers

    private void setVolumeFromMouse(int mouseX) {
        player.setVolume((mouseX - volX) / volW); // setVolume clamps
    }

    public boolean inList(int mx, int my) {
        return mx >= x && mx < x + 136 && my >= y + 27 && my <= y + 27 + visibleItems * scrollHeight;
    }

    private static boolean over(int mx, int my, float bx, float by, float bw, float bh) {
        return mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
    }

    public static String time(long ms) {
        long s = ms / 1000;
        return String.format("%d:%02d", s / 60, s % 60);
    }

    public static String trim(String s, float maxW, ToDoubleFunction<String> width) {
        if (width.applyAsDouble(s) <= maxW) return s;
        while (s.length() > 1 && width.applyAsDouble(s + "...") > maxW) s = s.substring(0, s.length() - 1);
        return s + "...";
    }

    private static final class Tween {
        private final Animation anim = new Animation();
        float value, target;
        private boolean init;

        /** Jump instantly, no glide. */
        void snap(float v) {
            value = target = v;
            init = true;
            anim.start(AnimationType.SMOOTHSTEP, v, v, 0.001f); // cancels any glide in flight
        }

        /** Glide toward `to`. Call once per frame; returns the value to draw. */
        float go(float to, float dur, float dt) {
            if (!init) snap(to);
            else if (to != target) {
                target = to;
                anim.start(AnimationType.EASE_OUT, value, to, dur);
            }
            if (anim.isRunning()) value = anim.update(dt);
            return value;
        }
    }

    private Tween volTw = new Tween(), seekTw = new Tween(), volHoverTw = new Tween(), seekHoverTw = new Tween();
    private Track lastTrack;
}