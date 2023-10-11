package kebriel.ctf.ability;

import kebriel.ctf.Constants;
import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.ability.components.SpawnAbility;
import kebriel.ctf.display.gui.component.Purchaseable;
import kebriel.ctf.player.Stat;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.util.JavaUtil;
import kebriel.ctf.util.MinecraftUtil;
import kebriel.ctf.internal.player.text.Text;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PerkHaste implements SpawnAbility, Purchaseable {
	
	@Override
	public int getCost() {
		return 7000;
	}

	@Override
	public Ability getInstance() {
		return new PerkHaste();
	}
	
	@Override
	public String getName() {
		return "Haste";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_PERK_HASTE;
	}

	@Override
	public String getID() {
		return "perk_haste";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.PERK;
	}

	@Override
	public Text getDescription() {
		return Text.get().primary("You spawn with ").secondary("Speed " + JavaUtil.asNumeral(Constants.PERK_HASTE_SPEED_STRENGTH) + " (" + Constants.PERK_HASTE_SPEED_DURATION + "s)");
	}

	@Override
	public Material getIcon() {
		return Material.SUGAR;
	}

	@Override
	public void apply(CTFPlayer player) {
		MinecraftUtil.doSyncIfNot(() -> player.getBukkitPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Constants.PERK_HASTE_SPEED_DURATION*20, Constants.PERK_HASTE_SPEED_STRENGTH, true, false), true));
	}
}
