package kebriel.ctf.display.cosmetic;

import kebriel.ctf.display.cosmetic.component.CosmeticAura;
import kebriel.ctf.display.cosmetic.component.CosmeticType;
import kebriel.ctf.display.cosmetic.component.WrappedParticle;
import kebriel.ctf.player.PlayerState;
import kebriel.ctf.player.Stat;
import kebriel.ctf.player.Stat.StatType;
import kebriel.ctf.internal.nms.GamePacket;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.util.MinecraftUtil;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Material;

import kebriel.ctf.player.CTFPlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Function;

public class AuraWings implements CosmeticAura {

	@Override
	public Material getIcon() {
		return Material.FEATHER;
	}

	@Override
	public String getID() {
		return "aura_wings";
	}

	@Override
	public Text getDescription() {
		return Text.get().blue("Project a pair of magical wings")
				.newLine().blue("behind you when standing still,")
				.newLine().blue("and leave arcane dust in your wake");
	}

	@Override
	public String getName() {
		return "Angelic";
	}

	@Override
	public CosmeticType getType() {
		return CosmeticType.AURA;
	}

	@Override
	public Function<CTFPlayer, Boolean> getUnlockCriteria() {
		return player -> {
			boolean unlocked = true;
			for(Stat stat : Stat.values())
				if(stat.getStatType() == StatType.UNLOCKABLE_ABILITY || stat.getStatType() == StatType.UNLOCKABLE_SLOT)
					if(!player.getIsUnlocked(stat))
						unlocked = false;
			return unlocked;
		};
	}

	@Override
	public Text getSubtext() {
		return Text.get().red("You must purchase and own every unlockable ability, extra item, perk")
				.newLine().red("and slot in order to use this");
	}

	@Override
	public BukkitRunnable getAura(PlayerState player) {
		return new BukkitRunnable() {
			Location oldLoc = player.getLocation();
			int timer = 10;
			@Override
			public void run() {
				if(player.isDead() || !player.isOnGround())
					return;

				Location playerLoc = player.getLocation();

				if(playerLoc.getX() == oldLoc.getX() && playerLoc.getY() == oldLoc.getY() && playerLoc.getZ() == oldLoc.getZ()) {
					timer--;
					if(timer == 0) {
						timer = 10;
						WrappedParticle interior = new WrappedParticle(EnumParticle.ENCHANTMENT_TABLE, 0, 0, 0, 0, 1, true);
						WrappedParticle exterior = new WrappedParticle(EnumParticle.REDSTONE, 0.4, 0.77, 0.77, 1, 0, true);

						makeWing(player, interior, exterior, true);
						makeWing(player, interior, exterior, false);
					}
					return;
				}

				new GamePacket.PlayParticles(EnumParticle.ENCHANTMENT_TABLE, playerLoc, 0.7, 0.8, 0.7, 0.2, 4, true)
						.send();
				new GamePacket.PlayParticles(EnumParticle.SPELL_INSTANT, playerLoc, 0.2, 0, 0.2, 0, 1, true)
						.send();

				oldLoc = playerLoc;
			}
		};
	}

	/*
	 * True = left
	 * False = right
	 */
	private void makeWing(PlayerState player, WrappedParticle interior, WrappedParticle exterior, boolean orientation) {
		int wing[][] = {
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{ 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{ 0, 0, 0, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{ 0, 0, 2, 2, 1, 1, 1, 1, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{ 0, 2, 2, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0},
				{ 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 0, 0, 0, 0, 0, 0},
				{ 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 0, 0, 0, 0},
				{ 0, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 0, 0, 0},
				{ 0, 0, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 0, 0},
				{ 0, 0, 0, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 0},
				{ 0, 0, 0, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2},
				{ 0, 0, 0, 0, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2},
				{ 0, 0, 0, 0, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2},
				{ 0, 0, 0, 0, 0, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2},
				{ 0, 0, 0, 0, 0, 0, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 0, 2},
				{ 0, 0, 0, 0, 0, 0, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 2, 2, 1, 1, 1, 1, 1, 1, 1, 2, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 1, 1, 1, 1, 1, 2, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 1, 1, 1, 1, 2, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 1, 1, 2, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 1, 2, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0},
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0} };

		Location ploc = player.getLocation();

		double yaw = Math.toRadians(ploc.getYaw() + (orientation ? 165 : 15));
		double x = -2.15;
		double y = 2;
		double z = orientation ? 0.045 : -0.045;

		for (int i = 0; i < wing.length; i++) {
			for (int j = 0; j < wing[i].length; j++) {
				switch(wing[i][j]) {
					case 1 -> interior.play(MinecraftUtil.getLocRelative(ploc, x, y, z, yaw));
					case 2 -> exterior.play(MinecraftUtil.getLocRelative(ploc, x, y, z, yaw));
				}
				x+=0.1;
			}
			x = -2.15;
			y-=0.05;
		}
	}
}
