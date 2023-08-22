package kebriel.ctf.event.async.components;

import kebriel.ctf.game.flag.Flag;
import kebriel.ctf.player.CTFPlayer;

public class FlagEvent extends AsyncEvent {

    private final Flag flag;

    public FlagEvent(CTFPlayer player, Flag flag) {
        super(player);
        this.flag = flag;
    }

    public Flag getFlag() {
        return flag;
    }
}
