package kebriel.ctf.entity.entities;

import kebriel.ctf.entity.components.EntityWrapper;
import kebriel.ctf.entity.components.EquipmentSlots;
import kebriel.ctf.util.ReflectionUtil;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

public class Hologram extends EntityWrapper<EntityArmorStand> {

    private String display;
    private final EntityArmorStand stand;

    public Hologram(Location loc, String display) {
        super(EntityArmorStand.class, loc);
        this.display = display;
        stand = super.getEntity();
        stand.setCustomNameVisible(true);
        stand.setCustomName(display);
        stand.setGravity(false);
        stand.setInvisible(true);
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
        stand.setCustomName(display);
        if(super.isRendered() || super.isSpawned())
            super.refreshEntity();
    }

    public void setBlockWearing(ItemStack block) {
        if(!CraftItemStack.asBukkitCopy(block).getType().isBlock())
            throw new IllegalArgumentException("Only blocks can be set using setBlockWearing()");
        setEquipment(EquipmentSlots.HELMET, block);
    }

    public void setHeldItem(ItemStack item) {
        setEquipment(EquipmentSlots.HAND, item);
    }
}
