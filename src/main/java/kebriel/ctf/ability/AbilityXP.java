package kebriel.ctf.ability;

import kebriel.ctf.Constants;
import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.display.gui.component.Purchaseable;
import kebriel.ctf.event.async.AsyncPlayerAssistEvent;
import kebriel.ctf.event.async.AsyncPlayerKillEvent;
import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.player.Stat;
import kebriel.ctf.internal.player.text.Text;
import org.bukkit.Material;

public class AbilityXP implements Ability, Purchaseable, EventReactor {

	{
		EventReaction.register(this);
	}
	
	@Override
	public int getCost() {
		return 2000;
	}

	@Override
	public Ability getInstance() {
		return new AbilityXP();
	}
	
	@Override
	public String getName() {
		return "Essence Extraction";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_XP_ABILITY;
	}

	@Override
	public String getID() {
		return "ability_xp";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ABILITY;
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("You gain ").secondary(Constants.XP_MULT + "x").primary(" as much xp from")
				.newLine().primary("kills and assists");
	}

	@Override
	public Material getIcon() {
		return Material.EXP_BOTTLE;
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onKill(AsyncPlayerKillEvent event) {
		if(!event.getPlayer().getIsSelected(getID()))
			return;
		event.getXPReward().updateAndGet(i -> i*Constants.XP_MULT);
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onAssist(AsyncPlayerAssistEvent event) {
		if(!event.getPlayer().getIsSelected(getID()))
			return;
		event.getXPReward().updateAndGet(i -> i*Constants.XP_MULT);
	}

}
