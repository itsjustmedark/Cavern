package net.cavern.player.core;

import javax.sound.sampled.*;
import java.io.*;

/** Reads PCM / float / extensible WAVs (8-64 bit) and outputs 16-bit signed LE PCM. */
public final class WavReader {
    private WavReader() {}

    public static AudioInputStream open(File file) throws IOException, UnsupportedAudioFileException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file), 1 << 16));
        try {
            byte[] id = new byte[4];
            in.readFully(id);
            String magic = new String(id, "US-ASCII");
            if (!magic.equals("RIFF") && !magic.equals("RF64"))
                throw new UnsupportedAudioFileException("Not a RIFF/WAV file");
            in.readFully(new byte[4]); // file size
            in.readFully(id);
            if (!new String(id, "US-ASCII").equals("WAVE"))
                throw new UnsupportedAudioFileException("Not a WAVE file");

            int tag = 0, ch = 0, bits = 0, rate = 0;
            boolean haveFmt = false;
            long dataLen = -1;

            while (true) {
                in.readFully(id);
                String chunk = new String(id, "US-ASCII");
                long size = le32(in) & 0xFFFFFFFFL;

                if (chunk.equals("fmt ")) {
                    tag = le16(in);
                    ch = le16(in);
                    rate = le32(in);
                    le32(in); le16(in); // byte rate, block align
                    bits = le16(in);
                    long used = 16;
                    if (tag == 0xFFFE && size >= 40) { // extensible: real format is in the subformat GUID
                        le16(in); le16(in); le32(in); // cbSize, valid bits, channel mask
                        tag = le16(in);               // first 2 bytes of the subformat GUID
                        used = 16 + 2 + 2 + 4 + 2;
                    }
                    skipFully(in, size - used + (size & 1));
                    haveFmt = true;
                } else if (chunk.equals("data")) {
                    if (!haveFmt) throw new UnsupportedAudioFileException("WAV has no fmt chunk");
                    dataLen = (size == 0xFFFFFFFFL || size == 0) ? -1 : size; // unknown -> read to EOF
                    break;
                } else {
                    skipFully(in, size + (size & 1));
                }
            }

            boolean isFloat = tag == 3;
            if (tag != 1 && tag != 3)
                throw new UnsupportedAudioFileException("Unsupported WAV codec (format tag " + tag + ")");
            if (ch < 1 || rate < 1) throw new UnsupportedAudioFileException("Bad WAV header");
            if (isFloat ? (bits != 32 && bits != 64) : (bits != 8 && bits != 16 && bits != 24 && bits != 32))
                throw new UnsupportedAudioFileException("Unsupported WAV bit depth: " + bits);

            AudioFormat out = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
            return new AudioInputStream(new To16(in, bits / 8, ch, isFloat, dataLen), out, AudioSystem.NOT_SPECIFIED);
        } catch (IOException | UnsupportedAudioFileException | RuntimeException e) {
            try { in.close(); } catch (IOException ignored) {}
            throw e;
        }
    }

    private static int le16(DataInputStream in) throws IOException {
        int a = in.readUnsignedByte(), b = in.readUnsignedByte();
        return a | (b << 8);
    }

    private static int le32(DataInputStream in) throws IOException {
        int a = in.readUnsignedByte(), b = in.readUnsignedByte(), c = in.readUnsignedByte(), d = in.readUnsignedByte();
        return a | (b << 8) | (c << 16) | (d << 24);
    }

    private static void skipFully(DataInputStream in, long n) throws IOException {
        while (n > 0) {
            long s = in.skip(n);
            if (s <= 0) { if (in.read() < 0) throw new EOFException(); s = 1; }
            n -= s;
        }
    }

    private static final class To16 extends InputStream {
        private final InputStream src;
        private final int bps, ch;
        private final boolean isFloat;
        private long remaining; // bytes left in the data chunk, -1 = until EOF
        private byte[] tmp = new byte[0];

        To16(InputStream src, int bps, int ch, boolean isFloat, long dataLen) {
            this.src = src; this.bps = bps; this.ch = ch; this.isFloat = isFloat; this.remaining = dataLen;
        }

        @Override public int read() throws IOException {
            byte[] one = new byte[2];
            return read(one, 0, 1) <= 0 ? -1 : one[0] & 0xFF;
        }

        @Override public int read(byte[] b, int off, int len) throws IOException {
            int frames = len / (ch * 2);
            if (frames == 0) return 0;
            int want = frames * ch * bps;
            if (remaining >= 0) want = (int) Math.min(want, remaining);
            if (want <= 0) return -1;
            if (tmp.length < want) tmp = new byte[want];

            int got = 0;
            while (got < want) {
                int r = src.read(tmp, got, want - got);
                if (r < 0) break;
                got += r;
            }
            int frameBytes = ch * bps;
            got -= got % frameBytes; // whole frames only
            if (got <= 0) return -1;
            if (remaining >= 0) remaining -= got;

            int samples = got / bps;
            for (int i = 0; i < samples; i++) {
                int p = i * bps;
                int s;
                if (isFloat) {
                    double f = bps == 4
                            ? Float.intBitsToFloat((tmp[p] & 0xFF) | (tmp[p + 1] & 0xFF) << 8 | (tmp[p + 2] & 0xFF) << 16 | (tmp[p + 3] & 0xFF) << 24)
                            : Double.longBitsToDouble(le64(tmp, p));
                    s = (int) Math.round(Math.max(-1.0, Math.min(1.0, f)) * 32767.0);
                } else if (bps == 1) {
                    s = ((tmp[p] & 0xFF) - 128) << 8;
                } else {
                    // 16/24/32-bit signed: keep the top 16 bits
                    s = (short) ((tmp[p + bps - 2] & 0xFF) | (tmp[p + bps - 1] << 8));
                }
                b[off + i * 2] = (byte) s;
                b[off + i * 2 + 1] = (byte) (s >> 8);
            }
            return samples * 2;
        }

        private static long le64(byte[] t, int p) {
            long v = 0;
            for (int i = 7; i >= 0; i--) v = (v << 8) | (t[p + i] & 0xFF);
            return v;
        }

        @Override public void close() throws IOException { src.close(); }
    }
}