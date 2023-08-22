package kebriel.ctf.player;

import io.netty.util.internal.ConcurrentSet;
import kebriel.ctf.Constants;
import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.CooldownAbility;
import kebriel.ctf.ability.components.PassiveAbility;
import kebriel.ctf.ability.components.SpawnAbility;
import kebriel.ctf.event.async.AsyncPlayerAssistEvent;
import kebriel.ctf.event.async.AsyncPlayerDeathEvent;
import kebriel.ctf.event.async.AsyncPlayerKillEvent;
import kebriel.ctf.event.async.AsyncPlayerKillEvent.PlayerKillType;
import kebriel.ctf.event.async.AsyncPlayerMoveBlockEvent;
import kebriel.ctf.event.async.AsyncPlayerRespawnEvent;
import kebriel.ctf.event.async.components.CTFEvent;
import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.event.reaction.Reactor;
import kebriel.ctf.event.reaction.ReactorPersistence;
import kebriel.ctf.event.reaction.ThreadControl;
import kebriel.ctf.game.flag.Flag;
import kebriel.ctf.game.flag.Flag.FlagStatus;
import kebriel.ctf.game.Game;
import kebriel.ctf.game.Team;
import kebriel.ctf.game.map.MapLocation;
import kebriel.ctf.game.map.MapLocation.LocationType;
import kebriel.ctf.game.map.GameMaps;
import kebriel.ctf.game.component.WrappedDamage;
import kebriel.ctf.internal.concurrent.AsyncExecutor;
import kebriel.ctf.internal.nms.GamePacket.AlterPlayerNametag;
import kebriel.ctf.internal.player.GameMessage;
import kebriel.ctf.internal.player.title.GameTitle;
import kebriel.ctf.player.item.InventoryProfile;
import kebriel.ctf.player.item.InventoryProfile.InventoryProfileType;
import kebriel.ctf.util.MinecraftUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Reactor(persistence = ReactorPersistence.IMPERSISTENT)
public class PlayerState implements EventReactor {

    /**
     * Utility class for players, separate from CTFPlayer, that's purely
     * for game-level functions and ephemeral data
     */

    private final Map<CooldownAbility, Integer> cooldowns;
    private final Map<Ability, Integer> triggeredEffects;
    private volatile boolean damagedThisLife;
    private volatile boolean dead;
    private volatile boolean berserked;
    private final Map<CTFPlayer, WrappedDamage> lastHitters;
    private volatile Flag heldFlag;
    private final Player p;
    private final CTFPlayer player;
    private final FlagTracker trackerInstance;
    private final Game game;
    private AlterPlayerNametag renderPacket;

    private final AtomicInteger deathTimer;

    private final List<InventoryProfile> inventories;
    private final InventoryProfile LOBBY_INVENTORY = new InventoryProfile(this, InventoryProfileType.LOBBY);
    private final InventoryProfile GAME_INVENTORY = new InventoryProfile(this, InventoryProfileType.GAME);
    private volatile InventoryProfile activeInventory;

    {
        inventories = new ArrayList<>();
        inventories.add(LOBBY_INVENTORY);
        inventories.add(GAME_INVENTORY);
        cooldowns = new ConcurrentHashMap<>();
        triggeredEffects = new ConcurrentHashMap<>();
        lastHitters = new ConcurrentHashMap<>(0);
        deathTimer = new AtomicInteger();
        game = Game.get();
    }

    public PlayerState(CTFPlayer player) {
        p = player.getBukkitPlayer();
        this.player = player;
        trackerInstance = new FlagTracker(player);

        EventReaction.register(this);
    }

    public void clearInventory() {
        MinecraftUtil.ensureSync();

        PlayerInventory inv = p.getInventory();
        inv.clear();
        inv.setArmorContents(null);
    }

    public void reset() {
        MinecraftUtil.doSyncIfNot(() -> {
            clearInventory();
            p.setGameMode(dead ? GameMode.SPECTATOR : GameMode.SURVIVAL);

            if(!game.isPlaying())
                p.setMaxHealth(20.0);

            terminatePassives();
            p.setHealth(p.getMaxHealth());
            p.setFallDistance(0.0f);
            p.setFireTicks(0);
            clearPotionEffects();

            damagedThisLife = false;
        });
    }

    public void spawn() {
        updateInventory();
        applySpawnAbilities();

        teleportPlayer(GameMaps.getCurrent().getLocation(player.getTeam().getTeamColor(), LocationType.SPAWN));
    }

    private void respawn() {
        dead = false;
        spawn();
    }

    public void join() {
        player.setScoreboard(game.getCurrentBoard());

        if(!game.isPlaying()) {
            reset();
            teleportPlayer(Constants.HUB);
            return;
        }

        if(dead)
            return;

        spawn();
    }

    public synchronized void updateInventory() {
        AsyncExecutor.doAsyncIfNot(() -> {
            for(InventoryProfile inv : inventories)
                if(inv.shouldBeActive() && activeInventory != inv) {
                    activeInventory = inv;
                    activeInventory.prepare();
                    activeInventory.applyToPlayer();
                }

        });
    }

    private void applySpawnAbilities() {
        for(Ability a : player.getSelected()) {
            if(a instanceof PassiveAbility passive)
                passive.start(player);
            else if(a instanceof SpawnAbility spawn)
                spawn.apply(player);
        }
    }

    private void terminatePassives() {
        for(Ability a : player.getSelected()) {
            if(a instanceof PassiveAbility passive)
                passive.terminate(player);
        }
    }

    public void die(AsyncPlayerDeathEvent event) {
        dead = true;
        handleAssist(event);
        reset();
        if(isHoldingFlag())
            dropFlag();

        deathTimer.set(6);
        new AsyncExecutor(task -> {
            player.sendTitle(GameTitle.GAME_DEATH_SCREEN.get());
            if(deathTimer.get() == 0) {
                respawn();
                CTFEvent.fireEvent(new AsyncPlayerRespawnEvent(player));
                task.terminate(true);
            }
            deathTimer.decrementAndGet();
        }).doRepeating(0, 1, TimeUnit.SECONDS);
    }

    private void handleAssist(AsyncPlayerDeathEvent event) {
        if(!lastHitters.isEmpty()) {
            // Handle kill for most recent hitter, registers even if they're offline
            CTFPlayer killer = getMostRecentHitter();

            PlayerKillType cause;
            DamageCause reason = event.getReason();
            switch(getLastHit(killer).getCause()) {
                case ENTITY_ATTACK -> cause = switch (reason) {
                    case FALL, VOID -> PlayerKillType.KNOCKBACK_MELEE;
                    default -> PlayerKillType.MELEE_ATTACK;
                };
                case PROJECTILE -> cause = switch (reason) {
                    case FALL, VOID -> PlayerKillType.KNOCKBACK_PROJECTILE;
                    default -> PlayerKillType.ARROW;
                };
                case FIRE_TICK -> cause = PlayerKillType.TICK_FIRE;
                case WITHER -> cause = PlayerKillType.TICK_WITHER;
                case CUSTOM -> cause = PlayerKillType.GENERIC_KILL;
                default -> cause = PlayerKillType.MELEE_ATTACK;
            }
            lastHitters.remove(killer);
            CTFEvent.fireEvent(new AsyncPlayerKillEvent(killer, player, event.getFinalDamage(), cause));

            // Everyone else in the list gets an assist, only if they're online
            for(CTFPlayer player : lastHitters.keySet())
                if(player.isOnline())
                    CTFEvent.fireEvent(new AsyncPlayerAssistEvent(player, this.player, cause, getLastHit(player)));

            clearLastHitters();
        }else{ // No kills or assists, player just dies
            GameMessage.GAME_GENERIC_DEATH.sendGlobalPrefixed();
        }

        player.incrementStat(Stat.DEATHS);
    }

    public void teleportPlayer(MapLocation loc) {
        MinecraftUtil.doSyncIfNot(() -> p.teleport(loc.getBukkitLocation()));
    }

    public void putOnCooldown(CooldownAbility ab, int duration) {
        if(isOnCooldown(ab))
            return;

        cooldowns.put(ab, duration);
        AsyncExecutor.doTimer(() -> {
            int remainingDuration = cooldowns.get(ab) - 1;
            if(remainingDuration == 0) {
                cooldowns.remove(ab);
            }else{
                cooldowns.put(ab, remainingDuration);
            }
        }, 1);
    }

    public boolean isOnCooldown(CooldownAbility ab) {
        return cooldowns.containsKey(ab);
    }

    public void triggerEffect(Ability ab, int duration) {
        if(hasEffectTriggered(ab))
            return;

        triggeredEffects.put(ab, duration);
        AsyncExecutor.doTimer(() -> {
            int remainingDuration = cooldowns.get(ab) - 1;
            if(remainingDuration == 0) {
                triggeredEffects.remove(ab);
            }else{
                triggeredEffects.put(ab, remainingDuration);
            }
        }, 1);
    }

    public boolean hasEffectTriggered(Ability ab) {
        return triggeredEffects.containsKey(ab);
    }

    public void clearPotionEffects() {
        for(PotionEffect pot : p.getActivePotionEffects()) p.removePotionEffect(pot.getType());
    }
    
    public Player getPlayer() {
        return p;
    }

    public FlagTracker getTracker() {
        return trackerInstance;
    }

    public void registerDamage() {
        damagedThisLife = true;
    }

    public boolean hasTakenDamage() {
        return damagedThisLife;
    }

    public boolean isDead() {
        return dead;
    }

    public boolean isLastHitter(CTFPlayer hitter) {
        return lastHitters.containsKey(hitter);
    }

    public void clearLastHitters() {
        lastHitters.clear();
    }

    public void addHit(CTFPlayer hitter, double damage, DamageCause cause) {
        if(isLastHitter(hitter)) {
            lastHitters.put(hitter, lastHitters.get(hitter).add(damage, cause));
        }else{
            lastHitters.put(hitter, new WrappedDamage(damage, cause));
        }
    }

    public WrappedDamage getLastHit(CTFPlayer attacker) {
        return lastHitters.get(attacker);
    }

    public List<CTFPlayer> getLastHitters() {
        List<CTFPlayer> hitters = new ArrayList<>();
        for(CTFPlayer p : lastHitters.keySet()) {
            hitters.add(p);
        }
        return hitters;
    }

    public CTFPlayer getMostRecentHitter() {
        return getLastHitters().get(getLastHitters().size()-1);
    }

    public boolean isHoldingFlag() {
        return heldFlag != null;
    }

    public Flag getHeldFlag() {
        return heldFlag;
    }

    public void pickupFlag(Flag flag) {
        flag.pickupFlag(player);
        heldFlag = flag;
    }

    public void dropFlag() {
        if(heldFlag != null && heldFlag.getStatus() == FlagStatus.TAKEN) {
            heldFlag.dropFlag();
            heldFlag = null;
        }
    }

    private void captureFlag() {
        heldFlag.captureFlag();
        heldFlag = null;
    }

    private void returnFlag() {
        heldFlag.returnFlag();
        heldFlag = null;
    }

    public void eraseFlag() {
        heldFlag = null;
    }

    public void addUntrackableRender(AlterPlayerNametag packet) {
        if(renderPacket == null)
            this.renderPacket = packet;
    }

    public void clearUntrackableRender() {
        renderPacket.derender();
        renderPacket = null;
    }

    /*
     * ### Event Reaction ###
     */

    /**
     * Handle flags either being returned or captured by players being
     * within vicinity of the appropriate spawn
     */
    @EventReact(allowedWhen = GameStage.PLAYING)
    public void onPlayerMove(AsyncPlayerMoveBlockEvent event) {
        if(!event.getPlayer().equals(player))
            return;

        if(!isHoldingFlag())
            return;

        Team t = player.getTeam();
        Location playersSpawn = GameMaps.getCurrent().getLocation(t.getTeamColor(), LocationType.SPAWN).getBukkitLocation();

        if(playersSpawn.distance(event.getLocTo()) > Constants.FLAG_INTERACTION_RADIUS)
            return;

        if(t.equals(heldFlag.getTeam())) { // Return your own flag
            returnFlag();
            return;
        }

        captureFlag();
    }

    @EventReact(thread = ThreadControl.ASYNC)
    public void onInteract(PlayerInteractEvent event) {
        if(!event.getPlayer().equals(player.getBukkitPlayer()))
            return;

        activeInventory.passInteract(event);
    }

    /**
     * Fetches whether this player is onGround without using deprecated methods
     */
    public boolean isOnGround() {
        return player.getNMSPlayer().onGround;
    }

    public int getDeathTimer() {
        return deathTimer.get();
    }

    public CTFPlayer getCTFPlayer() {
        return player;
    }

    public PlayerInventory getInventory() {
        return p.getInventory();
    }

    public InventoryProfile getInventoryProfile(InventoryProfileType type) {
        for(InventoryProfile prof : inventories)
            if(prof.getType() == type)
                return prof;
        return null;
    }

    public InventoryProfile getActiveInventory() {
        return activeInventory;
    }

    public MapLocation getSpawn() {
        return GameMaps.getCurrent().getLocation(player.getTeam().getTeamColor(), LocationType.SPAWN);
    }

    public Location getLocation() {
        return p.getLocation();
    }

    public String getName() {
        return p.getName();
    }
}
