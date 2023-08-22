package kebriel.ctf.ability;

import kebriel.ctf.Constants;
import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.ability.components.ItemAbility;
import kebriel.ctf.display.gui.component.Purchaseable;
import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.Stat;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.player.item.InvSlot;
import kebriel.ctf.player.item.Item;
import kebriel.ctf.util.JavaUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ItemFishingRod implements ItemAbility, Purchaseable, EventReactor {

	private static final Map<Player, List<Player>> cache = new ConcurrentHashMap<>();

	{
		EventReaction.register(this);
	}
	
	@Override
	public int getCost() {
		return 3500;
	}

	@Override
	public Ability getInstance() {
		return new ItemFishingRod();
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ITEM;
	}

	@Override
	public String getName() {
		return "Battle Rod";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_ITEM_ROD;
	}

	@Override
	public String getID() {
		return "item_rod";
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("Enemies struck with this rod will ")
				.newLine().primary("receive ").secondary(Constants.ITEM_ROD_DAMAGE_MULT + "%").primary(" extra damage from")
				.newLine().primary("your next melee hit. Does not stack.");
	}

	@Override
	public Material getIcon() {
		return Material.FISHING_ROD;
	}

	@Override
	public InvSlot getSlot() {
		return InvSlot.HOTBAR_3;
	}

	@Override
	public Item getItem() {
		return Item.FISHING_ROD;
	}

	@Override
	public ItemAbility[] incompatibleWith() {
		return new ItemAbility[0];
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onProjectileHit(EntityDamageByEntityEvent event) {
		if(!(event.getEntity() instanceof Player damaged && event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player shooter))
			return;

		CTFPlayer player = CTFPlayer.get(shooter);
		if(!player.getIsSelected(getID()))
			return;

		if(!(proj instanceof FishHook))
			return;

		cache.compute(shooter, (k, v) -> {
			if(v == null) v = new ArrayList<>();
			if(!v.contains(damaged))
				v.add(damaged);
			return v;
		});
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onAttack(EntityDamageByEntityEvent event) {
		if(!(event.getEntity() instanceof Player victim && event.getDamager() instanceof Player attacker))
			return;

		CTFPlayer player = CTFPlayer.get(attacker);
		if(!player.getIsSelected(getID()))
			return;

		if(cache.get(attacker) != null && cache.get(attacker).contains(victim)) {
			cache.get(attacker).remove(victim);
			double extra = event.getDamage()*JavaUtil.percentageToFraction(Constants.ITEM_ROD_DAMAGE_MULT);
			event.setDamage(event.getDamage()+extra);
		}
	}
}
