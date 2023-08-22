package kebriel.ctf.display.cosmetic.component;

import kebriel.ctf.player.Stat;

public enum CosmeticType {
	
	AURA(Stat.SELECTED_AURA), TRAIL(Stat.SELECTED_TRAIL), KILL(Stat.SELECTED_KILL_EFFECT), MESSAGE(Stat.SELECTED_KILL_MESSAGE);

	private final Stat slot;

	CosmeticType(Stat slot) {
		this.slot = slot;
	}

	public Stat getSlot() {
		return slot;
	}

}
