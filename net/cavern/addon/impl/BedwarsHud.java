package net.cavern.addon.impl;

import net.cavern.addon.api.Addon;
import net.cavern.gui.toggles.Option;
import net.cavern.manager.FontManager;
import net.cavern.utils.CustomFontRenderer;
import net.cavern.utils.FontType;
import net.cavern.utils.RenderingUtils;
import net.cavern.widget.LabelWidget;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.function.IntSupplier;

public class BedwarsHud extends Addon {

    public static Option ironOpt = new Option("Iron", true), goldOpt = new Option("Gold", true),
            diamondOpt = new Option("Diamond", true), emeraldOpt = new Option("Emerald", true),
            ArrowOpt = new Option("Arrows", false);

    public int iron, gold, diamond, emerald, arrow;

    // Layout constants (were repeated in every ItemInfo).
    private static final int TEXT_X = 19, TEXT_Y = 209, IMG_X = 2, ROW_HEIGHT = 18, ICON_SIZE = 16;

    public BedwarsHud() {
        super("bedwars_hud", "Bedwars Hud");
        addLegacyId("bw");
        addOption(ironOpt, goldOpt, diamondOpt, emeraldOpt, ArrowOpt);
    }

    private static final class Entry {
        final Option option;
        final IntSupplier count;
        final ResourceLocation icon;
        final int imgY;
        int lastCount = -1;
        String text = "0";

        Entry(Option option, IntSupplier count, String texture, int imgY) {
            this.option = option;
            this.count = count;
            this.icon = new ResourceLocation(texture);
            this.imgY = imgY;
        }
    }

    private final Entry[] entries = {
            new Entry(ironOpt, () -> iron, "textures/items/iron_ingot.png", 205),
            new Entry(goldOpt, () -> gold, "textures/items/gold_ingot.png", 205),
            new Entry(diamondOpt, () -> diamond, "textures/items/diamond.png", 203),
            new Entry(emeraldOpt, () -> emerald, "textures/items/emerald.png", 205),
            new Entry(ArrowOpt, () -> arrow, "textures/items/arrow.png", 203)
    };

    @Override
    public void entityHook() {
        if (mc.thePlayer != null) {
            int iron = 0, gold = 0, diamond = 0, emerald = 0, arrow = 0;

            for (ItemStack stack : mc.thePlayer.inventory.mainInventory) {
                if (stack == null) continue;

                Item item = stack.getItem();
                int size = stack.stackSize;

                if (item == Items.iron_ingot) iron += size;
                else if (item == Items.gold_ingot) gold += size;
                else if (item == Items.diamond) diamond += size;
                else if (item == Items.emerald) emerald += size;
                else if (item == Items.arrow) arrow += size;
            }

            this.iron = iron;
            this.gold = gold;
            this.diamond = diamond;
            this.emerald = emerald;
            this.arrow = arrow;
        }
        super.entityHook();
    }

    @Override
    public void displayHook() {
        CustomFontRenderer font = FontManager.getFont(FontType.NORMAL, 14);
        int offset = 0;

        for (Entry e : entries) {
            if (!e.option.isVal()) continue;   // disabled rows shouldnt take up space

            // only rebuilding the string when an item count actually changes.
            int c = e.count.getAsInt();
            if (c != e.lastCount) {
                e.lastCount = c;
                e.text = String.valueOf(c);
            }

            // keep one label widget per Entry and update it instead of allocating.
            new LabelWidget(TEXT_X, TEXT_Y + offset, ROW_HEIGHT, font, e.text, -1).draw();
            RenderingUtils.drawImg(e.icon, IMG_X, e.imgY + offset, ICON_SIZE, ICON_SIZE);

            offset += ROW_HEIGHT;
        }

        super.displayHook();
    }
}