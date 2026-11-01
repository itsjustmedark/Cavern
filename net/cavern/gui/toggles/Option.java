package net.cavern.gui.toggles;

import lombok.Getter;
import lombok.Setter;
import net.cavern.Cavern;
import net.cavern.utils.ui.Animation;

@Getter@Setter
public class Option {
    private final String id;
    public String display;
    public boolean val, hovered, optWasState = false;
    Animation optToggleAnim = new Animation();
    public float optState;


    public Option (String id, String display, boolean val){
        this.id = id;
        this.display = display;
        this.val = val;
    }

    // Old constructor still works, id is derived from the display text.
    // Renaming the display text will reset that option's saved value, so prefer the 3-arg one.
    public Option(String display, boolean val) {
        this(display.toLowerCase().replaceAll("[^a-z0-9]+", "_"), display, val);
    }

    public void setVal(boolean val) {
        if (this.val == val) return;
        this.val = val;
        Cavern.INSTANCE.getFileManager().markDirty();
    }

    public void toggle() { setVal(!val); }
}
