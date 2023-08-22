package kebriel.ctf.ability;

import kebriel.ctf.CTFMain;
import kebriel.ctf.Constants;
import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.ability.components.ItemAbility;
import kebriel.ctf.display.gui.component.Purchaseable;
import kebriel.ctf.event.async.AsyncPlayerKillEvent;
import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.event.reaction.ThreadControl;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.Stat;
import kebriel.ctf.internal.nms.GamePacket;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.player.item.InvSlot;
import kebriel.ctf.player.item.Item;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ItemBlocks implements ItemAbility, Purchaseable, EventReactor {

	{
		EventReaction.register(this);
	}
	
	@Override
	public int getCost() {
		return 3000;
	}

	@Override
	public Ability getInstance() {
		return new ItemBlocks();
	}
	
	@Override
	public String getName() {
		return "Ghost Blocks";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_ITEM_BLOCKS;
	}

	@Override
	public String getID() {
		return "item_blocks";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ITEM;
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("Spawn with ").secondary(Constants.ITEM_BLOCKS_SPAWN_AMOUNT).primary(" glass blocks")
				.newLine().primary("that cannot be broken, but disappear after ").secondary(Constants.ITEM_BLOCKS_DURATION + "s")
				.emptyLine()
				.newLine().primary("Gain ").secondary(Constants.ITEM_BLOCKS_KILL_REWARD).primary(" per kill");
	}

	@Override
	public Material getIcon() {
		return Material.GLASS;
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onBlockPlace(BlockPlaceEvent event) {
		CTFPlayer player = CTFPlayer.get(event.getPlayer());
		if(!player.getIsSelected(getID()))
			return;

		if(event.getBlockPlaced().getType() == Material.GLASS) {
			Block b = event.getBlockPlaced();
			BlockState state = event.getBlockReplacedState();
			event.setCancelled(false);
			new BukkitRunnable() {
				@Override
				public void run() {
					b.setType(state.getType());
					b.setData(state.getData().getData());
					new GamePacket.PlayParticles(EnumParticle.CLOUD, b.getLocation(), 0.5f, 0.5f, 0.5f, 0, 30, false)
							.send();
				}

			}.runTaskLater(CTFMain.instance, Constants.ITEM_BLOCKS_DURATION*20);
		}
	}

	@EventReact(allowedWhen = GameStage.IN_GAME, thread = ThreadControl.MAIN)
	public void onKill(AsyncPlayerKillEvent event) {
		CTFPlayer player = event.getPlayer();
		if(!player.getIsSelected(getID()))
			return;

		event.getPlayer().getBukkitPlayer().getInventory().addItem(getItem().getItem().setAmount(Constants.ITEM_BLOCKS_KILL_REWARD).build());
	}

	@Override
	public InvSlot getSlot() {
		return InvSlot.EXTRA_ITEM;
	}

	@Override
	public Item getItem() {
		return Item.GHOST_BLOCKS;
	}

	@Override
	public ItemAbility[] incompatibleWith() {
		return new ItemAbility[0];
	}
}
