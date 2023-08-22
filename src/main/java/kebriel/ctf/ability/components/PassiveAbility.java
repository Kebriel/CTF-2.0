package kebriel.ctf.ability.components;

import kebriel.ctf.player.CTFPlayer;

public interface PassiveAbility extends SpawnAbility {

    @Override
    default void apply(CTFPlayer player) {
        start(player);
    }

    void start(CTFPlayer player);
    void terminate(CTFPlayer player);
}
