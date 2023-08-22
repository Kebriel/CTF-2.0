package kebriel.ctf.display.gui.menu;

import kebriel.ctf.display.gui.component.inventory.GameGUI;
import kebriel.ctf.display.gui.component.inventory.Slot;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.PlayerState;
import kebriel.ctf.player.item.CTFItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Overview extends GameGUI {

    public Overview(CTFPlayer player) {
        super(player);
    }

    @Override
    public String getName() {
        return "Overview";
    }

    @Override
    public int getSize() {
        return 54;
    }

    @Override
    public Object[][] draw() {
        return new Object[][]{
                {"-", "-", "-", "-", "-", "-", "-", "-", "-"},

                {"|", "A1", "*", "*", "H", "*", "*", "*", "|"},

                {"|", "A2", "*", "S", "C", "B", "*", "P1", "|"},

                {"|", "A3", "*", "I", "L", "*", "*", "P2", "|"},

                {"|", "A4", "*", "*", "B", "*", "*", "*", "|"},

                {"-", "-", "-", "-", "-", "-", "-", "-", "-"},
        };
    }

    @Override
    public void setPlaceholders() {
        CTFPlayer player = getPlayer();
        ItemStack borderGlass = CTFItem.newItem(Material.STAINED_GLASS_PANE).setData(player.getTeam().getTeamColor().getColorID()).build();
        setPlaceholder("-", borderGlass);
        setPlaceholder("|", borderGlass);
        setPlaceholder("*", CTFItem.newItem(Material.STAINED_GLASS_PANE).build());
        setPlaceholder("A1", Slot.ABILITY_1.getButton(this).setActiveSlots(10));
        setPlaceholder("A2", Slot.ABILITY_2.getButton(this).setActiveSlots(19));
        setPlaceholder("A3", Slot.ABILITY_3.getButton(this).setActiveSlots(28));
        setPlaceholder("A4", Slot.ABILITY_4.getButton(this).setActiveSlots(37));
        setPlaceholder("I", Slot.EXTRA_ITEM.getButton(this).setActiveSlots(30));
        setPlaceholder("P1", Slot.PERK_1.getButton(this).setActiveSlots(25));
        setPlaceholder("P2", Slot.PERK_2.getButton(this).setActiveSlots(25));
        setPlaceholder("S", PlayerState.getFor(player).SWORD.getItem());
        setPlaceholder("B", PlayerState.getFor(player).BOW.getItem());
        setPlaceholder("H", PlayerState.getFor(player).HELMET.getItem());
        setPlaceholder("C", PlayerState.getFor(player).CHESTPLATE.getItem());
        setPlaceholder("L", PlayerState.getFor(player).LEGGINGS.getItem());
        setPlaceholder("B", PlayerState.getFor(player).BOOTS.getItem());
    }
}
