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


public class AbilityBastion implements Ability, Purchaseable, EventReactor {

	{
		EventReaction.register(this);
	}
	
	@Override
	public int getCost() {
		return 3000;
	}

	@Override
	public Ability getInstance() {
		return new AbilityBastion();
	}
	
	@Override
	public String getName() {
		return "Bastion";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_BASTION;
	}

	@Override
	public Material getIcon() {
		return Material.IRON_BLOCK;
	}

	@Override
	public String getID() {
		return "ability_bastion";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ABILITY;
	}

	@Override
	public Text getDescription() {
		return Text.get().yellow("You receive ").aqua(Constants.BASTION_KNOCKBACK_REDUC + "%").yellow(" less knockback")
				.newLine().yellow("from attacks");
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onDamage(EntityDamageEvent event) {
		if(!(event.getEntity() instanceof Player player))
			return;

		if(!CTFPlayer.get(player).getIsSelected(getID()))
			return;

		double reduc = JavaUtil.percentageToFractionInverted(Constants.BASTION_KNOCKBACK_REDUC);
		player.setVelocity(player.getVelocity().multiply(reduc));
	}
}
