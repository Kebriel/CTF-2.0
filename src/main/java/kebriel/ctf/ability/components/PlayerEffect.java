package kebriel.ctf.ability.components;

import kebriel.ctf.Constants;
import kebriel.ctf.ability.AbilityAntidote;
import kebriel.ctf.player.CTFPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.function.Consumer;

public enum PlayerEffect {
    FIREBRAND_FIRE_TICKS(player -> player.setFireTicks(Constants.FIREBRAND_FIRE_DURATION * 20)),
    SNOWBALL_SLOW(player -> player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Constants.ITEM_SNOWBALL_SLOW_DURATION * 20, Constants.ITEM_SNOWBALL_SLOW_DURATION + 1, true, false), true));
    //...

    private final Consumer<Player> effect;

    PlayerEffect(Consumer<Player> effect) {
        this.effect = effect;
    }

    public void apply(Player player) {
        if (!CTFPlayer.get(player).getIsAbilitySelected(AbilityAntidote.class))
            effect.accept(player);
    }
}
