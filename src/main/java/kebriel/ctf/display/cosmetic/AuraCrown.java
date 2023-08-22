package kebriel.ctf.display.cosmetic;

import kebriel.ctf.Constants;
import kebriel.ctf.display.cosmetic.component.CosmeticAura;
import kebriel.ctf.display.cosmetic.component.CosmeticType;
import kebriel.ctf.display.cosmetic.component.WrappedParticle;
import kebriel.ctf.internal.concurrent.AsyncExecutor;
import kebriel.ctf.player.PlayerState;
import kebriel.ctf.player.Stat;
import kebriel.ctf.internal.nms.GamePacket;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.util.MinecraftUtil;
import kebriel.ctf.internal.player.text.Text;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Function;

public class AuraCrown implements CosmeticAura {

	@Override
	public CosmeticType getType() {
		return CosmeticType.AURA;
	}

	@Override
	public Function<CTFPlayer, Boolean> getUnlockCriteria() {
		return player -> (int) player.getStat(Stat.LEVEL) >= Constants.COSMETIC_CROWN_LEVEL_COST;
	}

	@Override
	public String getID() {
		return "aura_crown";
	}

	@Override
	public Text getSubtext() {
		return Text.get().red("You must be Level ").boldColor(ChatColor.YELLOW, Constants.COSMETIC_CROWN_LEVEL_COST).red(" or higher to use this");
	}

	@Override
	public Material getIcon() {
		return Material.DIAMOND_HELMET;
	}

	@Override
	public Text getDescription() {
		return Text.get().blue("You are crowned in a mantle of")
				.newLine().blue("fiery gold");
	}

	@Override
	public String getName() {
		return "Crowned";
	}

	@Override
	public BukkitRunnable getAura(PlayerState player) {
		return new BukkitRunnable() {
			Location oldLoc = player.getLocation();
			int timer = 3;
			int direction = 1;
			float angle = 0;
			float spriteProgress = 0;
			@Override
			public void run() {
				if(player.isDead())
					return;

				/*
				 * Thread-unsafe getting of player's location made acceptable
				 * by the fact that all operations done with the thread:
				 * 1. Are async, they don't pertain to gamestate or any main-threaded
				 * operations whatsoever
				 * 2. Are particle-sending instances of GamePacket, which checks the 'validity'
				 * (online status) of a player asynchronously before sending, therefor there is no
				 * risk of a player being offline while this thread still believes they are online
				 *
				 * In the event that this thread is working with a location that is a few
				 * milliseconds 'behind' the main thread, the only consequence will be entirely
				 * insignificant displacement of particles
				 */
				Location playerLoc = player.getLocation();
				AsyncExecutor.doTask(() -> {

					// Crown effect
					if(timer == 0) {
						timer = 3;

						WrappedParticle p1 = new WrappedParticle(EnumParticle.REDSTONE, 0.77, 0.55, 0.122, 1, 0, true);
						WrappedParticle p2 = new WrappedParticle(EnumParticle.REDSTONE, 0.77, 0.77, 0.255, 1, 0, true);

						double x = 0.295;
						double y = 2;
						double z = 0.288;

						double yaw = Math.toRadians(playerLoc.getYaw());
						for (int point = 0; point < 5; point++) { // Draws square relative to player's location and orientation
							Location relativeLoc = MinecraftUtil.getLocRelative(playerLoc, x, y, z, yaw);
							z -= 0.12;
							p1.play(relativeLoc);
							p2.play(relativeLoc.clone().add(0, 0.1, 0));

							relativeLoc = MinecraftUtil.getLocRelative(playerLoc, x, y, z, yaw);
							x -= 0.12;
							p1.play(relativeLoc);
							p2.play(relativeLoc.clone().add(0, 0.1, 0));

							relativeLoc = MinecraftUtil.getLocRelative(playerLoc, x, y, z, yaw);
							z += 0.12;
							p1.play(relativeLoc);
							p2.play(relativeLoc.clone().add(0, 0.1, 0));

							relativeLoc = MinecraftUtil.getLocRelative(playerLoc, x, y, z, yaw);
							x += 0.12;
							p1.play(relativeLoc);
							p2.play(relativeLoc.clone().add(0, 0.1, 0));
						}
					}

					// When still -- make a sprite effect
					if(playerLoc.getX() == oldLoc.getX() && playerLoc.getY() == oldLoc.getY() && playerLoc.getZ() == oldLoc.getZ()) {
						Location sprite = new Location(MinecraftUtil.getBukkitWorld(),
								playerLoc.getX() + (0.8 * Math.sin(angle)),
								playerLoc.getY() + spriteProgress,
								playerLoc.getZ() + (0.8 * Math.cos(angle)));

						// Re-adjust upwards or downwards direction if necessary
						if(spriteProgress <= 0 || spriteProgress >= 2) {
							new GamePacket.PlayParticles(EnumParticle.FLAME, sprite, 0, 0, 0, 0.02f, 10, true)
									.send();
							direction = spriteProgress <= 0 ? 1 : -1;
						}

						// Adjust y modifier/'progress' based on direction
						spriteProgress+=(0.1*direction);

						// Play sprite effect
						new GamePacket.PlayParticles(EnumParticle.REDSTONE, sprite, 0.77, 0.77, 0.255, 1, 0, true)
								.send();
						angle+=0.1;
					}

					timer--;
					oldLoc = player.getLocation();
				});
			}
		};
	}
}
