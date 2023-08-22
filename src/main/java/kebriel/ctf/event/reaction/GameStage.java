package kebriel.ctf.event.reaction;

import kebriel.ctf.game.Game;
import kebriel.ctf.game.component.phase.InGame;
import kebriel.ctf.game.component.phase.Lobby;
import kebriel.ctf.game.component.phase.PostGame;

import java.util.function.Supplier;

public enum GameStage {
    LOBBY(() -> Game.get().getPhase() instanceof Lobby),
    IN_GAME(() -> Game.get().getPhase() instanceof InGame),
    POST_GAME(() -> Game.get().getPhase() instanceof PostGame),

    WAITING(() -> LOBBY.get() && ((Lobby) Game.get().getPhase()).isWaiting()),
    VOTING(() -> Game.get().isVoting()),
    IS_STARTING(() -> Game.get().isStarting()),
    GRACE_PERIOD(() -> Game.get().isGracePeriod()),
    DEATHMATCH(() -> Game.get().isDeathmatch()),
    PLAYING(() -> IN_GAME.get() && !GRACE_PERIOD.get()),

    IN_MAP(() -> IN_GAME.get() || POST_GAME.get()),

    UNDEFINED(() -> true)
    ;

    private final Supplier<Boolean> determine;

    GameStage(Supplier<Boolean> determine) {
        this.determine = determine;
    }

    public boolean get() {
        return determine.get();
    }
}
