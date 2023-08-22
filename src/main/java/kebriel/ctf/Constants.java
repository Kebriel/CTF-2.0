package kebriel.ctf;

import kebriel.ctf.game.map.MapLocation;
import kebriel.ctf.util.JavaUtil;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    // Abilities
    public static final int ALCHEMIST_COOLDOWN = 30;
    public static final int BASTION_KNOCKBACK_REDUC = 100;
    public static final int BERSERK_DAMAGE_BOOST = 50;
    public static final int BERSERK_COOLDOWN = 3;
    public static final int BRACED_DAMAGE_REDUC = 50;
    public static final int CLEAN_STRIKE_DAMAGE_MULT = 30;
    public static final int EXPANDED_QUIVER_ARROW_COUNT = 5;
    public static final int FAR_SHOT_DAMAGE_MULT = 25;
    public static final int FAR_SHOT_DISTANCE_THRESHOLD = 15;
    public static final int FIREBRAND_FIRE_DURATION = 3;
    public static final int FLEET_FOOT_SPEED_MULT = 1;
    public static final int FLEET_FOOT_SPEED_DURATION = 10;
    public static final int FLEET_FOOT_JUMP_MULT = 1;
    public static final int FLEET_FOOT_JUMP_DURATION = 10;
    public static final int GLADIATOR_DAMAGE_REDUC = 5;
    public static final int GLADIATOR_RANGE = 8;
    public static final int IRONSKIN_RESISTANCE_MULT = 1;
    public static final int IRONSKIN_SLOW_MULT = 1;
    public static final int JUGGERNAUT_PROT_STRENGTH = 1;
    public static final int KEVLAR_DAMAGE_REDUC = 45;
    public static final int LEECH_HEAL_CHANCE = 20;
    public static final int LEECH_HEAL_AMOUNT = 2;
    public static final int LIFEBLOOD_EXTRA_HEALTH = 4;
    public static final int MASSACRE_DAMAGE_MULT = 5;
    public static final int MASSACRE_RANGE = 8;
    public static final int MENDING_REGEN_STRENGTH = 1;
    public static final int MENDING_REGEN_DURATION = 5;
    public static final int MENDING_THRESHOLD = 6;
    public static final int PANIC_SPEED_STRENGTH = 1;
    public static final int PANIC_SPEED_DURATION = 5;
    public static final int PANIC_THRESHOLD = 6;
    public static final int PARRY_DAMAGE_REDUC = 30;
    public static final int POINT_BLANK_KNOCKBACK = 30;
    public static final int POINT_BLANK_RANGE = 8;
    public static final int XP_MULT = 2;

    // Items
    public static final int ITEM_BANDAGE_HEAL_AMOUNT = 2;
    public static final int ITEM_BANDAGE_RESIST_STRENGTH = 1;
    public static final int ITEM_BANDAGE_RESIST_DURATION = 5;
    public static final int ITEM_BANDAGE_REGEN_STRENGTH = 1;
    public static final int ITEM_BANDAGE_REGEN_DURATION = 1;
    public static final int ITEM_BLOCKS_SPAWN_AMOUNT = 16;
    public static final int ITEM_BLOCKS_KILL_REWARD = 5;
    public static final int ITEM_BLOCKS_DURATION = 3;
    public static final int ITEM_ROD_DAMAGE_MULT = 20;
    public static final int ITEM_KB_STICK_STRENGTH = 2;
    public static final int ITEM_SNOWBALL_SLOW_STRENGTH = 1;
    public static final int ITEM_SNOWBALL_SLOW_DURATION = 3;

    // Perks
    public static final int PERK_HASTE_SPEED_STRENGTH = 1;
    public static final int PERK_HASTE_SPEED_DURATION = 30;
    public static final int ROYALTY_EXTRA_XP_KA = 1;
    public static final int ROYALTY_EXTRA_GOLD_KA = 1;
    public static final int ROYALTY_EXTRA_XP_MISC = 10;
    public static final int ROYALTY_EXTRA_GOLD_MISC = 10;

    // Game
    public static String HUB_NAME = "hub";
    public static final MapLocation HUB = MapLocation.getHub();
    public static final int MINIMUM_PLAYERS_TO_START = 2;
    public static final int GRACE_PERIOD_DURATION = 30;
    public static final int GAME_DURATION_SECONDS = 1200;
    public static final int DEATHMATCH_DURATION_SECONDS = 10;
    public static final int ENDGAME_DURATION_SECONDS = 10;
    public static final int FLAG_INTERACTION_RADIUS = 2;
    public static final int QUEUE_OFFSET = 1;

    // Base stats
    public static final int BASE_KILL_GOLD = 5;
    public static final int BASE_KILL_XP = 5;
    public static final int BASE_ASSIST_GOLD = 1;
    public static final int BASE_ASSIST_XP = 1;
    public static final int BASE_CAPTURE_GOLD = 50;
    public static final int BASE_CAPTURE_XP = 50;
    public static final int BASE_RETURN_GOLD = 15;
    public static final int BASE_RETURN_XP = 15;
    public static final int BASE_FLAGHOLDER_MULT = 2;
    public static final int BASE_WIN_GOLD = 80;
    public static final int BASE_WIN_XP = 80;
    public static final int BASE_PARTICIPATION_GOLD = 40;
    public static final int BASE_PARTICIPATION_XP = 40;

    // Cosmetics
    public static final int COSMETIC_CROWN_LEVEL_COST = 500;
    public static final int COSMETIC_DEMONIC_KILL_COST = 10000;
    public static final int COSMETIC_LOSS_REQ = 5000;

    // Player
    public static final int BASE_LEVEL_COST = 50;
    public static final double LEVEL_COST_MULT = 0.7;

    // Internal
    public static final int SKIN_QUERY_FAILURE_WAIT = 20;

    /*
     * The max distance, in chunks, at which packets should still be sent to players
     */
    public static final int PACKET_PROXIMITY = Bukkit.getViewDistance();

    /*
     * How often (in seconds) all players should have their data saved to the database,
     * purposed to reduce the consequence severity of unexpected server crashes. Technically,
     * the ideal would be to autosave roughly every second, and so this should be set
     * as high as the relevant hardware can handle.
     */
    public static final long AUTOSAVE_INTERVAL = 120;

    /*
     * After a player leaves the server, their profile will be cleared from the cache
     * after this many seconds. A profile is not saved when it's reaped (it's saved when they
     * leave), this is to avoid unnecessary resource use and concurrency issues. Note
     * that once their profile is reaped, if they join this same server again even
     * a minute later, their profile will need to be reloaded. If it's not, a quickload
     * will be performed to ensure that their stats are up-to-date, but this will
     * be more resource-efficient than a regular load. This feature is largely intended
     * to reduce unnecessary resource consumption in instances where a player quits
     * and immediately rejoins for whatever reason, such as restarting their client
     */
    public static final long PROFILE_REAP_DELAY = 120;

    private class TypeDefaultMappings {

        private static final Map<Class<?>, Object> mappings = new HashMap<>();

        static {
            mappings.put(Integer.class, 0);
            mappings.put(Double.class, 0.0);
            mappings.put(Float.class, 0);
            mappings.put(Long.class, 0);
            mappings.put(Short.class, 0);
            mappings.put(String.class, " ");
            mappings.put(Character.class, ' ');
            mappings.put(Boolean.class, false);
        }

        public static Object getDefault(Class<?> type) {
            return mappings.getOrDefault(type, type.isPrimitive() ? 0 : null);
        }

        public static Object getDefault(Object obj) {
            return getDefault(JavaUtil.getRawType(obj));
        }
    }
}
