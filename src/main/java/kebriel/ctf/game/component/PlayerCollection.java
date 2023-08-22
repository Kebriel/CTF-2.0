package kebriel.ctf.game.component;

import kebriel.ctf.player.CTFPlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerCollection {

    private final List<CTFPlayer> players;

    public PlayerCollection() {
        players = new CopyOnWriteArrayList<>();
    }

    public void add(CTFPlayer p) {
        players.add(p);
    }

    public void add(Player p) {
        add(CTFPlayer.get(p.getUniqueId()));
    }

    public void addAll(Collection<? extends CTFPlayer> toAdd) {
        players.addAll(toAdd);
    }

    public void remove(CTFPlayer p) {
        players.remove(p);
    }

    public void clear() {
        players.clear();
    }

    public List<CTFPlayer> getPlayers() {
        return players;
    }

    public int getSize() {
        return players.size();
    }

    public boolean containsPlayer(Player player) {
        return containsPlayer(CTFPlayer.get(player));
    }

    public boolean containsPlayer(CTFPlayer player) {
        return players.contains(player);
    }

    public boolean containsBoth(CTFPlayer a, CTFPlayer b) {
        return containsPlayer(a) && containsPlayer(b);
    }

    public CTFPlayer takeLast() {
        return players.get(players.size()-1);
    }
}
