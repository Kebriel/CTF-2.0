package kebriel.ctf.player;

import kebriel.ctf.util.JavaUtil;

import java.util.ArrayList;
import java.util.List;

public enum Stat {

    GOLD(0, StatType.NUMERIC), KILLS(0, StatType.NUMERIC), DEATHS(0, StatType.NUMERIC), ASSISTS(0, StatType.NUMERIC), XP(0, StatType.NUMERIC), GAMES(0, StatType.NUMERIC),
    WINS(0, StatType.NUMERIC), LOSSES(0, StatType.NUMERIC), LEVEL(1, StatType.NUMERIC), FLAG_CARRIER_KILLS(0, StatType.NUMERIC), CHALLENEGES_COMPLETED(0, StatType.NUMERIC), FLAGS_CAPTURED(0, StatType.NUMERIC), SECONDS_PLAYED((long) 0, StatType.NUMERIC),
    UNLOCKED_SLOT_ABILITY3(false, StatType.UNLOCKABLE_SLOT), UNLOCKED_SLOT_ABILITY4(false, StatType.UNLOCKABLE_SLOT), UNLOCKED_SLOT_ITEM1(false, StatType.UNLOCKABLE_SLOT), UNLOCKED_SLOT_PERK1(false, StatType.UNLOCKABLE_SLOT), UNLOCKED_SLOT_PERK2(false, StatType.UNLOCKABLE_SLOT),
    SELECTED_ABILITY1("", StatType.SELECTED), SELECTED_ABILITY2("", StatType.SELECTED), SELECTED_ABILITY3("", StatType.SELECTED), SELECTED_ABILITY4("", StatType.SELECTED), SELECTED_EXTRA_ITEM1("", StatType.SELECTED),
    SELECTED_PERK1("", StatType.SELECTED), SELECTED_PERK2("", StatType.SELECTED), UNLOCKED_GLADIATOR(false, StatType.UNLOCKABLE_ABILITY), UNLOCKED_MASSACRE(false, StatType.UNLOCKABLE_ABILITY), UNLOCKED_BASTION(false, StatType.UNLOCKABLE_ABILITY), UNLOCKED_KEVLAR(false, StatType.UNLOCKABLE_ABILITY),
    UNLOCKED_JUGGERNAUT(false, StatType.UNLOCKABLE_ABILITY), UNLOCKED_XP_ABILITY(false, StatType.UNLOCKABLE_ABILITY), UNLOCKED_FLEETFOOTED(false, StatType.UNLOCKABLE_ABILITY), UNLOCKED_IRONSKIN(false, StatType.UNLOCKABLE_ABILITY), UNLOCKED_FARSHOT(false, StatType.UNLOCKABLE_ABILITY),
    UNLOCKED_LIFEBLOOD(false, StatType.UNLOCKABLE_ABILITY), UNLOCKED_BRACED(false, StatType.UNLOCKABLE_ABILITY), UNLOCKED_ALCHEMIST(false, StatType.UNLOCKABLE_ABILITY), UNLOCKED_BERSERK(false, StatType.UNLOCKABLE_ABILITY), UNLOCKED_BLANK(false, StatType.UNLOCKABLE_ABILITY),
    UNLOCKED_LEECH(false, StatType.UNLOCKABLE_ABILITY), UNLOCKED_FIREBRAND(false, StatType.UNLOCKABLE_ABILITY), UNLOCKED_ITEM_ROD(false, StatType.UNLOCKABLE_ABILITY), UNLOCKED_ITEM_SNOW(false, StatType.UNLOCKABLE_ABILITY), UNLOCKED_ITEM_GAPPLE(false, StatType.UNLOCKABLE_ABILITY),
    UNLOCKED_ITEM_STICK(false, StatType.UNLOCKABLE_ABILITY), UNLOCKED_ITEM_BANDAGE(false, StatType.UNLOCKABLE_ABILITY), UNLOCKED_ITEM_PEARL(false, StatType.UNLOCKABLE_ABILITY), UNLOCKED_ITEM_BLOCKS(false, StatType.UNLOCKABLE_ABILITY),
    UNLOCKED_PERK_UNTRACKABLE(false, StatType.UNLOCKABLE_ABILITY), UNLOCKED_PERK_HASTE(false, StatType.UNLOCKABLE_ABILITY), UNLOCKED_PERK_REINFORCE(false, StatType.UNLOCKABLE_ABILITY), UNLOCKED_PERK_ROYALTY(false, StatType.UNLOCKABLE_ABILITY),
    SELECTED_AURA("", StatType.SELECTED), SELECTED_TRAIL("", StatType.SELECTED), SELECTED_KILL_EFFECT("", StatType.SELECTED), SELECTED_KILL_MESSAGE("", StatType.SELECTED),

    //Values not saved to database, included for convenience and continuity
    GAME_DEATHS(0, StatType.TEMP), GAME_KILLS(0, StatType.TEMP), GAME_GOLD(0, StatType.TEMP), GAME_CAPTURES(0, StatType.TEMP);

    private final Object defaultValue;
    private final StatType type;

    Stat(Object defaultValue, StatType type) {
        this.defaultValue = defaultValue;
        this.type = type;
    }

    /**
     * Allows access to this StatType's default value, that being the value that
     * it is set to within the database when first created. Also defines what
     * type of data that this stat saves
     * @return returns the default value as an object
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * @return returns the raw type of this StatType's default value
     */
    public <T> Class<T> getRawType() {
        return (Class<T>) JavaUtil.getRawType(defaultValue);
    }

    public static Stat[] getPermStats() {
        List<Stat> stats = new ArrayList<>();
        for(Stat s : values()) {
            if(!s.isTemporary()) stats.add(s);
        }
        return (Stat[]) stats.toArray();
    }

    public StatType getStatType() {
        return type;
    }

    public String getID() {
        return this.toString();
    }

    public boolean isTemporary() {
        return type == StatType.TEMP;
    }

    public enum StatType {
        NUMERIC, UNLOCKABLE_ABILITY, UNLOCKABLE_SLOT, SELECTED, TEMP
    }
}
