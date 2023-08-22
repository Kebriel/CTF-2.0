package kebriel.ctf.ability.components;

import kebriel.ctf.player.PlayerState;
import kebriel.ctf.player.item.CTFItem;
import kebriel.ctf.player.item.InventoryProfile.InventoryProfileType;
import kebriel.ctf.player.item.Item;

import java.util.function.Function;

public interface ItemModificationAbility extends ItemAbility {

    Function<CTFItem, CTFItem> getApply(PlayerState player);

    @Override
    default Item getItem() {
        return null;
    }

    @Override
    default void setItem(PlayerState player) {
        getApply(player).apply(player.getInventoryProfile(InventoryProfileType.GAME).getItemInSlot(getSlot()));
    }
}
