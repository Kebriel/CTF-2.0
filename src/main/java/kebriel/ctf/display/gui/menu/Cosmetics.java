package kebriel.ctf.display.gui.menu;

import kebriel.ctf.display.gui.component.button.GUIButtonPortal;
import kebriel.ctf.display.gui.component.button.GUIButtonRun;
import kebriel.ctf.display.gui.component.inventory.GameGUI;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.item.CTFItem;
import kebriel.ctf.internal.player.GameSound;
import kebriel.ctf.internal.player.text.Text;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Cosmetics extends GameGUI {

    public Cosmetics(CTFPlayer player) {
        super(player);
    }

    @Override
    public String getName() {
        return ChatColor.LIGHT_PURPLE + "Cosmetics";
    }

    @Override
    public int getSize() {
        return 54;
    }

    @Override
    public Object[][] draw() {
        return new Object[][]{
                {"-", "-", "-", "-", "-", "-", "-", "-", "-"},

                {"|", "AUR", "*", "*", "TRA", "*", "*", "EFF", "|"},

                {"|", "*", "*", "*", "*", "*", "*", "*", "|"},

                {"|", "MSG", "*", "*", "WIP", "*", "*", "WIP", "|"},

                {"|", "*", "*", "*", "*", "*", "*", "*", "|"},

                {"-", "-", "-", "-", "-", "-", "-", "-", "-"}
        };
    }

    @Override
    public void setPlaceholders() {
        ItemStack borderGlass = CTFItem.newItem(Material.STAINED_GLASS_PANE).setData((byte)2).build();
        setPlaceholder("-", borderGlass);
        setPlaceholder("|", borderGlass);
        setPlaceholder("*", CTFItem.newItem(Material.STAINED_GLASS_PANE).setData((byte)4).build());
        setPlaceholder("AUR", new GUIButtonPortal(
                CTFItem.newItem(Material.BLAZE_POWDER).setName(Text.get().gold("Particle Effects")).addLore(
                        Text.get().blue("Choose from an array of unique")
                                .newLine().blue("particle effects to display")
                                .newLine().blue("around your character")).build(),
                this,
                new Auras(getPlayer())
        ).setActiveSlots(10));
        setPlaceholder("TRA", new GUIButtonPortal(
                CTFItem.newItem(Material.ARROW).setName(Text.get().gold("Arrow Trails")).addLore(
                        Text.get().blue("Select a unique cosmetic effect that")
                                .newLine().blue("will follow your arrows as")
                                .newLine().blue("they fly")).build(),
                this,
                new ArrowTrails(getPlayer())
        ).setActiveSlots(13));
        setPlaceholder("EFF", new GUIButtonPortal(
                CTFItem.newItem(Material.SKULL_ITEM).setName(Text.get().gold("Kill Effects")).addLore(
                        Text.get().blue("Choose from a plethora of special")
                                .newLine().blue("effects that will display whenever")
                                .newLine().blue("you kill a player")).build(),
                this,
                new KillEffects(getPlayer())
        ).setActiveSlots(16));
        setPlaceholder("MSG", new GUIButtonPortal(
                CTFItem.newItem(Material.NAME_TAG).setName(Text.get().gold("Kill Messages")).addLore(
                        Text.get().blue("Choose a from a suite of themes")
                                .newLine().blue("that will play special kill")
                                .newLine().blue("messages whenever you get a kill")).build(),
                this,
                new KillMessages(getPlayer())
        ).setActiveSlots(28));
        setPlaceholder("WIP", new GUIButtonRun(
                CTFItem.newItem(Material.BARRIER).setName(Text.get().aqua("Coming soon...")).build(),
                this,
                player -> player.play(GameSound.MENU_NO)
        ).setActiveSlots(31, 34));
    }
}
