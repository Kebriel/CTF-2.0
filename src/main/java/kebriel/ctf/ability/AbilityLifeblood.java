package kebriel.ctf.ability;

import kebriel.ctf.Constants;
import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.ability.components.SpawnAbility;
import kebriel.ctf.display.gui.component.Purchaseable;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.player.Stat;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.internal.player.text.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AbilityLifeblood implements SpawnAbility, Purchaseable, EventReactor {

	@Override
	public int getCost() {
		return 3000;
	}

	@Override
	public Ability getInstance() {
		return new AbilityLifeblood();
	}
	
	@Override
	public String getName() {
		return "Lifeblood";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_LIFEBLOOD;
	}

	@Override
	public String getID() {
		return "ability_lifeblood";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ABILITY;
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("Spawn with an extra ").red(((double)Constants.LIFEBLOOD_EXTRA_HEALTH/2) + "❤").primary(" of max health");
	}

	@Override
	public Material getIcon() {
		return Material.REDSTONE_BLOCK;
	}

	@Override
	public void apply(CTFPlayer player) {
		Player p = player.getBukkitPlayer();
		p.setMaxHealth(p.getMaxHealth()+Constants.LIFEBLOOD_EXTRA_HEALTH);
	}
}
