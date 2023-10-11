package kebriel.ctf.ability;

import kebriel.ctf.Constants;
import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.ability.components.PassiveAbility;
import kebriel.ctf.display.gui.component.Purchaseable;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.internal.concurrent.AsyncExecutor;
import kebriel.ctf.player.Stat;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.util.JavaUtil;
import kebriel.ctf.util.MinecraftUtil;
import kebriel.ctf.internal.player.text.Text;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.TimeUnit;

public class AbilityIronskin implements PassiveAbility, Purchaseable, EventReactor {

	{
		EventReaction.register(this);
	}
	
	@Override
	public int getCost() {
		return 2500;
	}

	@Override
	public Ability getInstance() {
		return new AbilityIronskin();
	}
	
	@Override
	public String getName() {
		return "Ironskin";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_IRONSKIN;
	}

	@Override
	public String getID() {
		return "ability_ironskin";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ABILITY;
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("You have permanent ").secondary("Resistance " + JavaUtil.asNumeral(Constants.IRONSKIN_RESISTANCE_MULT)).primary(" and")
				.newLine().secondary("Slowness " + JavaUtil.asNumeral(Constants.IRONSKIN_SLOW_MULT));
	}

	@Override
	public Material getIcon() {
		return Material.ANVIL;
	}

	@Override
	public void start(CTFPlayer player) {
		AsyncExecutor.doAfterDelay(() -> {
			if(player.getState().isDead() || !player.isOnline() || !player.getIsSelected(getID()))
				return;

			MinecraftUtil.runOnMainThread(() -> {
				player.getBukkitPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 0, true, false), true);
				player.getBukkitPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 0, true, false), true);
			});
			start(player);
		}, 4, TimeUnit.SECONDS);
	}

	@Override
	public void terminate(CTFPlayer player) {}
}
