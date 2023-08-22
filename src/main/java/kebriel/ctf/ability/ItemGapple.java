package kebriel.ctf.ability;

import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.ability.components.ItemAbility;
import kebriel.ctf.display.gui.component.Purchaseable;
import kebriel.ctf.player.Stat;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.player.item.InvSlot;
import kebriel.ctf.player.item.Item;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class ItemGapple implements ItemAbility, Purchaseable {
	
	@Override
	public int getCost() {
		return 2000;
	}

	@Override
	public Ability getInstance() {
		return new ItemGapple();
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ITEM;
	}

	@Override
	public String getName() {
		return "Golden Apple";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_ITEM_GAPPLE;
	}

	@Override
	public String getID() {
		return "item_gapple";
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("Spawn with a golden apple each life");
	}

	@Override
	public Material getIcon() {
		return Material.GOLDEN_APPLE;
	}

	@Override
	public InvSlot getSlot() {
		return InvSlot.EXTRA_ITEM;
	}

	@Override
	public Item getItem() {
		return Item.GAPPLE;
	}

	@Override
	public ItemAbility[] incompatibleWith() {
		return new ItemAbility[0];
	}
}
