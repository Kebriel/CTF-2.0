package kebriel.ctf.ability;

import kebriel.ctf.Constants;
import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.ability.components.ItemAbility;
import kebriel.ctf.ability.components.ItemModificationAbility;
import kebriel.ctf.player.PlayerState;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.player.item.CTFItem;
import kebriel.ctf.player.item.InvSlot;
import org.bukkit.Material;

import java.util.function.Function;

public class AbilityExpandedQuiver implements ItemModificationAbility {

	@Override
	public Ability getInstance() {
		return new AbilityExpandedQuiver();
	}

	@Override
	public Material getIcon() {
		return Material.ARROW;
	}

	@Override
	public String getName() {
		return "Expanded Quiver";
	}

	@Override
	public String getID() {
		return "ability_quiver";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ABILITY;
	}

	@Override
	public Text getDescription() {
		return Text.get().yellow("Start with ").aqua(String.valueOf(Constants.EXPANDED_QUIVER_ARROW_COUNT)).yellow(" extra arrows")
				.newLine().yellow("each life");
	}

	@Override
	public InvSlot getSlot() {
		return InvSlot.HOTBAR_8;
	}

	@Override
	public ItemAbility[] incompatibleWith() {
		return new ItemAbility[0];
	}

	@Override
	public Function<CTFItem, CTFItem> getApply(PlayerState player) {
		return arrows -> arrows.setAmount(arrows.getAmount()+Constants.EXPANDED_QUIVER_ARROW_COUNT);
	}
}
