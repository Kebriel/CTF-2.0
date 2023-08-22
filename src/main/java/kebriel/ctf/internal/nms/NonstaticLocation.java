package kebriel.ctf.internal.nms;

import kebriel.ctf.entity.components.EntityBase;
import kebriel.ctf.util.JavaUtil;
import kebriel.ctf.util.MinecraftUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class NonstaticLocation {

    private volatile Location location;
    private final Entity subject;
    private final Object lock = new Object();

    public static NonstaticLocation fromBukkitEntity(Entity entity) {
        return new NonstaticLocation(entity);
    }

    public static NonstaticLocation fromNMSEntity(net.minecraft.server.v1_8_R3.Entity entity) {
        return new NonstaticLocation(entity.getBukkitEntity());
    }

    public static NonstaticLocation fromPlayer(Player player) {
        return new NonstaticLocation(player);
    }

    public static NonstaticLocation fromBaseEntity(EntityBase entity) {
        return fromNMSEntity(entity.getEntity());
    }

    public static NonstaticLocation[] fromBukkitEntities(Entity[] entities) {
        NonstaticLocation[] result = new NonstaticLocation[entities.length];
        for(int i = 0; i < entities.length; i++) {
            result[i] = fromBukkitEntity(entities[i]);
        }
        return result;
    }

    public static NonstaticLocation[] fromNMSEntities(net.minecraft.server.v1_8_R3.Entity[] entities) {
        NonstaticLocation[] result = new NonstaticLocation[entities.length];
        for(int i = 0; i < entities.length; i++) {
            result[i] = fromNMSEntity(entities[i]);
        }
        return result;
    }

    public static NonstaticLocation[] fromPlayers(Player[] players) {
        NonstaticLocation[] result = new NonstaticLocation[players.length];
        for(int i = 0; i < players.length; i++) {
            result[i] = fromPlayer(players[i]);
        }
        return result;
    }

    public static NonstaticLocation[] fromBaseEntities(EntityBase[] entities) {
        NonstaticLocation[] result = new NonstaticLocation[entities.length];
        for(int i = 0; i < entities.length; i++) {
            result[i] = fromBaseEntity(entities[i]);
        }
        return result;
    }

    private NonstaticLocation(Entity subject) {
        this.subject = subject;
        location = new AtomicReference<>(subject.getLocation());
    }

    /**
     * Forces the current thread (if it's NOT the main thread) to wait for
     * the location represented by this object to be safely gotten. The
     * 'wait' for this process should always be a matter of mere
     * milliseconds and should be incredibly insignificant, unless
     * the server is overloaded or the hardware it's running on
     * is almost nonfunctionally poor.
     * @return
     */
    public Location getAndWaitForLoc() {
        if(!validate()) return null;
        // If run on main thread, eliminates the purpose of this class and the location can just be gotten
        if (Bukkit.isPrimaryThread()) return subject.getLocation();

        CountDownLatch wait = new CountDownLatch(1);
        updateLocation(wait);
        JavaUtil.safeLatchWait(wait);
        return location;
    }

    private void updateLocation(CountDownLatch wait) {
        MinecraftUtil.runOnMainThread(() -> {
            location = subject.getLocation();
            wait.countDown();
        });
    }

    private boolean validate() {
        if(!subject.isValid()) return false;
        if(subject.isDead()) return false;
        return true;
    }


}
