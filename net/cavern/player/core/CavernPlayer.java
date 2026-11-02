package net.cavern.player.core;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.cavern.Cavern;
import net.cavern.player.components.CoverArt;
import net.cavern.player.components.Track;
import net.cavern.utils.file.Configurable;

import javax.sound.sampled.*;
import java.util.List;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

import static java.lang.Thread.sleep;

public class CavernPlayer implements Configurable {

    public enum State { STOPPED, PLAYING, PAUSED }

    private static final int BUFFER_MS = 200;

    @Getter private volatile String lastError = "";

    @Getter private final MusicLibrary library;
    @Getter private final CoverArt cover = new CoverArt();
    @Getter private volatile State state = State.STOPPED;
    @Getter private volatile Track current;
    @Getter private volatile float volume = 0.6f;
    @Getter private volatile boolean shuffle;
    private final Deque<Track> history = new ArrayDeque<>();
    private final Random rng = new Random();
    private volatile long startMs;

    private final Object lock = new Object();
    private volatile int playId;
    private volatile SourceDataLine line;
    private volatile float sampleRate;
    private volatile int failStreak;

    public CavernPlayer(MusicLibrary library) { this.library = library; }

    @Override
    public void saveConfig(JsonObject out) {
        out.addProperty("volume", volume);
        out.addProperty("shuffle", shuffle);
    }

    @Override
    public void loadConfig(JsonObject in) {
        if (in.has("volume"))
            volume = Math.max(0f, Math.min(1f, in.get("volume").getAsFloat()));
        if (in.has("shuffle"))
            shuffle = in.get("shuffle").getAsBoolean();
    }

    public void play(Track track) { play(track, false, 0, true); }

    private void play(Track track, boolean auto) { play(track, auto, 0, true); }

    /** Restarts the current track at the given position. */
    public void seek(long ms) {
        Track t = current;
        if (t == null) return;
        long dur = getDurationMs();
        long target = Math.max(0, dur > 0 ? Math.min(ms, dur - 1000) : ms);
        play(t, false, target, false);
    }

    private void play(Track track, boolean auto, long offsetMs, boolean loadCover) {
        if (track == null) return;
        final int id;
        synchronized (lock) {
            cancelCurrent();
            id = playId;
            current = track;
            state = State.PLAYING;
            lastError = "";
            line = null;          // so the position doesn't read the old line
            startMs = offsetMs;
        }
        if (loadCover) cover.load(track.getFile());
        Thread t = new Thread(() -> run(track, id, auto, offsetMs), "Cavern-Music");
        t.setDaemon(true);
        t.start();
    }

    public void pause() {
        synchronized (lock) {
            if (state != State.PLAYING) return;
            state = State.PAUSED;
            SourceDataLine l = line;
            if (l != null) l.stop();
        }
    }

    public void resume() {
        synchronized (lock) {
            if (state != State.PAUSED) return;
            state = State.PLAYING;
            SourceDataLine l = line;
            if (l != null) l.start();
        }
    }

    public void togglePause() {
        if (state == State.PLAYING) pause();
        else if (state == State.PAUSED) resume();
        else next();
    }

    public void stop() {
        synchronized (lock) {
            cancelCurrent();
            state = State.STOPPED;
            current = null;
        }
        cover.clear();
    }

    public void next() { step(1, false); }

    public void previous() {
        if (current != null && getPositionMs() > 3000) { play(current); return; }
        step(-1, false);
    }

    public void setVolume(float v) {
        volume = Math.max(0f, Math.min(1f, v));
        Cavern.INSTANCE.getFileManager().markDirty();
    }

    public void toggleShuffle() {
        shuffle = !shuffle;
        synchronized (history) { history.clear(); }
        Cavern.INSTANCE.getFileManager().markDirty();
    }

    private void step(int dir, boolean auto) {
        List<Track> list = library.getTracks();
        if (list.isEmpty()) return;

        if (shuffle) {
            if (dir < 0) {
                // go back to what was actually played before
                Track prev;
                synchronized (history) { prev = history.pollFirst(); }
                if (prev != null && prev.getFile().exists()) play(prev, auto);
                else if (current != null) play(current, auto); // nothing earlier, restart
                return;
            }

            // remember where we were, then jump somewhere random
            Track cur = current;
            if (cur != null) synchronized (history) {
                history.addFirst(cur);
                if (history.size() > 100) history.removeLast();
            }
            int i = indexOfCurrent(list);
            int pick = rng.nextInt(list.size());
            if (list.size() > 1) while (pick == i) pick = rng.nextInt(list.size());
            play(list.get(pick), auto);
            return;
        }

        int i = indexOfCurrent(list);
        play(list.get(i < 0 ? 0 : Math.floorMod(i + dir, list.size())), auto);
    }

    private static String describe(Exception e, Track t) {
        if (e instanceof UnsupportedAudioFileException) {
            String n = t.getFile().getName();
            return "No decoder for ." + n.substring(n.lastIndexOf('.') + 1) + " (codec jar missing?)";
        }
        String m = e.getMessage();
        return e.getClass().getSimpleName() + (m == null ? "" : ": " + m);
    }

    // ------------------------------------------------------------------ status

    public long getPositionMs() {
        SourceDataLine l = line;
        float sr = sampleRate;
        if (state == State.STOPPED) return 0;
        if (l == null || sr <= 0) return startMs; // still opening: show where we're headed
        long ms = startMs + (long) (l.getLongFramePosition() * 1000.0 / sr);
        long dur = getDurationMs();
        return dur > 0 ? Math.min(ms, dur) : ms;
    }

    public long getDurationMs() {
        Track t = current;
        return t == null ? 0 : t.getDurationSeconds() * 1000L;
    }

    // ---------------------------------------------------------------- internals

    private void step(int dir) {
        List<Track> list = library.getTracks();
        if (list.isEmpty()) return;
        int i = indexOfCurrent(list);
        play(list.get(i < 0 ? 0 : Math.floorMod(i + dir, list.size())));
    }

    private int indexOfCurrent(List<Track> list) {
        Track c = current;
        if (c == null) return -1;
        for (int i = 0; i < list.size(); i++)
            if (list.get(i).getFile().equals(c.getFile())) return i;
        return -1;
    }

    /** Call while holding lock. */
    private void cancelCurrent() {
        playId++;
        SourceDataLine l = line;
        if (l != null) {
            try { l.stop(); } catch (RuntimeException ignored) {}
        }
    }

    private static boolean isPcm(AudioFormat f) {
        AudioFormat.Encoding e = f.getEncoding();
        return e == AudioFormat.Encoding.PCM_SIGNED
                || e == AudioFormat.Encoding.PCM_UNSIGNED
                || e == AudioFormat.Encoding.PCM_FLOAT;
    }

    private static AudioInputStream openStream(java.io.File f) throws Exception {
        if (f.getName().toLowerCase().endsWith(".wav")) return WavReader.open(f);
        return AudioSystem.getAudioInputStream(f);
    }

    /** Whatever the file is, hand back 16-bit signed little-endian PCM the line can play. */
    private static AudioInputStream toPcm16(AudioInputStream raw) {
        AudioInputStream s = raw;
        AudioFormat f = s.getFormat();
        System.out.println("[Cavern] source format: " + f);

        // Stage 1: compressed -> PCM (MP3/OGG usually do 16-bit in one hop, FLAC picks its own depth)
        if (!isPcm(f)) {
            int ch = f.getChannels();
            float rate = f.getSampleRate();
            AudioFormat direct = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
            s = AudioSystem.isConversionSupported(direct, f)
                    ? AudioSystem.getAudioInputStream(direct, raw)
                    : AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, raw);
            f = s.getFormat();
        }

        // Stage 2: already 16-bit signed LE at a rate the line accepts? done.
        int ch = f.getChannels();
        float rate = f.getSampleRate();
        AudioFormat target = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
        if (!AudioSystem.isLineSupported(new DataLine.Info(SourceDataLine.class, target)))
            target = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100f, 16, ch, ch * 2, 44100f, false);

        if (f.matches(target)) return s;
        return AudioSystem.getAudioInputStream(target, s);
    }

    private void run(Track track, int id, boolean auto, long offsetMs) {
        SourceDataLine out = null;
        boolean failed = false;
        try (AudioInputStream raw = openStream(track.getFile());
             AudioInputStream in = toPcm16(raw)) {

            AudioFormat pcm = in.getFormat();
            int frameSize = pcm.getFrameSize();

            if (offsetMs > 0) {
                long toSkip = (long) (offsetMs * pcm.getFrameRate() / 1000.0) * frameSize;
                byte[] junk = new byte[frameSize * 4096];
                while (toSkip > 0 && id == playId) {
                    int r = in.read(junk, 0, (int) Math.min(junk.length, toSkip));
                    if (r < 0) break;
                    toSkip -= r;
                }
                if (id != playId) return;
            }

            out = AudioSystem.getSourceDataLine(pcm);
            out.open(pcm, (int) (pcm.getFrameRate() * BUFFER_MS / 1000) * frameSize);

            synchronized (lock) {
                // skipped while we were opening
                if (id != playId) return;
                line = out;
                sampleRate = pcm.getSampleRate();
            }
            failStreak = 0;
            lastError = "";

            byte[] buf = new byte[frameSize * 2048];
            long writtenFrames = 0;
            int n;
            while (id == playId && (n = in.read(buf, 0, buf.length)) != -1) {
                applyVolume(buf, n, volume);
                int off = 0;
                while (off < n && id == playId) {
                    if (state == State.PAUSED) {
                        out.stop();
                        while (state == State.PAUSED && id == playId) Thread.sleep(30);
                        continue;
                    }
                    if (!out.isRunning()) out.start();
                    off += out.write(buf, off, n - off);
                }
                writtenFrames += n / frameSize;
            }

            if (id == playId && writtenFrames == 0)
                throw new java.io.IOException("Decoder produced no audio");

            // let the buffered audio finish playing
            while (id == playId && out.getLongFramePosition() < writtenFrames) {
                if (state == State.PLAYING && !out.isRunning()) out.start();
                sleep(25);
            }
        } catch (Exception e) {
            failed = true;
            if (id == playId) lastError = describe(e, track);
            System.err.println("[Cavern] [ERROR] Can't play " + track.getFile().getName() + ":");
            e.printStackTrace();
        } finally {
            if (out != null) {
                try { out.stop(); out.flush(); out.close(); } catch (Exception ignored) {}
            }
            synchronized (lock) { if (line == out) line = null; }
        }

        if (id != playId) return;

        if (failed) {
            if (!auto) { // you picked this track: stay on it and show the error
                synchronized (lock) { if (id == playId) state = State.STOPPED; }
                return;
            }
            if (++failStreak >= library.getTracks().size()) { stop(); return; } // nothing playable
        }
        step(1, true);
    }

    private static void applyVolume(byte[] b, int len, float vol) {
        float gain = vol * vol;
        if (gain >= 0.999f) return;
        for (int i = 0; i + 1 < len; i += 2) {
            short s = (short) ((b[i] & 0xFF) | (b[i + 1] << 8));
            s = (short) (s * gain);
            b[i] = (byte) s;
            b[i + 1] = (byte) (s >> 8);
        }
    }
}