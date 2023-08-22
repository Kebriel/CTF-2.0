package kebriel.ctf.player;

import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.ability.components.Ability;

public class SelectedAbility {

    private Ability selected;
    private final Stat slot;

    public SelectedAbility(Stat slot) {
        this.slot = slot;
    }

    public void select(Ability selected) {
        this.selected = selected;
    }

    public Ability getSelected() {
        return selected;
    }

    public Stat getSlot() {
        return slot;
    }

    public AbilityType getType() {
        return selected.getType();
    }

    public boolean isEmpty() {
        return selected == null;
    }
}
