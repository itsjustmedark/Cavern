package net.cavern;

import lombok.Getter;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.arikia.dev.drpc.DiscordUser;
import net.arikia.dev.drpc.callbacks.ReadyCallback;
import net.cavern.gui.toggles.CavernGui;
import net.cavern.manager.FileManager;
import net.cavern.utils.ui.liquidglass.LiquidGlass;
import net.minecraft.client.Minecraft;
import net.cavern.gui.quickzoom.QuickZoom;
import net.cavern.manager.AddonManager;
import org.lwjgl.opengl.Display;

import java.awt.*;
import java.nio.file.Path;

import static java.lang.Thread.sleep;
import static net.cavern.gui.quickzoom.QuickZoom.mc;

@Getter
public enum Cavern {
    INSTANCE;
    public final int clientColor = new Color(90, 70, 124, 255).getRGB();
    public final Color clientColorNonRGB = new Color(90, 70, 124, 255);
    private AddonManager addonManager;
    // making an exception for this, thats fuckin ugly man. i dont know how i went 2 fuckin years without even having these here man what the fuck
    public static final String NAME = "Cavern", CAVERN_VERSION = "1.3.0", CAVERN_FORMATTED = String.format("%s %s", Cavern.NAME, Cavern.CAVERN_VERSION), CAVERN_DEV = "justmedark";
    private boolean running = true;
    private FileManager fileManager;
    public CavernGui cavernGui;
    private long started;

    public void initialize() {
        addonManager = new AddonManager();
        QuickZoom.init();
//        startRPC(); disabled because it kills cpu performance
        //Fast Render you can personally fuck off and never enable again, 1 hour of pain over this
        Minecraft.getMinecraft().gameSettings.ofFastRender = false;
        Minecraft.getMinecraft().gameSettings.guiScale = 2;
        Display.setTitle(String.format("%s %s", Cavern.NAME, Cavern.CAVERN_VERSION));
        initFileSystem();
        // Initialize now instead of the first time its drawn
        LiquidGlass.init();
    }

    private void initFileSystem() {
        Path path = mc.mcDataDir.toPath().resolve("Cavern").resolve("addon_states.json");
        fileManager = new FileManager(path);
        fileManager.register("addons", addonManager);
        fileManager.load();

        Runtime.getRuntime().addShutdownHook(new Thread(fileManager::save));
    }

    private void startRPC(){
        started = System.currentTimeMillis();
        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().setReadyEventHandler(discordUser -> updateRPC("Loading " + CAVERN_FORMATTED + "...", "")).build();

//        DiscordRPC.discordInitialize("1502242079529767043", handlers, true);

//        DONT YOU DARE EVEN TRY
//        new Thread("DiscordRPC"){
//            @Override
//            public void run(){
//                while (running) {
//                    DiscordRPC.discordRunCallbacks();
//                    try { sleep(1000); } catch (InterruptedException e) { break; }
//                }
//            }
//        }.start();
    }

    public void stopRPC(){
        running = false;
        DiscordRPC.discordShutdown();
    }

    public void updateRPC(String details, String title){
        DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder(title);
        builder.setBigImage("logo", "");
        builder.setDetails(details);
        builder.setStartTimestamps(started);
        DiscordRPC.discordUpdatePresence(builder.build());
    }
}
