package kebriel.ctf.ability.components;

import kebriel.ctf.player.PlayerState;
import kebriel.ctf.player.item.InvSlot;
import kebriel.ctf.player.item.InventoryProfile.InventoryProfileType;
import kebriel.ctf.player.item.Item;

public interface ItemAbility extends Ability {

    default void setItem(PlayerState player) {
        player.getInventoryProfile(InventoryProfileType.GAME).setItemInSlot(getSlot(), getItem());
    }

    InvSlot getSlot();
    Item getItem();
    ItemAbility[] incompatibleWith();
}
