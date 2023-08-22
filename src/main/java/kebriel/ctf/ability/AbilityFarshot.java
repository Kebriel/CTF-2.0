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
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class AbilityFarshot implements Ability, Purchaseable, EventReactor {

	{
		EventReaction.register(this);
	}
	
	@Override
	public int getCost() {
		return 2000;
	}

	@Override
	public Ability getInstance() {
		return new AbilityFarshot();
	}
	
	@Override
	public String getName() {
		return "Far Shot";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_FARSHOT;
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("Your arrows deal ").secondary(Constants.FAR_SHOT_DAMAGE_MULT + "%").primary(" more damage")
				.newLine().primary("for every block they fly past the ").secondary(Constants.FAR_SHOT_DISTANCE_THRESHOLD + "th");
	}

	@Override
	public String getID() {
		return "ability_farshot";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ABILITY;
	}

	@Override
	public Material getIcon() {
		return Material.EYE_OF_ENDER;
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onProjectileHit(EntityDamageByEntityEvent event) {
		if(!(event.getEntity() instanceof Player && event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player shooter))
			return;

		CTFPlayer player = CTFPlayer.get(shooter);
		if(!player.getIsSelected(getID()))
			return;

		if(!(proj instanceof Arrow))
			return;

		double distance = shooter.getLocation().distance(event.getEntity().getLocation());
		int damagemult = (int) Math.floor(distance-Constants.FAR_SHOT_DISTANCE_THRESHOLD);
		if(damagemult > 0)
			event.setDamage(event.getDamage()+(damagemult*(double)Constants.FAR_SHOT_DAMAGE_MULT/100));
	}
}
