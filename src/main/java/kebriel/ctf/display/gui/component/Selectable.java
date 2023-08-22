package kebriel.ctf.display.gui.component;

import kebriel.ctf.ability.components.PassiveAbility;
import kebriel.ctf.game.Game;
import kebriel.ctf.player.Stat;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.item.CTFItem;
import kebriel.ctf.internal.player.GameSound;
import kebriel.ctf.internal.player.text.Text;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public interface Selectable {

    EMPTY EMPTY = new EMPTY();

    Material getIcon();
    String getID();
    Text getDescription();
    String getName();

    default Text getSubtext() {
        return null;
    }

    default boolean mustBeUnlocked() {
        return this instanceof Unlockable;
    }

    default String getPolishedName() {
        return Text.get().gold(getName()).toString();
    }

    default String[] getFullDescription(CTFPlayer player) {
        Text description = getDescription();
        description.definePrimary(ChatColor.YELLOW).defineSecondary(ChatColor.AQUA);
        description.emptyLine();
        description.newLine().add(this instanceof Unlockable a && !a.isUnlocked(player)  ? getSubtext() : player.getIsSelected(getID()) ? "Selected" : "Click to select");
        return description.build();
    }

    default ItemStack getMenuItem(CTFPlayer player) {
        return getMenuItemRaw(player).build();
    }

    default CTFItem getMenuItemRaw(CTFPlayer player) {
        CTFItem item = CTFItem.newItem(getIcon()).setName(getPolishedName());
        String[] lore = getFullDescription(player);
        for(int i = 0; i < lore.length; i++)
            item.setLore(i, lore[i]);
        if(player.getIsSelected(getID()))
            item.addEnchantment(Enchantment.ARROW_DAMAGE, 0).addFlag(ItemFlag.HIDE_ENCHANTS);
        return item;
    }

    default void select(CTFPlayer player, Stat selectSlot) {
        player.setSelected(getID(), selectSlot);
        player.refreshMenu();
        player.play(GameSound.MENU_YES);

        if(Game.get().isPlaying())
            if(this instanceof PassiveAbility passive)
                passive.start(player);
    }

    class EMPTY implements Selectable {

        @Override
        public Material getIcon() {
            return Material.AIR;
        }

        @Override
        public String getID() {
            return "empty";
        }

        @Override
        public Text getDescription() {
            return Text.get();
        }

        @Override
        public String getName() {
            return "Empty";
        }
    }
}
