package kebriel.ctf.internal.nms;

import kebriel.ctf.player.CTFPlayer;

import java.util.Collection;
import java.util.Set;

public class RenderablePacket {

    private final GamePacket packet;

    public RenderablePacket(GamePacket packet) {
        this.packet = packet;
    }

    /**
     * Adds a player to the list of players who will have this
     * packet continuously rendered for them, and renders it
     * for them specifically
     */
    public void renderFor(CTFPlayer player) {
        packet.sendFor(player);
    }

    /**
     * Removes a player from the list of players who continuously
     * see this packet, and unrenders the packet for them specifically
     */
    public void unrenderFor(CTFPlayer player) {
        if(!player.valid()) return;
        packet.undoFor(player);
    }

    public void fullRender() {
        packet.sendToLobby();
    }

    public void addReceivers(Collection<CTFPlayer> receivingPlayers) {
        packet.addReceivers(receivingPlayers);
        PacketRegistry.updateThis(this);
    }

    public void addReceivers(CTFPlayer... receivingPlayers) {
        packet.addReceivers(receivingPlayers);
        PacketRegistry.updateThis(this);
    }

    public void setReceivers(Collection<CTFPlayer> receivingPlayers) {
        packet.setReceivers(receivingPlayers);
        PacketRegistry.updateThis(this);
    }

    public void removeReceiver(CTFPlayer player) {
        packet.removeReceiver(player);
        PacketRegistry.updateThis(this);
    }

    public void removeReceivers(CTFPlayer... players) {
        for(CTFPlayer player : players) {
            packet.removeReceiver(player);
        }
        PacketRegistry.updateThis(this);
    }

    public void addReceiver(CTFPlayer player) {
        packet.addReceiver(player);
        PacketRegistry.updateThis(this);
    }

    public Set<CTFPlayer> getReceivers() {
        return packet.getReceivers();
    }

    public GamePacket getGamePacket() {
        return packet;
    }
}
