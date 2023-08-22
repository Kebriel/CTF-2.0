package kebriel.ctf.internal.player;

import org.bukkit.Sound;

public enum GameSound {

    MENU_YES(Sound.ORB_PICKUP, 1, 1),
    MENU_NO(Sound.VILLAGER_NO, 1, 1),
    MENU_SUCCESS(Sound.LEVEL_UP, 1, 1),
    LEVEL_UP(Sound.LEVEL_UP, 1, 2),
    ABILITY_FAIL(Sound.ENDERMAN_HIT, 1, 0.9f),
    TELEPORT(Sound.ENDERMAN_TELEPORT, 1, 1),
    TRACKER(Sound.CLICK, 1, 0.5f);

    private final Sound sound;
    private final float volume;
    private final float pitch;

    GameSound(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public Sound getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }
}
