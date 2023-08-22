package kebriel.ctf.ability;

import kebriel.ctf.Constants;
import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.ability.components.CooldownAbility;
import kebriel.ctf.display.gui.component.Purchaseable;
import kebriel.ctf.event.async.AsyncPlayerKillEvent;
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

public class AbilityBerserk implements Ability, CooldownAbility, EventReactor, Purchaseable {

	{
		EventReaction.register(this);
	}
	
	@Override
	public int getCost() {
		return 2500;
	}

	@Override
	public Ability getInstance() {
		return new AbilityBerserk();
	}
	
	@Override
	public String getName() {
		return "Berserk";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_BERSERK;
	}

	@Override
	public String getID() {
		return "ability_berserk";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ABILITY;
	}

	@Override
	public Text getDescription() {
		return Text.get().yellow("Gain a ").aqua(Constants.BERSERK_DAMAGE_BOOST + "%").yellow(" damage boost for")
				.newLine().aqua(Constants.BERSERK_COOLDOWN + "s").yellow(" after getting a kill. Does not stack.");
	}

	@Override
	public Material getIcon() {
		return Material.DIAMOND_SWORD;
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onAttack(EntityDamageByEntityEvent event) {
		if(!(event.getEntity() instanceof Player && event.getDamager() instanceof Player attacker))
			return;

		CTFPlayer player = CTFPlayer.get(attacker);
		if(!player.getIsSelected(getID()))
			return;

		if(isOnCooldown(player)) { // Under Berserk effect, gain damage boost
			double extra = event.getDamage()*JavaUtil.percentageToFraction(Constants.BERSERK_DAMAGE_BOOST);
			event.setDamage(event.getDamage()+extra);
		}
	}

	@EventReact
	public void onKill(AsyncPlayerKillEvent event) {
		CTFPlayer player = event.getPlayer();
		if(!player.getIsSelected(getID()))
			return;

		if(!isOnCooldown(player)) {
			activate(player);
		}
	}

	@Override
	public int getDuration() {
		return Constants.BERSERK_COOLDOWN;
	}
}
