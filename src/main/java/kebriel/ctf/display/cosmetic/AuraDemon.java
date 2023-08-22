package kebriel.ctf.display.cosmetic;

import kebriel.ctf.Constants;
import kebriel.ctf.display.cosmetic.component.CosmeticAura;
import kebriel.ctf.display.cosmetic.component.CosmeticType;
import kebriel.ctf.player.PlayerState;
import kebriel.ctf.player.Stat;
import kebriel.ctf.internal.nms.GamePacket;
import kebriel.ctf.internal.player.text.Text;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;

import kebriel.ctf.player.CTFPlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.function.Function;

public class AuraDemon implements CosmeticAura {

	@Override
	public String getID() {
		return "aura_demon";
	}

	@Override
	public CosmeticType getType() {
		return CosmeticType.AURA;
	}

	@Override
	public Function<CTFPlayer, Boolean> getUnlockCriteria() {
		return player -> (int) player.getStat(Stat.KILLS) >= Constants.COSMETIC_DEMONIC_KILL_COST;
	}

	@Override
	public Text getSubtext() {
		return Text.get().red("You must have killed at least ").boldColor(ChatColor.YELLOW, Constants.COSMETIC_DEMONIC_KILL_COST).red(" players to use this");
	}

	@Override
	public Material getIcon() {
		return Material.OBSIDIAN;
	}

	@Override
	public Text getDescription() {
		return Text.get().blue("Embrace a demonic aura around")
				.newLine().blue("yourself, forming a dark ritual")
				.newLine().blue("at your feet");
	}

	@Override
	public String getName() {
		return "Demonic";
	}

	@Override
	public BukkitRunnable getAura(PlayerState player) {
		return new BukkitRunnable() {
			Location oldLoc = player.getLocation();
			double radius = 1.9f;
			double angle = 0;

			double spriteRadius = 1.5f;

			int timer = 70;
			boolean sprite = false;
			double yoffset = 0;
			double spriteProgress = 0;
			@Override
			public void run() {
				if(player.isDead())
					return;

				Location playerLoc = player.getLocation();

				//Triangle effect
				angle +=0.05;
				Location[] locs = new Location[3];
				for(int i = 0; i < 3; i++) {
					double angle = (2*Math.PI*i/3)+ this.angle;
					locs[i] = playerLoc.clone().add(radius * Math.sin(angle), 0, radius*Math.cos(angle));
					new GamePacket.PlayParticles(EnumParticle.SPELL_WITCH, locs[i], 0, 0, 0, 0, 1, true)
							.send();
				}

				for(int i = 0; i < locs.length; i++) {
					Vector v = locs[i].toVector();
					Location otherLoc = locs[i < 2 ? i+1 : i-1].clone();
					Vector line = otherLoc.toVector().subtract(v).normalize().multiply(0.1);
					double distance = locs[i].distance(otherLoc);
					double progress = 0;

					for(; progress < distance; v.add(line)) {
						new GamePacket.PlayParticles(EnumParticle.SPELL_MOB_AMBIENT, locs[i].clone().add(v.getX(), v.getY(), v.getZ()), 0, 0, 0, 0, 1, true)
								.send();
						progress+=0.1;
					}
				}

				if(playerLoc.getX() == oldLoc.getX() && playerLoc.getY() == oldLoc.getY() && playerLoc.getZ() == oldLoc.getZ()) {
					// Sprite effect
					if(timer <= 0)
						sprite = true;

					if(sprite) {
						spriteRadius-=0.03;
						yoffset+=0.05;
						spriteProgress +=0.3;
						Location l = playerLoc.clone().add(spriteRadius * Math.sin(spriteProgress), yoffset, spriteRadius * Math.cos(spriteProgress));
						new GamePacket.PlayParticles(EnumParticle.SPELL_MOB, l, 0, 0, 0, 0, 3, true)
								.send();
						new GamePacket.PlayParticles(EnumParticle.SMOKE_NORMAL, l, 0, 0, 0, 0.02f, 2, true)
								.send();

						if(spriteRadius <= 0) { // Reset
							sprite = false;
							timer = 70;
							spriteProgress = 0;
							yoffset = 0;
							spriteRadius = 1.5f;
							new GamePacket.PlayParticles(EnumParticle.SMOKE_NORMAL, l, 0, 0, 0, 0.08f, 25, true)
									.send();
						}
					}

					timer--;
				}

				oldLoc = playerLoc;
			}
		};
	}
}
