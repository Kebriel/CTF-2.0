package kebriel.ctf.display.cosmetic.component;

import kebriel.ctf.internal.nms.GamePacket;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;

public class WrappedParticle {

    private final EnumParticle particle;
    private Location baseLoc;
    private final double xOffset, yOffset, zOffset;
    private final float data;
    private final int count;
    private boolean longDistance;
    private int[] extraData;

    public WrappedParticle(EnumParticle particle, Location baseLoc, double xOffset, double yOffset, double zOffset, float speed, int count) {
        this.particle = particle;
        this.baseLoc = baseLoc;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        data = speed;
        this.count = count;
    }

    public WrappedParticle(EnumParticle particle, Location baseLoc, double xOffset, double yOffset, double zOffset, float speed, int count, boolean longDistance, int... extraData) {
        this(particle, baseLoc, xOffset, yOffset, zOffset, speed, count);
        this.longDistance = longDistance;
        this.extraData = extraData;
    }

    public WrappedParticle(EnumParticle particle, double xOffset, double yOffset, double zOffset, float speed, int count, boolean longDistance) {
        this(particle, null, xOffset, yOffset, zOffset, speed, count, longDistance);
    }

    public void play() {
        new GamePacket.PlayParticles(particle, baseLoc, xOffset, yOffset, zOffset, data, count, longDistance, extraData)
                .send();
    }

    public void play(Location baseLoc) {
        this.baseLoc = baseLoc;
        play();
    }
}
