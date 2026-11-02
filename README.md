# Cavern
#### Cavern, previously Echo, is a pvp client for Minecraft 1.8.8 that puts the user experience first with an intuitive easy to use ui with smooth animations and clean visuals.

### Controls

| Action             | Keybinds  |
|--------------------|-----------|
| Open CavernGUI     | Press `U` |
| Open Cavern Player | Press `M` |

## Cavern Gui
<img width="655" height="462" alt="image" src="https://github.com/user-attachments/assets/be3fc24b-8788-4b25-a7f4-bfe1530cd188" />

## Cavern Player
<img width="818" height="450" alt="image" src="https://github.com/user-attachments/assets/2e801656-5b1e-4147-81ae-7e3cdd94efc7" />

#### WITH SCRUBBING

<img width="832" height="435" alt="image" src="https://github.com/user-attachments/assets/696a4436-e505-48b3-b56d-4c7e8319dc1b" />

### Features

- Plays your local library: **MP3, FLAC and WAV**
- Reads title, artist, album and embedded **cover art** from file tags
- Play / pause, previous / next, and **shuffle**
- **Scrubbing**: click or drag the progress bar to jump anywhere in a track
- Volume slider (saved between sessions, along with the shuffle setting)
- Smooth animations on the progress bar, volume bar and hover states
- Auto-advances to the next track and skips files that fail to decode

### Getting started

1. Put your music in `Cavern/Music` (subfolders are scanned up to 4 levels deep).
2. Open the Cavern Player from the client.
3. Press **Rescan** whenever you add or remove files.

### Controls

| Action          | How                                                                          |
|-----------------|------------------------------------------------------------------------------|
| Play a track    | Click it in the list                                                         |
| Pause / resume  | Pause button or `Space`                                                      |
| Next / previous | Skip buttons. Previous restarts the track if more than 3 seconds have played |
| Shuffle         | Shuffle button. Previous walks back through what you actually heard          |
| Seek            | Click or drag the progress bar (the jump happens on release)                 |
| Volume          | Click or drag the volume bar                                                 |
| Refresh library | **Rescan**                                                                   |
| Close           | `Esc`                                                                        |

### Supported formats

| Format | Notes                                                                                                                                              |
|--------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| MP3    | Decoded with JLayer / mp3spi                                                                                                                       |
| FLAC   | Decoded with jFLAC, including 24-bit files                                                                                                         |
| WAV    | Parsed by Cavern's own reader: 8/16/24/32-bit PCM, 32/64-bit float, and `WAVE_FORMAT_EXTENSIBLE` headers (as exported by FL Studio and other DAWs) |

Notes:

- Java's built-in WAV reader can't open extensible-header files, and the MP3 decoder will misread them as noise, which is why WAVs use a dedicated reader.
- Very high sample rates (88.2 kHz and above) may not play if your audio device doesn't support them.
- OGG files are picked up by the scanner but are not officially supported yet.

### Dependencies

To build Cavern with the music player, add these jars to your `libraries` folder and the project classpath:

| Library                      | Purpose                                  |
|------------------------------|------------------------------------------|
| `jaudiotagger-3.0.1.jar`     | Reading tags, duration and cover art     |
| `mp3spi-1.9.5.4.jar`         | MP3 support for Java Sound               |
| `jlayer-1.0.1.4.jar`         | MP3 decoder used by mp3spi               |
| `tritonus-share-0.3.7.4.jar` | Shared Java Sound helpers used by mp3spi |
| `jflac-codec-1.5.2.jar`      | FLAC decoding                            |

Without the codec jars, the player shows a "No decoder for .xxx (codec jar missing?)" message for that file type.

### Troubleshooting

| Message                       | Likely cause                                                                                |
|-------------------------------|---------------------------------------------------------------------------------------------|
| `No music found`              | The `Cavern/Music` folder is empty. Add files and press **Rescan**                          |
| `No decoder for .mp3 / .flac` | A codec jar is missing from the classpath                                                   |
| `Decoder produced no audio`   | The file is empty, corrupt, or uses an unsupported encoding                                 |
| Track skips instantly         | Same as above. Check `latest.log` for `[Cavern] [ERROR] Can't play ...` and its stack trace |
