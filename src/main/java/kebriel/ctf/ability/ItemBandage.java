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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ItemBandage implements ItemAbility, Purchaseable, EventReactor {

	{
		EventReaction.register(this);
	}
	
	@Override
	public int getCost() {
		return 3500;
	}

	@Override
	public Ability getInstance() {
		return new ItemBandage();
	}
	
	@Override
	public String getName() {
		return "Medical Bandage";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_ITEM_BANDAGE;
	}

	@Override
	public String getID() {
		return "item_bandage";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ITEM;
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("Spawn with a bandage that will heal")
				.newLine().red((double)Constants.ITEM_BANDAGE_HEAL_AMOUNT/2 + "❤").primary(" and give ").gray("Resistance " + JavaUtil.asNumeral(Constants.ITEM_BANDAGE_RESIST_STRENGTH) + "(" + Constants.ITEM_BANDAGE_RESIST_DURATION + "s)")
				.newLine().primary("and ").red("Regen " + JavaUtil.asNumeral(Constants.ITEM_BANDAGE_REGEN_STRENGTH) + "(" + Constants.ITEM_BANDAGE_REGEN_DURATION + "s)").primary(" upon consumption");
	}

	@Override
	public Material getIcon() {
		return Material.PAPER;
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if(!CTFPlayer.get(player).getIsSelected(getID()))
			return;

		if(event.getItem() != null && event.getItem().getType() == Material.PAPER) {
			player.getInventory().remove(Material.PAPER);
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Constants.ITEM_BANDAGE_RESIST_DURATION*20, Constants.ITEM_BANDAGE_RESIST_STRENGTH, true, false), true);
			player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Constants.ITEM_BANDAGE_REGEN_DURATION*20, Constants.ITEM_BANDAGE_REGEN_STRENGTH, true, false), true);
			player.setHealth(Math.min(player.getHealth() + 2, player.getMaxHealth()));
		}
	}

	@Override
	public InvSlot getSlot() {
		return InvSlot.EXTRA_ITEM;
	}

	@Override
	public Item getItem() {
		return Item.BANDAGE;
	}

	@Override
	public ItemAbility[] incompatibleWith() {
		return new ItemAbility[0];
	}
}
