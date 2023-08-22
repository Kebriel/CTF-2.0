package kebriel.ctf.display.gui.component.inventory;

import kebriel.ctf.display.gui.component.Purchaseable;
import kebriel.ctf.display.gui.component.inventory.Slot.SlotType;
import kebriel.ctf.player.Stat;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.item.CTFItem;
import kebriel.ctf.internal.player.GameMessage;
import kebriel.ctf.internal.player.GameSound;
import kebriel.ctf.internal.player.text.Text;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public interface PlayerSlot extends Purchaseable {

    SlotType getType();
    Stat getSelectSlot();
    Stat getPrereqSlot();
    int getRequiredLevel();

    default boolean isEmpty(CTFPlayer player) {
        return player.getSelected(getSelectSlot()) == null;
    }

    @Override
    default Material getIcon() {
        return getType().getIcon();
    }

    @Override
    default String getName() {
        return getType().getName().toString();
    }

    @Override
    default Text getDescription() {
        return getType().getDescription();
    }

    @Override
    default Text getSubtext() {
        return getType().getDescription();
    }

    @Override
    default String getID() {
        return null;
    }

    @Override
    default boolean tryPurchase(CTFPlayer player) {
        if(getPrereqSlot() != null && !player.getIsUnlocked(getPrereqSlot())) {
            player.play(GameSound.MENU_NO);
            player.send(GameMessage.MENU_UNLOCK_PREREQ_SLOT);
            return false;
        }
        return Purchaseable.super.tryPurchase(player);
    }

    @Override
    default String[] getFullDescription(CTFPlayer player) {
        if(!mustBeUnlocked() || player.getIsUnlocked(getUnlockData())) {
            return getSubtext().build();
        }else{
            Text t = Text.get().darkAqua("Click to buy for ").gold(getCost() + "g");
            if(getRequiredLevel() > 0) t.emptyLine().newLine().yellow("You must be level ").reset("" + getRequiredLevel()).yellow(" to purchase this");
            return t.build();
        }
    }

    @Override
    default ItemStack getMenuItem(CTFPlayer player) {
        boolean unlocked = player.getIsUnlocked(getUnlockData());
        if(!isEmpty(player))
            return player.getSelected(getSelectSlot())
                    .getMenuItemRaw(player)
                    .addLore(" ")
                    .addLore(getSubtext().build())
                    .build();
        return CTFItem.newItem(unlocked ? getIcon() : Material.BEDROCK)
                .setName(getPolishedName() + (unlocked ? "" : Text.get().boldColor(ChatColor.RED, " LOCKED")))
                .addLore(getFullDescription(player))
                .build();
    }

    @Override
    default String getPolishedName() {
        return getName();
    }
}
