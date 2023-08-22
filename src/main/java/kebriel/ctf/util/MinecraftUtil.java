package kebriel.ctf.util;

import kebriel.ctf.CTFMain;
import kebriel.ctf.Constants;
import kebriel.ctf.entity.components.EntityWrapper;
import kebriel.ctf.game.map.MapLocation;
import kebriel.ctf.internal.nms.GamePacket;
import kebriel.ctf.internal.sql.SQLManager.WrappedData;
import kebriel.ctf.player.CTFPlayer;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTypes;
import net.minecraft.server.v1_8_R3.NBTBase;
import net.minecraft.server.v1_8_R3.NBTTagByte;
import net.minecraft.server.v1_8_R3.NBTTagByteArray;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagDouble;
import net.minecraft.server.v1_8_R3.NBTTagFloat;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagIntArray;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagLong;
import net.minecraft.server.v1_8_R3.NBTTagShort;
import net.minecraft.server.v1_8_R3.NBTTagString;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class that I wrote while creating this plugin, mostly
 * to ease processes such as thread checking, NMS-Bukkit conversion,
 * and more complex/specific work with Locations/Blocks
 */

public class MinecraftUtil {

    public static void runOnMainThread(Runnable task) {
        Bukkit.getScheduler().runTask(CTFMain.instance, task);
    }

    public static void runBukkitAsync(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(CTFMain.instance, task);
    }

    public static void doSyncIfNot(Runnable task) {
        if(!Bukkit.isPrimaryThread()) {
            runOnMainThread(task);
            return;
        }

        task.run();
    }

    /**
     * Checks if the current thread is the main thread, and throws an error if so
     */
    public static void ensureAsync() {
        if(isMainThread())
            throw new IllegalStateException("This should not be run on the main thread");
    }

    public static void ensureSync() {
        if(!isMainThread())
            throw new IllegalStateException("This should be run on the main thread");
    }

    public static boolean isMainThread() {
        return Bukkit.isPrimaryThread();
    }

    public static int getEntityTypeID(Entity entity) {
        return EntityTypes.a(entity);
    }

    public static int getChunkDistance(Location from, Location to) {
        int dx = Math.abs(from.getBlockX() - to.getBlockX());
        int dz = Math.abs(from.getBlockZ() - to.getBlockZ());

        int chunkDistanceX = dx / 16;
        int chunkDistanceZ = dz / 16;

        return Math.max(chunkDistanceX, chunkDistanceZ);
    }

    public static EntityPlayer convertBukkitPlayer(Player player) {
        return ((CraftPlayer)player).getHandle();
    }

    public static Entity convertBukkitEntity(org.bukkit.entity.Entity entity) {
        return ((CraftEntity)entity).getHandle();
    }

    public static World convertBukkitWorld(org.bukkit.World world) {
        return ((CraftWorld)world).getHandle();
    }

    public static Location[] locsFromEntities(Entity[] entities) {
        Location[] locs = new Location[entities.length];
        for(int i = 0; i < entities.length; i++) {
            locs[i] = entities[i].getBukkitEntity().getLocation();
        }
        return locs;
    }

    public static Location[] locsFromBukkitEntities(org.bukkit.entity.Entity[] entities) {
        Location[] locs = new Location[entities.length];
        for(int i = 0; i < entities.length; i++) {
            locs[i] = entities[i].getLocation();
        }
        return locs;
    }

    public static Location[] locsFromPlayers(Player[] players) {
        Location[] locs = new Location[players.length];
        for(int i = 0; i < players.length; i++) {
            locs[i] = players[i].getLocation();
        }
        return locs;
    }

    public static Location[] locsFromBaseEntities(EntityWrapper<?>[] entities) {
        Location[] locs = new Location[entities.length];
        for(int i = 0; i < entities.length; i++) {
            locs[i] = entities[i].getEntity().getBukkitEntity().getLocation();
        }
        return locs;
    }

    public static boolean packetIsInProximity(CTFPlayer receiver, GamePacket packet) {
        return getChunkDistance(receiver.getLocation(), packet.getNonstaticTarget().get()) <= Constants.PACKET_PROXIMITY;
    }

    public static World getNMSWorld() {
        return ((CraftWorld)getBukkitWorld()).getHandle();
    }

    public static org.bukkit.World getBukkitWorld() {
        return Bukkit.getWorlds().get(0);
    }

    public static boolean areChunksEqual(org.bukkit.Chunk a, org.bukkit.Chunk b) {
        return a.getX() == b.getX() && a.getZ() == b.getZ();
    }

    public static Entity getEntity(int entityID) {
        try {
            return getNMSWorld().a(entityID);
        }catch(NullPointerException ex) {
            return null;
        }
    }

    public static boolean isSolidBlock(Location loc) {
        return loc.getBlock().getType().isSolid();
    }

    public static boolean isSolidBlockAbove(Location loc) {
        return isSolidBlock(loc.clone().add(0, 1, 0));
    }

    public static boolean isSolidBlockBelow(Location loc) {
        return isSolidBlock(loc.clone().subtract(0, 1, 0));
    }

    public static Location findNextOpenSpace(Location loc, int searchDistance, int size, char axis, char operand) {
        if((operand != '+' && operand != '-') || (axis != 'x' && axis != 'y' && axis != 'z')) return loc;
        Vector mod = new Vector(axis == 'x' ? 1 : 0, axis == 'y' ? 1 : 0, axis == 'z' ? 1 : 0);
        mod.multiply(operand == '+' ? 1 : -1);
        loc = loc.clone();
        for(int i = searchDistance; i > 0; i--) {
            boolean success = true;
            for(int j = size; j > 0; j--) {
                if(MinecraftUtil.isSolidBlock(loc)) {
                    success = false;
                    break;
                }
                loc.add(mod);
            }
            if(success) return loc;
            searchDistance-=size;
        }
        return null; // No solid block was found
    }

    public static List<WrappedData> wrapLocations(Collection<MapLocation> locs) {
        List<WrappedData> data = new ArrayList<>();
        for(MapLocation loc : locs) {
            double[] coords = loc.fillAndGetCoordArray();

            double[] defaults = new double[3];
            Arrays.fill(defaults, 0.0);

            for(int i = 0; i < coords.length; i++)
                data.add(new WrappedData(loc.formatSingleField(i), coords[i], defaults[i]));
        }
        return data;
    }

    public static List<NBTTagCompound> unpackNBTAttributes(ItemStack from) {
        NBTTagCompound itemNBT = CraftItemStack.asNMSCopy(from).getTag();
        if(itemNBT == null)
            return null;

        NBTTagList tags = itemNBT.getList("AttributeModifiers", 10);

        List<NBTTagCompound> result = new ArrayList<>();
        for(int i = 0; i < tags.size(); i++)
            result.add(tags.get(i));

        return result;
    }

    public static Map<String, Object> unpackNBTCompound(NBTTagCompound nbt) {
        Map<String, Object> result = new HashMap<>();

        result.put("AttributeName", getValueFromNBT(nbt.get("AttributeName")));
        result.put("Name", getValueFromNBT(nbt.get("Name")));
        result.put("Amount", getValueFromNBT(nbt.get("Amount")));
        result.put("Operation", getValueFromNBT(nbt.get("Operation")));
        result.put("UUIDLeast", getValueFromNBT(nbt.get("UUIDLeast")));
        result.put("UUIDMost", getValueFromNBT(nbt.get("UUIDMost")));
        return result;
    }

    public static Object getValueFromNBT(NBTBase nbt) {
        Object result = null;
        if(nbt instanceof NBTTagByte byt)
            result = byt.f();
        else if(nbt instanceof NBTTagByteArray bytArr)
            bytArr.c();
        else if(nbt instanceof NBTTagDouble dub)
            result = dub.g();
        else if(nbt instanceof NBTTagFloat flo)
            result = flo.h();
        else if(nbt instanceof NBTTagInt i)
            result = i.d();
        else if(nbt instanceof NBTTagIntArray intArr)
            result = intArr.c();
        else if(nbt instanceof NBTTagLong l)
            result = l.c();
        else if(nbt instanceof NBTTagShort sho)
            result = sho.e();
        else if(nbt instanceof NBTTagString str)
            result = str.a_();
        return result;
    }

    public static Location getLocRelative(Location relativeTo, double relX, double relY, double relZ, double yaw) {
        yaw = Math.toRadians(yaw);
        return new Location(relativeTo.getWorld(), relativeTo.getX() + relX * Math.cos(yaw) + relZ * -Math.sin(yaw),
                relativeTo.getY() + relY, relativeTo.getZ() + relX * Math.sin(yaw) + relZ * Math.cos(yaw));
    }
}
