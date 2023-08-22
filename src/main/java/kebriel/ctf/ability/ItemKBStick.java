package kebriel.ctf.ability;

import kebriel.ctf.Constants;
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
import org.bukkit.enchantments.Enchantment;

public class ItemKBStick implements ItemAbility, Purchaseable {
	
	@Override
	public int getCost() {
		return 3000;
	}

	@Override
	public Ability getInstance() {
		return new ItemKBStick();
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ITEM;
	}

	@Override
	public String getName() {
		return "Knockback Stick";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_ITEM_STICK;
	}

	@Override
	public String getID() {
		return "item_stick";
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("Spawn with a stick that's enchanted")
				.newLine().primary("with ").secondary("Knockback " + Constants.ITEM_KB_STICK_STRENGTH);
	}

	@Override
	public Material getIcon() {
		return Material.STICK;
	}

	@Override
	public InvSlot getSlot() {
		return InvSlot.EXTRA_ITEM;
	}

	@Override
	public Item getItem() {
		return Item.KNOCKBACK_STICK;
	}

	@Override
	public ItemAbility[] incompatibleWith() {
		return new ItemAbility[0];
	}
}
