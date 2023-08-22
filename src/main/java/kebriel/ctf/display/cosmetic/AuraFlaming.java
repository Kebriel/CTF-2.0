package kebriel.ctf.display.cosmetic;

import kebriel.ctf.display.cosmetic.component.CosmeticAura;
import kebriel.ctf.display.cosmetic.component.CosmeticType;
import kebriel.ctf.player.PlayerState;
import kebriel.ctf.player.Stat;
import kebriel.ctf.internal.nms.GamePacket;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.internal.player.text.Text;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.function.Function;

public class AuraFlaming implements CosmeticAura {

	@Override
	public Material getIcon() {
		return Material.MAGMA_CREAM;
	}

	@Override
	public String getID() {
		return "aura_flaming";
	}

	@Override
	public Text getDescription() {
		return Text.get().blue("Surround yourself in an aura")
				.newLine().blue("of living flame");
	}

	@Override
	public String getName() {
		return "Blazing";
	}

	@Override
	public CosmeticType getType() {
		return CosmeticType.AURA;
	}

	@Override
	public Function<CTFPlayer, Boolean> getUnlockCriteria() {
		return player -> player.getIsUnlocked(Stat.SELECTED_EXTRA_ITEM1)
				&& player.getIsUnlocked(Stat.UNLOCKED_SLOT_PERK1)
				&& player.getIsUnlocked(Stat.UNLOCKED_SLOT_PERK2);
	}

	@Override
	public Text getSubtext() {
		return Text.get().red("You must own all unlockable extra item and perk slots to use this");
	}

	@Override
	public BukkitRunnable getAura(PlayerState player) {
		return new BukkitRunnable() {
			Location oldLoc = player.getLocation();
			int delay = 60;
			final double[] angles = new double[3];
			final double[] yProgresses = new double[3];
			final boolean[] orientations = new boolean[3];
			boolean aura;
			int num = 0;

			@Override
			public void run() {
				if(player.isDead())
					return;

				Location playerLoc = player.getLocation();

				// If they're unmoving, apply aura
				if(playerLoc.getX() == oldLoc.getX() && playerLoc.getY() == oldLoc.getY() && playerLoc.getZ() == oldLoc.getZ()) {
					aura = true;
					if(delay == 30) { // Sets two more 'flame trails' to spawn at a delay
						num = 1;
					}else if(delay == 0) {
						num = 2;
					}

					for(int i = 0; i < 3; i++) {
						if(num >= i) {
							// Determines if this should progress up or down
							yProgresses[i]+=orientations[i] ? -0.08 : 0.08;

							if(yProgresses[i] >= 2) { // Reached highest allowed level, go downwards
								orientations[i] = true;
								yProgresses[i] = 2;
							}

							if(yProgresses[i] <= 0) { // Reached lowest allowed level, go upwards
								orientations[i] = false;
								yProgresses[i] = 0;
							}

							angles[i]+=0.1;

							// Scatters 'closeness' of the particle effect to the player
							double radius = 1.4 - (0.2*i);
							new GamePacket.PlayParticles(EnumParticle.FLAME, playerLoc.clone().add(radius * Math.sin(angles[i]), yProgresses[i], radius * Math.cos(angles[i])), 0, 0, 0, 0, 1, true)
									.send();
						}
					}
					delay--;
					return;
				}

				if(aura) {
					// If they moved (return wasn't triggered), reset aura
					Arrays.fill(angles, 0);
					Arrays.fill(yProgresses, 0);
					Arrays.fill(orientations, false);
					num = 0;
					delay = 60;
				}

				// Ensure aura reset is only run when applicable
				aura = false;

				new GamePacket.PlayParticles(EnumParticle.FLAME, playerLoc, 0.4, 0.3, 0.4, 0, 1, true)
						.send();
				new GamePacket.PlayParticles(EnumParticle.SMOKE_NORMAL, playerLoc, 0.4, 0.3, 0.4, 0, 1, true)
						.send();

				oldLoc = player.getLocation();
			}
		};
	}
}
