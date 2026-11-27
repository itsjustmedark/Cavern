package net.minecraft.client.gui;

import lombok.Getter;
import net.cavern.utils.ui.Animation;
import net.minecraft.util.IChatComponent;

public class ChatLine
{
    public final Animation anim = new Animation();
    private final IChatComponent lineString;
    /** GUI Update Counter value this Line was created at */
    private final int updateCounterCreated;
    public float animState = 0f;

    /**
     * int value to refer to existing Chat Lines, can be 0 which means unreferrable
     */
    @Getter
    private final int chatLineID;

    public ChatLine(int p_i45000_1_, IChatComponent p_i45000_2_, int p_i45000_3_)
    {
        this.lineString = p_i45000_2_;
        this.updateCounterCreated = p_i45000_1_;
        this.chatLineID = p_i45000_3_;
    }

    public IChatComponent getChatComponent() { return this.lineString; }
    public int getUpdatedCounter() { return this.updateCounterCreated; }
}
