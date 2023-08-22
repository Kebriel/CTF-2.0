package kebriel.ctf.entity.components;

import kebriel.ctf.internal.nms.GamePacket;
import kebriel.ctf.internal.nms.GamePacket.RotateEntity;
import kebriel.ctf.util.JavaUtil;
import net.minecraft.server.v1_8_R3.Entity;

public interface Rotatable<T extends EntityWrapper<?>> {

    private EntityWrapper<?> getEntity() {
        return (T) this;
    }

    default void rotateTo(float yaw, float pitch) {
        EntityWrapper<?> eb = getEntity();
        Entity e = eb.getEntity();

        yaw = JavaUtil.wrapAngle(yaw);
        pitch = JavaUtil.wrapAngle(pitch);

        if(eb.isRendered()) {
            GamePacket packet = new RotateEntity(e, yaw, pitch);
            packet.sendToLobby();
        }else if(eb.isSpawned()) {
            e.setPositionRotation(e.locX, e.locY, e.locZ, yaw, pitch);
        }
    }

    default void rotateBy(float yawMod, float pitchMod) {
        EntityWrapper<?> eb = getEntity();
        Entity e = eb.getEntity();
        rotateTo(e.yaw + yawMod, e.pitch + pitchMod);
    }

}
