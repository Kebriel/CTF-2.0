package kebriel.ctf.ability;

import kebriel.ctf.Constants;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.ItemAbility;
import kebriel.ctf.ability.components.PlayerEffect;
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
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemFlag;

public class AbilityFirebrand implements ItemAbility, Purchaseable, EventReactor {

	{
		EventReaction.register(this);
	}
	
	@Override
	public int getCost() {
		return 2500;
	}

	@Override
	public Ability getInstance() {
		return new AbilityFirebrand();
	}
	
	@Override
	public String getName() {
		return "Firebrand";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_FIREBRAND;
	}

	@Override
	public String getID() {
		return "ability_firebrand";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ABILITY;
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("Your sword is replaced by a wooden sword")
				.newLine().primary("that deals less damage but inflicts")
				.newLine().secondary(Constants.FIREBRAND_FIRE_DURATION + "s").primary(" fire upon enemies with every hit");
	}

	@Override
	public Material getIcon() {
		return Material.BLAZE_POWDER;
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onAttack(EntityDamageByEntityEvent event) {
		if(!(event.getEntity() instanceof Player victim && event.getDamager() instanceof Player attacker))
			return;

		CTFPlayer player = CTFPlayer.get(attacker);
		if(!player.getIsSelected(getID()))
			return;

		if(!(attacker.getItemInHand() != null
				&& attacker.getItemInHand().isSimilar(getItem().buildItem())))
			return;
		PlayerEffect.FIREBRAND_FIRE_TICKS.apply(victim);
	}

	@Override
	public InvSlot getSlot() {
		return InvSlot.HOTBAR_1;
	}

	@Override
	public Item getItem() {
		return Item.FIREBRAND;
	}

	@Override
	public ItemAbility[] incompatibleWith() {
		return new ItemAbility[0];
	}
}
