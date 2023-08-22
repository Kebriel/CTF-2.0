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

import java.util.function.Function;

public class AuraVoid implements CosmeticAura {

	@Override
	public Material getIcon() {
		return Material.BEDROCK;
	}

	@Override
	public String getID() {
		return "aura_void";
	}

	@Override
	public Text getDescription() {
		return Text.get().blue("Surround yourself in an aura of")
				.newLine().blue("the void, and leave darkness in")
				.newLine().blue("your steps");
	}

	@Override
	public String getName() {
		return "Void Aura";
	}

	@Override
	public CosmeticType getType() {
		return CosmeticType.AURA;
	}

	@Override
	public Function<CTFPlayer, Boolean> getUnlockCriteria() {
		return player -> (int) player.getStat(Stat.LOSSES) >= Constants.COSMETIC_LOSS_REQ
				&& (int) player.getStat(Stat.WINS) >= Constants.COSMETIC_LOSS_REQ*2
				&& (int) player.getStat(Stat.FLAGS_CAPTURED) >= Constants.COSMETIC_LOSS_REQ*3;
	}

	@Override
	public Text getSubtext() {
		return Text.get().red("You must have ").boldColor(ChatColor.YELLOW, Constants.COSMETIC_LOSS_REQ).red(" losses, twice as many wins, and ").boldColor(ChatColor.YELLOW, "3x").red(" as many flags captured");
	}

	@Override
	public BukkitRunnable getAura(PlayerState player) {
		return new BukkitRunnable() {
			Location oldLoc = player.getLocation();
			int delay = 240;
			double radius = 0.2;
			final double[] angles = new double[4];
			boolean inwards;
			int num;

			@Override
			public void run() {
				if(player.isDead() || !player.isOnGround())
					return;

				Location playerLoc = player.getLocation();

				if(playerLoc.getX() == oldLoc.getX() && playerLoc.getY() == oldLoc.getY() && playerLoc.getZ() == oldLoc.getZ()) {
					new GamePacket.PlayParticles(EnumParticle.SUSPENDED_DEPTH, playerLoc, 0.5, 1, 0.5, 0, 3, true)
							.send();
					new GamePacket.PlayParticles(EnumParticle.SMOKE_NORMAL, playerLoc, 0.5, 1, 0.5, 0, 1, true)
							.send();

					if(radius <= 0.2)
						inwards = false;
					if(radius >= 1.5) {
						inwards = true;
						for(double a = 0; a < 100; a+=1) {
							Location circle = playerLoc.clone().add((radius*Math.sin(a)), 0, (radius*Math.cos(a)));
							new GamePacket.PlayParticles(EnumParticle.SMOKE_NORMAL, circle, 0, 0, 0, 0, 1, true)
									.send();
							new GamePacket.PlayParticles(EnumParticle.REDSTONE, circle, 0.0001, 0.0001, 0.0001, 1, 0, true)
									.send();
						}
					}

					radius+=inwards ? -0.01 : 0.01;

					for(int i = 0; i < 4; i++) {
						if(delay == 160) {
							num = 1;
						}else if(delay == 80) {
							num = 2;
						}else if(delay == 0) {
							num = 3;
						}

						if(num >= i) {
							angles[i]+=0.1;
						}

						Location l = playerLoc.clone().add(radius*Math.sin(angles[i]), 0, radius*Math.cos(angles[i]));
						switch(num) {
							case 0 -> new GamePacket.PlayParticles(EnumParticle.REDSTONE, l, 0.0001, 0.0001, 0.0001, 1, 0, true)
									.send();
							case 1 -> new GamePacket.PlayParticles(EnumParticle.REDSTONE, l, 0.255, 0.255, 0.255, 1, 0, true)
									.send();
							case 2 -> new GamePacket.PlayParticles(EnumParticle.REDSTONE, l, 0.170, 0.170, 0.170, 1, 0, true)
									.send();
							case 3 -> new GamePacket.PlayParticles(EnumParticle.REDSTONE, l, 0.77, 0.77, 0.77, 1, 0, true)
									.send();
						}
					}
					if(delay > 0)
						delay--;
					return;
				}

				new GamePacket.PlayParticles(EnumParticle.REDSTONE, playerLoc, 0.0001, 0.0001, 0.0001, 1, 0, true)
						.send();
				new GamePacket.PlayParticles(EnumParticle.SMOKE_NORMAL, playerLoc, 0.2, 0, 0.2, 0.01f, 1, true)
						.send();

				oldLoc = playerLoc;
			}
		};
	}

}
