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
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class AbilityKevlar implements ItemAbility, Purchaseable, EventReactor {

	{
		EventReaction.register(this);
	}
	
	@Override
	public int getCost() {
		return 3000;
	}

	@Override
	public Ability getInstance() {
		return new AbilityKevlar();
	}
	
	@Override
	public String getName() {
		return "Kevlar";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_KEVLAR;
	}

	@Override
	public String getID() {
		return "ability_kevlar";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ABILITY;
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("Your chestplate is replaced by a chainmail chestplate")
				.newLine().primary("You receive ").secondary(Constants.KEVLAR_DAMAGE_REDUC + "%").primary(" less damage from")
				.newLine().primary("ranged attacks");
	}

	@Override
	public Material getIcon() {
		return Material.CHAINMAIL_CHESTPLATE;
	}

	@Override
	public InvSlot getSlot() {
		return InvSlot.CHESTPLATE;
	}

	@Override
	public Item getItem() {
		return Item.KEVLAR_VEST;
	}

	@Override
	public ItemAbility[] incompatibleWith() {
		return new ItemAbility[0];
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onProjectileHit(EntityDamageByEntityEvent event) {
		if(!(event.getEntity() instanceof Player victim && event.getDamager() instanceof Projectile))
			return;

		CTFPlayer player = CTFPlayer.get(victim);
		if(!player.getIsSelected(getID()))
			return;

		double damage = event.getDamage();
		event.setDamage(damage*JavaUtil.percentageToFractionInverted(Constants.KEVLAR_DAMAGE_REDUC));
	}
}
