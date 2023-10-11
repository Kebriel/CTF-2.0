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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AbilityMending implements Ability, EventReactor {

	{
		EventReaction.register(this);
	}

	@Override
	public Ability getInstance() {
		return new AbilityMending();
	}
	
	@Override
	public String getName() {
		return "Mending";
	}

	@Override
	public String getID() {
		return "ability_mending";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ABILITY;
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("Receive ").red("Regen " + JavaUtil.asNumeral(Constants.MENDING_REGEN_STRENGTH) + " (" + Constants.MENDING_REGEN_DURATION + "s)").primary(" when you")
				.newLine().primary("fall below ").red(((double)Constants.MENDING_THRESHOLD/2) + "❤");
	}

	@Override
	public Material getIcon() {
		return Material.REDSTONE;
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onDamage(EntityDamageEvent event) {
		if(!(event.getEntity() instanceof Player player))
			return;

		if(!CTFPlayer.get(player).getIsSelected(getID()))
			return;

		if(player.getHealth() - event.getDamage() < Constants.MENDING_THRESHOLD && player.getHealth() >= Constants.MENDING_THRESHOLD)
			player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Constants.MENDING_REGEN_DURATION*20, Constants.MENDING_REGEN_STRENGTH, true, false));
	}
}
