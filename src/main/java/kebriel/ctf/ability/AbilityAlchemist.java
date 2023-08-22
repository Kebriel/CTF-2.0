package kebriel.ctf.ability;

import kebriel.ctf.Constants;
import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.AbilityType;
import kebriel.ctf.ability.components.ComplexDeselect;
import kebriel.ctf.ability.components.PassiveAbility;
import kebriel.ctf.display.gui.component.Purchaseable;
import kebriel.ctf.event.async.AsyncGamePhaseEnd;
import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.internal.concurrent.AsyncExecutor;
import kebriel.ctf.player.Stat;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.item.CTFItem;
import kebriel.ctf.internal.player.text.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AbilityAlchemist implements PassiveAbility, Purchaseable, EventReactor, ComplexDeselect {

	private static final Set<CTFPlayer> cache = new HashSet<>();

	{
		EventReaction.register(this);
	}
	
	@Override
	public int getCost() {
		return 3000;
	}

	@Override
	public Ability getInstance() {
		return new AbilityAlchemist();
	}
	
	@Override
	public String getName() {
		return "Alchemist";
	}

	@Override
	public Stat getUnlockData() {
		return Stat.UNLOCKED_ALCHEMIST;
	}

	@Override
	public Material getIcon() {
		return Material.BREWING_STAND_ITEM;
	}

	@Override
	public String getID() {
		return "ability_alchemist";
	}

	@Override
	public AbilityType getType() {
		return AbilityType.ABILITY;
	}

	@Override
	public Text getDescription() {
		return Text.get().yellow("Every ").aqua(Constants.ALCHEMIST_COOLDOWN + "s").yellow(", brew a random helpful")
				.newLine().yellow("potion")
				.newLine().text(" ");
	}

	@Override
	public void start(CTFPlayer player) {
		cache.add(player);
		AsyncExecutor.doAfterDelay(() -> {
			if(player.getState().isDead() || !player.isOnline() || !cache.contains(player) || !GameStage.IN_GAME.get())
				return;

			CTFItem potion = CTFItem.newItem(Material.POTION);
			switch(new Random().nextInt(6)) {
				case 0 -> potion.setName(Text.get().aqua("Potion of Healing").toString())
						.addAffectToPotion(PotionEffectType.HEAL, 1, 1, false)
						.addLore(Text.get().red("Instant Healing II").toString());
				case 1 -> potion.setName(Text.get().aqua("Potion of Regeneration").toString())
						.addAffectToPotion(PotionEffectType.HEAL, 1, 200, false)
						.addLore(Text.get().red("Regeneration II (10s)").toString());
			}

			// TODO finish reimplementing remaining potions from CTF 1.0

			/*
			case 1: pot = new ItemBuilder(pot, ChatColor.AQUA + "Potion of Regen").addAffectToPotion(PotionEffectType.REGENERATION, 1, 200, false).toItem();
								pot = new ItemBuilder(pot).addLore(ChatColor.RED + "Regeneration II (10s)").toItem();
								break;
								case 2: pot = new ItemBuilder(pot, ChatColor.AQUA + "Potion of Absorption").addAffectToPotion(PotionEffectType.ABSORPTION, 1, 1000, false).toItem();
								pot = new ItemBuilder(pot).addLore(ChatColor.YELLOW + "Absorbtion II (50s)").toItem();
								break;
								case 3: pot = new ItemBuilder(pot, ChatColor.AQUA + "Potion of Speed").addAffectToPotion(PotionEffectType.SPEED, 1, 200, false).toItem();
								pot = new ItemBuilder(pot).addLore(ChatColor.AQUA + "Speed II (10s)").toItem();
								break;
								case 4: pot = new ItemBuilder(pot, ChatColor.AQUA + "Potion of Jump Boost").addAffectToPotion(PotionEffectType.JUMP, 1, 200, false).toItem();
								pot = new ItemBuilder(pot).addLore(ChatColor.GREEN + "Jump Boost II (10s)").toItem();
								break;
								case 5: pot = new ItemBuilder(pot, ChatColor.AQUA + "Potion of Strength").addAffectToPotion(PotionEffectType.INCREASE_DAMAGE, 0, 200, false).toItem();
								pot = new ItemBuilder(pot).addLore(ChatColor.RED + "Strength I (10s)").toItem();
								break;
								}
			 */

			start(player);
		}, Constants.ALCHEMIST_COOLDOWN, TimeUnit.SECONDS);
	}

	@Override
	public void terminate(CTFPlayer player) {
		cache.remove(player);
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onQuit(PlayerQuitEvent event) {
		CTFPlayer player = CTFPlayer.get(event.getPlayer());
		cache.remove(player);
	}

	@EventReact(allowedWhen = GameStage.IN_GAME)
	public void onGameEnd(AsyncGamePhaseEnd event) {
		cache.clear();
	}

	@Override
	public void deselect(CTFPlayer player) {
		cache.remove(player);
	}
}
