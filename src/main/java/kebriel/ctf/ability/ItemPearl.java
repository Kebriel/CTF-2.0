package kebriel.ctf.ability;

import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.ability.components.ItemAbility;
import kebriel.ctf.display.gui.component.Purchaseable;
import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.player.Stat;
import kebriel.ctf.internal.nms.GamePacket;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.internal.player.GameSound;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.player.item.Item;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class ItemPearl implements ItemAbility, Purchaseable, EventReactor {

	{
		EventReaction.register(this);
	}
	
	@Override
	public int getCost() {
		return 4000;
	}

	@Override
	public Ability getInstance() {
		return new ItemSnowballs();
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ITEM;
	}

	@Override
	public String getName() {
		return "Unstable Pearl";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_ITEM_PEARL;
	}

	@Override
	public String getID() {
		return "item_pearl";
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("Spawn with an ender pearl that will")
				.newLine().primary("inflict half your health worth of true")
				.newLine().primary("damage upon landing")
				.emptyLine()
				.newLine().primary("Cannot be thrown if you are holding a flag");
	}

	@Override
	public Material getIcon() {
		return Material.ENDER_PEARL;
	}

	@Override
	public Item getItem() {
		return Item.PEARL;
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onInteract(PlayerInteractEvent event) {
		CTFPlayer player = CTFPlayer.get(event.getPlayer());
		if(!player.getIsSelected(getID()))
			return;

		if(player.getState().isHoldingFlag()) {
			event.setCancelled(true);
			player.play(GameSound.ABILITY_FAIL);
		}
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onTeleport(PlayerTeleportEvent event) {
		CTFPlayer player = CTFPlayer.get(event.getPlayer());
		if(!player.getIsSelected(getID()) || event.getCause() != TeleportCause.ENDER_PEARL)
			return;

		Player p = player.getBukkitPlayer();
		// This will only trigger if a player throws a pearl, then grabs a flag before the pearl lands
		if(player.getState().isHoldingFlag()) {
			event.setCancelled(true);
			player.play(GameSound.ABILITY_FAIL);
			p.getInventory().addItem(getItem().buildItem()); // Return failed pearl
			return;
		}

		p.setHealth(p.getHealth()/2);
		new GamePacket.PlayParticles(EnumParticle.SMOKE_NORMAL, player.getLocation(), 0.3, 0.3, 0.3, 0, 70, true)
				.send();
		player.play(GameSound.TELEPORT);
	}

}
