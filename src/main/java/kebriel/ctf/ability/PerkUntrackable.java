package kebriel.ctf.ability;

import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.ability.components.PassiveAbility;
import kebriel.ctf.display.gui.component.Purchaseable;
import kebriel.ctf.event.async.AsyncFlagTakeEvent;
import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.game.Teams;
import kebriel.ctf.internal.nms.GamePacket.AlterPlayerNametag;
import kebriel.ctf.internal.nms.GamePacket.AlterPlayerNametag.NametagVisibilityAction;
import kebriel.ctf.internal.player.GameMessage;
import kebriel.ctf.internal.player.GameSound;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.Stat;
import org.bukkit.Material;

public class PerkUntrackable implements PassiveAbility, Purchaseable, EventReactor {

	{
		EventReaction.register(this);
	}
	
	@Override
	public int getCost() {
		return 8000;
	}

	@Override
	public Ability getInstance() {
		return new PerkUntrackable();
	}
	
	@Override
	public String getName() {
		return "Stealth";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_PERK_UNTRACKABLE;
	}

	@Override
	public String getID() {
		return "perk_untrackable";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.PERK;
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("You cannot be tracked while holding a flag")
				.newLine().primary("and players cannot see your nametag");
	}

	@Override
	public Material getIcon() {
		return Material.COAL;
	}

	@Override
	public void start(CTFPlayer player) {
		AlterPlayerNametag packet = AlterPlayerNametag.setVisibility(player, NametagVisibilityAction.SET);
		packet.setReceiverSource(() -> Teams.getAllPlayersInTeamsBesides(player.getTeam()));
		packet.render();
		player.getState().addUntrackableRender(packet);
	}

	@Override
	public void terminate(CTFPlayer player) {
		player.getState().clearUntrackableRender();
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onFlagTake(AsyncFlagTakeEvent event) {
		if(!event.getPlayer().getIsSelected(getID()))
			return;

		for(CTFPlayer p : Teams.getAllPlayersInTeamsBesides(event.getPlayer().getTeam())) {
			p.getState().getTracker().reset();
			p.send(GameMessage.TRACKER_FAILURE_UNTRACKABLE_CANCEL);
			p.play(GameSound.ABILITY_FAIL);
		}
	}
}
