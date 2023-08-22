package kebriel.ctf.internal.nms.event;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import kebriel.ctf.entity.components.EntityWrapper;
import kebriel.ctf.event.async.components.CTFEvent;
import kebriel.ctf.internal.concurrent.AsyncExecutor;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.util.MinecraftUtil;
import kebriel.ctf.util.ReflectionUtil;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;

public class PacketListener extends ChannelDuplexHandler {

    private final CTFPlayer player;

    public PacketListener(CTFPlayer player) {
        this.player = player;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
        AsyncExecutor.doTask(() -> {
            if(packet instanceof PacketPlayInUseEntity interact) {
                /*
                 * This searches the world for an entity that matches the ID
                 * of the one that the client thinks it just clicked
                 *
                 * If that entity actually exists, it won't return null
                 *
                 * If it doesn't, that means the client interacted with an
                 * entity that it thinks exists, but doesn't actually --
                 * aka a 'fake entity'. We then search our cache of packet
                 * entities that are registered to have been 'spawned',
                 * and if one matches the entityID of the clicked fake
                 * entity, we fire the corresponding event asynchronously
                 */
                if(interact.a(MinecraftUtil.getNMSWorld()) == null) {
                    int entityID = ReflectionUtil.getField(interact, "a");
                    // Searches a cache of 'packet entities' that are known to be currently spawned
                    EntityWrapper<?> packetEntity = EntityWrapper.getPacketEntity(entityID);
                    if(packetEntity != null)
                        CTFEvent.fireEvent(new AsyncPacketEventEntityInteract(player, packetEntity, interact.a()));
                }
            }
        });
        super.channelRead(ctx, packet);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
        super.write(ctx, packet, promise);
    }
}
