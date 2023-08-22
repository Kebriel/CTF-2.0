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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;


public class AbilityBraced implements Ability, Purchaseable, EventReactor {

	{
		EventReaction.register(this);
	}
	
	@Override
	public int getCost() {
		return 2000;
	}

	@Override
	public Ability getInstance() {
		return new AbilityBraced();
	}
	
	@Override
	public String getName() {
		return "Braced";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_BRACED;
	}

	@Override
	public String getID() {
		return "ability_braced";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ABILITY;
	}

	@Override
	public Text getDescription() {
		return Text.get().yellow("You receive ").aqua(Constants.BRACED_DAMAGE_REDUC + "%").yellow(" less fall damage");
	}

	@Override
	public Material getIcon() {
		return Material.FEATHER;
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onDamage(EntityDamageEvent event) {
		if(!(event.getEntity() instanceof Player player))
			return;

		if(!CTFPlayer.get(player).getIsSelected(getID()))
			return;

		if(event.getCause() != DamageCause.FALL)
			return;

		event.setDamage(event.getDamage()*JavaUtil.percentageToFractionInverted(Constants.BRACED_DAMAGE_REDUC));
	}
}
