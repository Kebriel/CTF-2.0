package kebriel.ctf.entity.components;

import kebriel.ctf.internal.nms.GamePacket.SpawnEntity;
import kebriel.ctf.internal.nms.PacketRegistry;
import kebriel.ctf.util.MinecraftUtil;
import kebriel.ctf.util.ReflectionUtil;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class EntityWrapper<T extends Entity> implements Cloneable {

    private static final Map<Integer, EntityWrapper<?>> packetEntityCache = new ConcurrentHashMap<>();

    private final World world;
    private final Class<T> entityType;
    private final T entity;
    private final SpawnEntity spawnRender;
    private Location loc;
    private volatile boolean spawned;
    private final int id;
    private final boolean renderOnly;

    {
        world = MinecraftUtil.getNMSWorld();
    }

    public static EntityWrapper<?> getPacketEntity(int entityID) {
        return packetEntityCache.get(entityID);
    }

    public EntityWrapper(Class<T> entityType, Location loc) {
        this.entityType = entityType;
        this.loc = loc;
        entity = instantiateEntity();
        id = entity.getId();
        entity.setPosition(loc.getX(), loc.getY(), loc.getZ());

        renderOnly = getClass().isAnnotationPresent(RenderOnly.class);

        spawnRender = new SpawnEntity(entity);
    }

    public T getEntity() {
        return entity;
    }

    public int getEntityID() {
        return getEntity().getId();
    }

    public void setEquipment(EquipmentSlots slot, ItemStack equipment) {
        int slotNum = switch (slot) {
            case HAND -> 0;
            case HELMET -> 1;
            case CHESTPLATE -> 2;
            case LEGGINGS -> 3;
            case BOOTS -> 4;
        };
        getEntity().setEquipment(slotNum, equipment);
    }

    protected void rawSpawn() {
        world.addEntity(entity);
        spawned = true;
    }

    protected void rawDestroy() {
        entity.die();
        world.removeEntity(entity);
        spawned = false;
    }

    public void spawn() {
        if(renderOnly) {
            renderEntity();
            return;
        }

        if(spawned || packetEntityCache.containsKey(entity.getId()))
            return;
        MinecraftUtil.runOnMainThread(this::rawSpawn);
    }

    public void destroy() {
        if(renderOnly) {
            derenderEntity();
            return;
        }

        if (!spawned) return;
        MinecraftUtil.runOnMainThread(this::rawDestroy);
    }

    public void refreshEntity() {
        if(spawned) {
            MinecraftUtil.runOnMainThread(() -> {
                rawDestroy();
                rawSpawn();
            });
        }else if(PacketRegistry.isRendered(spawnRender)){
            PacketRegistry.refreshThis(spawnRender);
        }
    }

    public void renderEntity() {
        if(spawned || packetEntityCache.containsKey(id)) return;
        spawnRender.render();
        packetEntityCache.put(id, this);
    }

    public void derenderEntity() {
        if(spawned || !packetEntityCache.containsKey(id)) return;
        spawnRender.derender();
        packetEntityCache.remove(id);
    }

    public SpawnEntity getRender() {
        return spawnRender;
    }

    public Location getLocation() {
        return entity.getBukkitEntity().getLocation();
    }

    public Class<? extends Entity> getEntityBaseType() {
        return entityType;
    }

    public boolean isSpawned() {
        return spawned;
    }

    public boolean isRendered() {
        return packetEntityCache.containsKey(id);
    }

    public void setLocation(Location newLoc) {
        if(spawned || PacketRegistry.isRendered(spawnRender))
            return;
        loc = newLoc;
        entity.setPosition(loc.getX(), loc.getY(), loc.getZ());
    }

    private T instantiateEntity() {
        try {
            Constructor<T> constructor = entityType.getConstructor();
            return constructor.newInstance(MinecraftUtil.getNMSWorld());
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadFromNBT(NBTTagCompound nbt) {
        entity.f(nbt);
    }

    private void writeToNBT(NBTTagCompound nbt) {
        entity.e(nbt);
    }

    @Override
    public EntityWrapper<T> clone() {
        try {
            EntityWrapper<T> clone = (EntityWrapper<T>) super.clone();
            NBTTagCompound nbt = new NBTTagCompound();
            writeToNBT(nbt);
            // Writes data specific to certain entity classes
            ReflectionUtil.invokeVoidMethod(entity, "b", nbt);
            clone.loadFromNBT(nbt);
            return clone;
        } catch(CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof EntityWrapper<?> en)
            return en.getEntityID() == getEntityID();
        return false;
    }
}
