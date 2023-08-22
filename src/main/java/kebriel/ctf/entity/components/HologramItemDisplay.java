package kebriel.ctf.entity.components;

import kebriel.ctf.entity.entities.Hologram;
import net.minecraft.server.v1_8_R3.ItemStack;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

public interface HologramItemDisplay<T extends Hologram> {

    private Hologram getHologram() {
        return (T) this;
    }

    default void setBlockWearing(ItemStack block) {
        org.bukkit.inventory.ItemStack stack = CraftItemStack.asBukkitCopy(block);
        if(!stack.getType().isBlock()) throw new IllegalArgumentException("Only blocks can be set using setBlockWearing()");
        getHologram().setEquipment(EquipmentSlots.HELMET, block);
    }

    default void setHeldItem(ItemStack item) {
        getHologram().setEquipment(EquipmentSlots.HAND, item);
    }
}
