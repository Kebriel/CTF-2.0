package kebriel.ctf.ability;

import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.internal.player.text.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AbilityAntidote implements Ability {

	@Override
	public Ability getInstance() {
		return new AbilityAntidote();
	}
	
	@Override
	public String getName() {
		return "Antidote";
	}

	@Override
	public Material getIcon() {
		return Material.FERMENTED_SPIDER_EYE;
	}

	@Override
	public String getID() {
		return "ability_antidote";
	}

	@Override
	public AbilityType getType() {
		return null;
	}

	@Override
	public Text getDescription() {
		return Text.get().yellow("You are immune to player-inflicted debuffs");
	}
}
