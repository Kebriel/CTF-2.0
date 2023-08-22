package kebriel.ctf.ability;

import kebriel.ctf.Constants;
import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.display.gui.component.Purchaseable;
import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.Stat;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.util.JavaUtil;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class AbilityPointBlank implements Ability, Purchaseable, EventReactor {
	
	@Override
	public int getCost() {
		return 3000;
	}

	@Override
	public Ability getInstance() {
		return new AbilityPointBlank();
	}
	
	@Override
	public String getName() {
		return "Point Blank";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_BLANK;
	}

	@Override
	public String getID() {
		return "ability_blank";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ABILITY;
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("Your arrows deal ").secondary(Constants.POINT_BLANK_KNOCKBACK + "%").primary(" more knockback")
				.newLine().primary("to players within ").secondary(Constants.POINT_BLANK_RANGE).primary(" blocks of you");
	}

	@Override
	public Material getIcon() {
		return Material.STAINED_CLAY;
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onProjectileHit(EntityDamageByEntityEvent event) {
		if(!(event.getEntity() instanceof Player victim && event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player shooter))
			return;

		CTFPlayer player = CTFPlayer.get(shooter);
		if(!player.getIsSelected(getID()))
			return;

		if(!(proj instanceof Arrow))
			return;

		if(victim.getLocation().distance(shooter.getLocation()) <= Constants.POINT_BLANK_RANGE) {
			Vector knockback = proj.getLocation().getDirection().multiply(-1);
			knockback.setY(0.5); // Add some upward component to mimic Minecraft's knockback effect
			knockback.normalize();
			knockback.multiply(JavaUtil.percentageToFraction(Constants.POINT_BLANK_KNOCKBACK));

			victim.setVelocity(victim.getVelocity().add(knockback));
		}
	}
}
