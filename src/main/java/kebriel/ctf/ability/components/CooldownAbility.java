package kebriel.ctf.ability.components;

import kebriel.ctf.player.CTFPlayer;

public interface CooldownAbility {

    int getDuration();

    default void activate(CTFPlayer player) {
        player.getState().putOnCooldown(this, getDuration());
    }

    default boolean isOnCooldown(CTFPlayer player) {
        return player.getState().isOnCooldown(this);
    }
}
