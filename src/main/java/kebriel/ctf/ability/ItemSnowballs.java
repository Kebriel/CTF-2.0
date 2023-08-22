package kebriel.ctf.ability;

import kebriel.ctf.Constants;
import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.ability.components.ItemAbility;
import kebriel.ctf.display.gui.component.Purchaseable;
import kebriel.ctf.ability.components.PlayerEffect;
import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.Stat;
import kebriel.ctf.player.item.InvSlot;
import kebriel.ctf.player.item.Item;
import kebriel.ctf.util.JavaUtil;
import kebriel.ctf.internal.player.text.Text;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ItemSnowballs implements ItemAbility, Purchaseable, EventReactor {

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
		return "Snowballs";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_ITEM_SNOW;
	}

	@Override
	public String getID() {
		return "item_snow";
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("Spawn with a stack of snowballs that")
				.newLine().primary("inflict ").gray("Slowness " + JavaUtil.asNumeral(Constants.ITEM_SNOWBALL_SLOW_STRENGTH) + " (" + Constants.ITEM_SNOWBALL_SLOW_DURATION + "s)");
	}

	@Override
	public Material getIcon() {
		return Material.SNOW_BALL;
	}

	@Override
	public InvSlot getSlot() {
		return InvSlot.EXTRA_ITEM;
	}

	@Override
	public Item getItem() {
		return Item.SNOWBALLS;
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

		if(!(proj instanceof Snowball))
			return;

		PlayerEffect.SNOWBALL_SLOW.apply(damaged);
	}
}
