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

public class AbilityMassacre implements Ability, Purchaseable, EventReactor {

	{
		EventReaction.register(this);
	}
	
	@Override
	public int getCost() {
		return 2000;
	}

	@Override
	public Ability getInstance() {
		return new AbilityMassacre();
	}
	
	@Override
	public String getName() {
		return "Massacre";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_MASSACRE;
	}

	@Override
	public String getID() {
		return "ability_massacre";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ABILITY;
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("You deal ").secondary(Constants.MASSACRE_DAMAGE_MULT + "%").primary(" more damage per")
				.newLine().primary("opponent within").secondary("" + Constants.MASSACRE_RANGE).primary(" blocks of you");
	}

	@Override
	public Material getIcon() {
		return Material.IRON_AXE;
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onAttack(EntityDamageByEntityEvent event) {
		if(!(event.getEntity() instanceof Player && event.getDamager() instanceof Player attacker))
			return;

		CTFPlayer player = CTFPlayer.get(attacker);
		if(!player.getIsSelected(getID()))
			return;

		double damage = event.getDamage();
		for(CTFPlayer pl : CTFPlayer.getAllOnline()) {
			if(pl.getTeam().equals(CTFPlayer.get(attacker).getTeam()))
				continue;
			if(pl.getLocation().distance(attacker.getLocation()) <= Constants.MASSACRE_RANGE)
				damage+=(damage*JavaUtil.percentageToFraction(Constants.MASSACRE_DAMAGE_MULT));
		}
		event.setDamage(damage);
	}
}
