package kebriel.ctf.ability;

import kebriel.ctf.Constants;
import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.display.gui.component.Purchaseable;
import kebriel.ctf.event.async.AsyncFlagTakeEvent;
import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.event.reaction.ThreadControl;
import kebriel.ctf.player.Stat;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.util.JavaUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AbilityFleetFooted implements Ability, Purchaseable, EventReactor {

	{
		EventReaction.register(this);
	}
	
	@Override
	public int getCost() {
		return 2500;
	}

	@Override
	public Ability getInstance() {
		return new AbilityFleetFooted();
	}
	
	@Override
	public String getName() {
		return "Fleet-Footed";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_FLEETFOOTED;
	}

	@Override
	public String getID() {
		return "ability_fleet";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ABILITY;
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("You gain ").secondary("Speed " + JavaUtil.asNumeral(Constants.FLEET_FOOT_SPEED_MULT) + " (" + Constants.FLEET_FOOT_SPEED_DURATION + "s)").primary(" when you pickup a flag")
				.newLine().primary("You gain ").green("Jump " + JavaUtil.asNumeral(Constants.FLEET_FOOT_JUMP_MULT) + " (" + Constants.FLEET_FOOT_JUMP_DURATION + "s)").primary(" as well if it's your flag");
	}

	@Override
	public Material getIcon() {
		return Material.RABBIT_FOOT;
	}

	@EventReact(allowedWhen = GameStage.IN_GAME, thread = ThreadControl.MAIN)
	public void onFlagTake(AsyncFlagTakeEvent event) {
		if(event.getPlayer().getIsSelected(getID()))
			return;

		Player player = event.getPlayer().getBukkitPlayer();
		player.addPotionEffect(
				new PotionEffect(PotionEffectType.SPEED, Constants.FLEET_FOOT_SPEED_DURATION*20, Constants.FLEET_FOOT_SPEED_MULT, true, false), true);
		if(event.getFlag().getTeam().equals(event.getPlayer().getTeam()))
			player.addPotionEffect(
					new PotionEffect(PotionEffectType.JUMP, Constants.FLEET_FOOT_JUMP_DURATION*20, Constants.FLEET_FOOT_JUMP_MULT, true, false), true);
	}
}
