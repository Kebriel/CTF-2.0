package kebriel.ctf.ability;

import kebriel.ctf.Constants;
import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.ability.components.ItemAbility;
import kebriel.ctf.display.gui.component.Purchaseable;
import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.async.AsyncFlagCaptureEvent;
import kebriel.ctf.event.async.AsyncFlagReturnEvent;
import kebriel.ctf.event.async.AsyncPlayerAssistEvent;
import kebriel.ctf.event.async.AsyncPlayerKillEvent;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.player.Stat;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.player.item.InvSlot;
import kebriel.ctf.player.item.Item;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

public class PerkRoyalty implements ItemAbility, Purchaseable, EventReactor {

	{
		EventReaction.register(this);
	}
	
	@Override
	public int getCost() {
		return 8000;
	}

	@Override
	public Ability getInstance() {
		return new PerkRoyalty();
	}

	@Override
	public AbilityType getType() {
		return AbilityType.PERK;
	}

	@Override
	public String getName() {
		return "Royalty";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_PERK_ROYALTY;
	}

	@Override
	public String getID() {
		return "perk_royalty";
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("You and all players on your team receive")
				.newLine().secondary("+" + Constants.ROYALTY_EXTRA_XP_KA + "XP").primary(" and ").gold("+" + Constants.ROYALTY_EXTRA_GOLD_KA + "g").primary("from kills and assists,")
				.newLine().primary("and ").secondary("+" + Constants.ROYALTY_EXTRA_XP_MISC + "XP").primary(" and ").gold("+" + Constants.ROYALTY_EXTRA_GOLD_MISC + "g").primary(" from all other sources")
				.emptyLine()
				.newLine().primary("You will wear a gold helmet");
	}

	@Override
	public Material getIcon() {
		return Material.GOLD_HELMET;
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onKill(AsyncPlayerKillEvent event) {
		event.getXPReward().addAndGet(Constants.ROYALTY_EXTRA_XP_KA);
		event.getGoldReward().addAndGet(Constants.ROYALTY_EXTRA_GOLD_KA);
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onAssist(AsyncPlayerAssistEvent event) {
		event.getXPReward().addAndGet(Constants.ROYALTY_EXTRA_XP_KA);
		event.getGoldReward().addAndGet(Constants.ROYALTY_EXTRA_GOLD_KA);
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onFlagCapture(AsyncFlagCaptureEvent event) {
		event.getXPReward().addAndGet(Constants.ROYALTY_EXTRA_XP_MISC);
		event.getXPReward().addAndGet(Constants.ROYALTY_EXTRA_GOLD_MISC);
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onFlagReturn(AsyncFlagReturnEvent event) {
		event.getXPReward().addAndGet(Constants.ROYALTY_EXTRA_XP_MISC);
		event.getGoldReward().addAndGet(Constants.ROYALTY_EXTRA_GOLD_MISC);
	}

	@Override
	public InvSlot getSlot() {
		return InvSlot.HELMET;
	}

	@Override
	public Item getItem() {
		return Item.ROYALTY_CROWN;
	}

	@Override
	public ItemAbility[] incompatibleWith() {
		return new ItemAbility[0];
	}
}
