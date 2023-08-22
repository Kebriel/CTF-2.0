package kebriel.ctf.internal.nms;

public interface Revertable {

    default void undo() {
        if(this instanceof GamePacket packet)
            getRevertedPacket().setReceivers(packet.getReceivers()).send();
    }

    GamePacket getRevertedPacket();

}
