package kebriel.ctf.internal.nms;

import kebriel.ctf.player.CTFPlayer;
import net.minecraft.server.v1_8_R3.Packet;

import java.util.ArrayList;
import java.util.List;

public class WrappedPacket {

    private final List<Packet<?>> packets;

    public static WrappedPacket wrap(Packet<?> packet) {
        return new WrappedPacket(packet);
    }

    public WrappedPacket(Packet<?> packet) {
        packets = new ArrayList<>();
        packets.add(packet);
    }

    public WrappedPacket addPacket(Packet<?> packet) {
        packets.add(packet);
        return this;
    }

    public void send(CTFPlayer player) {
        for(Packet<?> packet : packets)
            player.sendPacket(packet);
    }
}
