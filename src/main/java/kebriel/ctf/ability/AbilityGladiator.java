package kebriel.ctf.ability;

import kebriel.ctf.Constants;
import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.display.gui.component.Purchaseable;
import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.player.Stat;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.util.JavaUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class AbilityGladiator implements Ability, Purchaseable, EventReactor {

	{
		EventReaction.register(this);
	}
	
	@Override
	public int getCost() {
		return 2000;
	}

	@Override
	public Ability getInstance() {
		return new AbilityGladiator();
	}
	
	@Override
	public String getName() {
		return "Gladiator";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_GLADIATOR;
	}

	@Override
	public String getID() {
		return "ability_gladiator";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ABILITY;
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("You receive ").secondary(Constants.GLADIATOR_DAMAGE_REDUC + "5%").primary(" less damage per")
				.newLine().primary("opponent within ").secondary("" + Constants.GLADIATOR_RANGE).primary(" blocks of you");
	}

	@Override
	public Material getIcon() {
		return Material.DIAMOND_CHESTPLATE;
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onAttack(EntityDamageByEntityEvent event) {
		if(!(event.getEntity() instanceof Player victim && event.getDamager() instanceof Player attacker))
			return;

		CTFPlayer player = CTFPlayer.get(attacker);
		if(!player.getIsSelected(getID()))
			return;

		double damage = event.getDamage();
		for(CTFPlayer pl : CTFPlayer.getAllOnline()) {
			if(pl.getTeam().equals(CTFPlayer.get(victim).getTeam())) continue;
			if(pl.getLocation().distance(victim.getLocation()) <= Constants.GLADIATOR_RANGE) {
				damage-=(damage*JavaUtil.percentageToFraction(Constants.GLADIATOR_DAMAGE_REDUC));
			}
		}
		event.setDamage(damage);
	}

}
