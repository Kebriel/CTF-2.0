package kebriel.ctf.ability;

import kebriel.ctf.Constants;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.ItemAbility;
import kebriel.ctf.ability.components.ItemModificationAbility;
import kebriel.ctf.display.gui.component.Purchaseable;
import kebriel.ctf.player.Stat;
import kebriel.ctf.player.PlayerState;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.player.item.CTFItem;
import kebriel.ctf.player.item.InvSlot;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.function.Function;

public class AbilityJuggernaut implements ItemModificationAbility, Purchaseable {
	
	@Override
	public int getCost() {
		return 3500;
	}

	@Override
	public Ability getInstance() {
		return new AbilityJuggernaut();
	}
	
	@Override
	public String getName() {
		return "Juggernaut";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_JUGGERNAUT;
	}

	@Override
	public String getID() {
		return "ability_juggernaut";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ABILITY;
	}

	@Override
	public Text getDescription() {
		return Text.get().yellow("Spawn with ").aqua("Protection " + Constants.JUGGERNAUT_PROT_STRENGTH).yellow(" on your chestplate");
	}

	@Override
	public Material getIcon() {
		return Material.DIAMOND;
	}

	@Override
	public Function<CTFItem, CTFItem> getApply(PlayerState player) {
		return chestplate -> chestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
	}

	@Override
	public InvSlot getSlot() {
		return InvSlot.CHESTPLATE;
	}

	@Override
	public ItemAbility[] incompatibleWith() {
		return new ItemAbility[0];
	}
}
