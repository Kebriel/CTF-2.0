package kebriel.ctf.ability;

import kebriel.ctf.Constants;
import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.util.JavaUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class AbilityCleanStrike implements Ability, EventReactor {

	{
		EventReaction.register(this);
	}

	@Override
	public Ability getInstance() {
		return new AbilityCleanStrike();
	}
	
	@Override
	public String getName() {
		return "Clean Strike";
	}

	@Override
	public String getID() {
		return "ability_clean";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ABILITY;
	}

	@Override
	public Text getDescription() {
		return Text.get().yellow("Your strikes deal ").aqua(Constants.CLEAN_STRIKE_DAMAGE_MULT + "%").yellow(" more damage")
				.newLine().yellow("to players with full health");
	}

	@Override
	public Material getIcon() {
		return Material.QUARTZ;
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onAttack(EntityDamageByEntityEvent event) {
		if(!(event.getEntity() instanceof Player victim && event.getDamager() instanceof Player attacker))
			return;

		CTFPlayer player = CTFPlayer.get(attacker);
		if(!player.getIsSelected(getID()))
			return;

		if(victim.getHealth() != victim.getMaxHealth())
			return;

		double extraDamage = event.getDamage()*JavaUtil.percentageToFraction(Constants.CLEAN_STRIKE_DAMAGE_MULT);
		event.setDamage(event.getDamage()+extraDamage);
	}
}
