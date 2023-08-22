package kebriel.ctf.ability;

import kebriel.ctf.Constants;
import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.display.gui.component.Purchaseable;
import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.Stat;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.util.JavaUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Random;

public class AbilityLeech implements Ability, Purchaseable, EventReactor {

	{
		EventReaction.register(this);
	}
	
	@Override
	public int getCost() {
		return 3500;
	}

	@Override
	public Ability getInstance() {
		return new AbilityLeech();
	}
	
	@Override
	public String getName() {
		return "Leech";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_LEECH;
	}

	@Override
	public String getID() {
		return "ability_leech";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ABILITY;
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("Your attacks have a ").secondary(Constants.LEECH_HEAL_CHANCE + "%").primary(" chance")
				.newLine().primary("to heal you ").red(((double)Constants.LEECH_HEAL_AMOUNT/2) + "❤");
	}

	@Override
	public Material getIcon() {
		return Material.SPIDER_EYE;
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onAttack(EntityDamageByEntityEvent event) {
		if(!(event.getEntity() instanceof Player && event.getDamager() instanceof Player attacker))
			return;

		CTFPlayer player = CTFPlayer.get(attacker);
		if(!player.getIsSelected(getID()))
			return;

		if(new Random().nextDouble() < JavaUtil.percentageToFraction(Constants.LEECH_HEAL_CHANCE))
			attacker.setHealth(Math.min(attacker.getHealth() + 2, attacker.getMaxHealth()));
	}
}
