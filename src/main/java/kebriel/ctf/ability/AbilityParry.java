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
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class AbilityParry implements Ability, EventReactor {

	{
		EventReaction.register(this);
	}

	@Override
	public Ability getInstance() {
		return new AbilityParry();
	}

	@Override
	public Material getIcon() {
		return Material.IRON_SWORD;
	}

	@Override
	public String getName() {
		return "Parry";
	}

	@Override
	public String getID() {
		return "ability_parry";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ABILITY;
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("Blocking will reduce incoming damage by ").secondary(Constants.PARRY_DAMAGE_REDUC + "%");
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onDamage(EntityDamageEvent event) {
		if(!(event.getEntity() instanceof Player player))
			return;

		if(!CTFPlayer.get(player).getIsSelected(getID()))
			return;

		if(event.getCause() == DamageCause.ENTITY_ATTACK || event.getCause() == DamageCause.PROJECTILE) {
			event.setDamage(event.getDamage()*JavaUtil.percentageToFractionInverted(Constants.PARRY_DAMAGE_REDUC));
			player.playSound(player.getLocation(), Sound.ANVIL_LAND, 1.7f, 0.5f);
		}
	}
}
