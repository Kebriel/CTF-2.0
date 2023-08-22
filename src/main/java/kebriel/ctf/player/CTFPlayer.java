package kebriel.ctf.player;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import io.netty.channel.Channel;
import kebriel.ctf.CTFMain;
import kebriel.ctf.Constants;
import kebriel.ctf.ability.components.Ability;
import kebriel.ctf.ability.components.AbilityRegistry;
import kebriel.ctf.display.cosmetic.component.CosmeticAura;
import kebriel.ctf.display.cosmetic.component.EffectInstance;
import kebriel.ctf.display.gui.component.Selectable;
import kebriel.ctf.display.gui.component.inventory.GUIBase;
import kebriel.ctf.display.scoreboards.ScoreboardLine;
import kebriel.ctf.game.Game;
import kebriel.ctf.game.Team;
import kebriel.ctf.game.Teams;
import kebriel.ctf.internal.APIQuery;
import kebriel.ctf.internal.nms.GamePacket.SetXP;
import kebriel.ctf.internal.player.title.GameTitle;
import kebriel.ctf.internal.player.title.WrappedTitle;
import kebriel.ctf.internal.sql.SQLManager;
import kebriel.ctf.player.Stat.StatType;
import kebriel.ctf.internal.concurrent.AsyncExecutor;
import kebriel.ctf.internal.nms.event.PacketListener;
import kebriel.ctf.internal.nms.PacketRegistry;
import kebriel.ctf.internal.sql.SQLHelper;
import kebriel.ctf.internal.sql.SQLManager.WrappedData;
import kebriel.ctf.internal.sql.Table;
import kebriel.ctf.internal.sql.WrappedStatement;
import kebriel.ctf.display.scoreboards.GameBoard;
import kebriel.ctf.util.JavaUtil;
import kebriel.ctf.util.MinecraftUtil;
import kebriel.ctf.internal.player.GameMessage;
import kebriel.ctf.internal.player.GameSound;
import kebriel.ctf.internal.player.text.Text;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Mega-class that encompasses all data-handling and game-handling
 * needed for players who play Capture the Flag
 */

public class CTFPlayer {

	// Generic nonstatic data
	private long seconds_this_session;
	private EffectInstance effect;
	private List<SelectedAbility> selected;
	private GameBoard currentSidebar;

	// Static/intrinsic data
	private final UUID id;
	private String name;
	private volatile boolean online;
	private volatile Player player;
	private volatile PlayerState state;
	private volatile GUIBase currentMenu;
	private volatile EntityPlayer nmsPlayer;
	private final Set<WrappedData> wrappedStats;

	// Utility objects
	private volatile AsyncExecutor reapTask;
	private final WeakReference<CTFPlayer> weakReference;
	private static final CTFMain main;
	private static final Set<CTFPlayer> cached;
	private static final Set<CTFPlayer> onlineCache;
	private final SetXP xpRender;

	static {
		main = CTFMain.instance;
		cached = new CopyOnWriteArraySet<>();
		onlineCache = new CopyOnWriteArraySet<>();
	}

	public static CTFPlayer get(UUID id) {
		for(CTFPlayer prof : cached) { // Avoid making new PlayerProfile if player already joined since server restarted
			if(prof.getUUID().equals(id)) return prof;
		}
		CTFPlayer p = new CTFPlayer(id);
		cached.add(p);
		return p;
	}

	public static CTFPlayer getOffline(APIQuery query) {
		MinecraftUtil.ensureAsync();

		if(!query.isFinished() || query.getResult() == null || !(query.getResult() instanceof UUID))
			return null;

		WrappedStatement statement = SQLHelper.getRow(Table.PLAYERS, (UUID) query.getResult());
		statement.waitForFinish();

		if(statement.hasResults()) { // 'Fake'/offline CTFPlayer is not cached
			CTFPlayer player = new CTFPlayer(statement.getValueOfField(1, "id"));
			player.setName(query.getQuery());
			for(Stat type : Stat.getPermStats())
				player.setStat(type, statement.getValueOfField(1, type.getID()));
			return player;
		}

		return null;
	}

	public static CTFPlayer get(Player p) {
		return get(p.getUniqueId());
	}

	public static Set<CTFPlayer> getAllCachedProfiles() {
		return cached;
	}

	public static Set<CTFPlayer> getAllOnline() {
		return onlineCache;
	}

	private CTFPlayer(UUID id) {
		this.id = id;
		wrappedStats = new HashSet<>();
		weakReference = new WeakReference<>(this);

		checkForLevel();
		xpRender = new SetXP(getStat(Stat.LEVEL), getLevelProgress());
		xpRender.setReceivers(this);

		if(online) {


			// Track playtime by second
			new BukkitRunnable() { //Async repeating task that tallies up playtime once the Player's profile is loaded

				@Override
				public void run() {
					if(online) {
						seconds_this_session++;
					}else{
						this.cancel();
					}
				}

			}.runTaskTimerAsynchronously(main, 0, 20);
		}
	}

	/**
	 * Assesses whether this player has enough xp to level up,
	 * potentially more than once
	 */
	private void checkForLevel() {
		int xp = getStat(Stat.XP);
		int level = getStat(Stat.LEVEL);

		int newLevels = 0;
		int nextLevelCost = getLevelCost(level);
		while(xp >= nextLevelCost) {
			newLevels++;
			xp-=nextLevelCost;
			nextLevelCost = getLevelCost(level);
		}

		if(newLevels > 0)
			levelTo(level, xp);
	}

	public <T> T getStat(Stat type) {
		return findStat(type).getValueAbsolute();
	}

	public boolean getIsUnlocked(Stat stat) {
		if(stat.getStatType() == StatType.UNLOCKABLE_SLOT || stat.getStatType() == StatType.UNLOCKABLE_ABILITY)
			return getStat(stat);

		throw new IllegalArgumentException("Cannot test if non-unlockable ability '" + stat + "' is unlocked");
	}

	public void setStat(Stat type, Object value) {
		if(type == Stat.XP)
			checkForLevel();
		findStat(type).setValue(value);
	}

	public void incrementStat(Stat type) {
		AsyncExecutor.doAsyncIfNot(() -> {
			WrappedData wrappedStat = findStat(type);
			if(JavaUtil.isNumeric(wrappedStat.getValueAbsolute())) {
				wrappedStat.setValue(JavaUtil.performAmbiguousMath(wrappedStat.getValueAbsolute(), 1, '+'));
			}
			updateScoreboardLine(type);
		});
	}

	public void addToStat(Stat type, Number toAdd) {
		AsyncExecutor.doAsyncIfNot(() -> {
			WrappedData wrappedStat = findStat(type);
			if(JavaUtil.isNumeric(wrappedStat.getValueAbsolute())) {
				wrappedStat.setValue(JavaUtil.performAmbiguousMath(wrappedStat.getValueAbsolute(), toAdd, '+'));
			}
			updateScoreboardLine(type);
		});
	}

	public void decrementStat(Stat type) {
		AsyncExecutor.doAsyncIfNot(() -> {
			WrappedData wrappedStat = findStat(type);
			if(JavaUtil.isNumeric(wrappedStat.getValueAbsolute())) {
				wrappedStat.setValue(JavaUtil.performAmbiguousMath(wrappedStat.getValueAbsolute(), 1, '-'));
			}
			updateScoreboardLine(type);
		});
	}

	public void subtractFromStat(Stat type, Number toSubtract) {
		AsyncExecutor.doAsyncIfNot(() -> {
			WrappedData wrappedStat = findStat(type);
			if(JavaUtil.isNumeric(wrappedStat.getValueAbsolute())) {
				wrappedStat.setValue(JavaUtil.performAmbiguousMath(wrappedStat.getValueAbsolute(), toSubtract, '-'));
			}
			updateScoreboardLine(type);
		});
	}

	private void loadStats() {
		AsyncExecutor.doAsyncIfNot(() -> {
			SQLManager.loadStatsForPlayer(id);

			for(Stat stat : Stat.values()) {
				if(stat.isTemporary()) setStat(stat, stat.getDefaultValue());
			}
		});
	}

	public void saveToDB() {
		if(!valid()) return;
		SQLManager.saveStatsForPlayer(this);
	}

	public synchronized void flush() {
		//setStat(StatType.SECONDS_PLAYED, getStat(StatType.SECONDS_PLAYED)+seconds_this_session);
		cached.remove(this);
	}

	private void reapTimer() {
		reapTask = new AsyncExecutor(t -> {
			if(!this.isOnline()) { // Double check for concurrency purposes
				flush();
			}
		}).doAfterDelay(Constants.PROFILE_REAP_DELAY, TimeUnit.SECONDS);
	}

	private WrappedData findStat(Stat type) {
		for(WrappedData wrappedStat : wrappedStats) {
			if(wrappedStat.getName().equals(type.getID())) return wrappedStat;
		}
		WrappedData newStat = WrappedData.fromStat(type);
		wrappedStats.add(newStat);
		return newStat;
	}

	public List<WrappedData> getWrappedStats() {
		List<WrappedData> result = new ArrayList<>();
		for(WrappedData st : wrappedStats) {
			result.add(st);
		}
		return result;
	}

	public UUID getUUID() {
		return id;
	}

	public Player getBukkitPlayer() {
		return player;
	}

	public EntityPlayer getNMSPlayer() {
		return nmsPlayer;
	}

	public String getNameRaw() {
		return name;
	}

	public String getNameFull() {
		if(Game.get().isPlaying())
			return getTeam().getChatColor() + getNameRaw() + ChatColor.RESET;
		return getNameRaw();
	}

	public long getSecondsPlayedThisSession() {
		return seconds_this_session;
	}

	public String getTimePlayed() {
		long seconds_played = getStat(Stat.SECONDS_PLAYED);
		long dy = TimeUnit.SECONDS.toDays(seconds_played);
		String timePlayed = "";
		final long yr = dy / 365;
		dy %= 365;
		final long mn = dy / 30;
		dy %= 30;
		final long wk = dy / 7;
		dy %= 7;
		final long hr = TimeUnit.SECONDS.toHours(seconds_played)
				- TimeUnit.DAYS.toHours(TimeUnit.SECONDS.toDays(seconds_played));
		final long min = TimeUnit.SECONDS.toMinutes(seconds_played)
				- TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(seconds_played));
		final long sec = seconds_played - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(1000*seconds_played));
		if(yr == 0) {
			if(mn == 0) {
				if(wk == 0) {
					if(dy == 0) {
						if(hr == 0) {
							if(min == 0) {
								timePlayed = sec + "s";
							}else {
								timePlayed = min + "m " + sec + "s";
							}
						}else {
							timePlayed = hr + "h " + min + "m " + sec + "s";
						}
					}else {
						timePlayed = dy + "d " + hr + "h " + min + "m " + sec + "s";
					}
				}else {
					timePlayed = wk + "w " + dy + "d " + hr + "h " + min + "m " + sec + "s";
				}
			}else {
				timePlayed = mn + "mon " + wk + "w " + dy + "d " + hr + "h " + min + "m " + sec + "s";
			}
		}else { //Full
			timePlayed = yr + "yr " + mn + "mon " + wk + "w " + dy + "d " + hr + "h " + min + "m " + sec + "s";
		}
		return timePlayed;
	}

	public double calcKdr() {
		return calcRatio(getStat(Stat.KILLS), getStat(Stat.DEATHS));
	}

	public double calcWlr() {
		return calcRatio(getStat(Stat.WINS), getStat(Stat.LOSSES));
	}

	private double calcRatio(double firstValue, double secondValue) {
		if (secondValue == 0) return firstValue == 0 ? 0.00 : firstValue;
		double ratio = (double) firstValue / secondValue;
		DecimalFormat format = new DecimalFormat("#.00");
		return Double.parseDouble(format.format(ratio));
	}

	// Reset temporary game stats
	public void resetGameStats() {
		for(Stat stat : Stat.values())
			if(stat.isTemporary())
				setStat(stat, stat.getDefaultValue());
	}

	public boolean getIsSelected(String id) {
		for(WrappedData wrappedStat : wrappedStats)
			return wrappedStat.getValueNormal().equals(id);
		return false;
	}

	public boolean getIsAbilitySelected(Class<? extends Ability> select) {
		for(Ability ab : AbilityRegistry.get())
			return ab.getClass().equals(select);
		return false;
	}

	public void setSelected(String id, Stat slot) {
		if(slot.getStatType() != StatType.SELECTED)
			throw new IllegalArgumentException("Incompatible stat type '" + slot.getID() + "' for saving a player's selection");
		// TODO update SelectedAbility
		setStat(slot, id);
	}

	/**
	 * @param type the stat to unlock
	 * @return returns success
	 */
	public boolean unlock(Stat type) {
		if(getStat(type) instanceof Boolean stat) {
			if(stat) return false; //Already unlocked
			setStat(type, true);
		}
		return false;
	}

	public Team getTeam() {
		return Teams.getPlayersTeam(this);
	}

	public Location getLocation() {
		return player.getLocation();
	}

	public synchronized void loggedOn() {
		if(valid()) // Failsafe
			return;

		AsyncExecutor.doTask(() -> {
			makeValid();

			checkForLevel();
			xpRender.send();

			loadStats();

			if(!getSelected(Stat.SELECTED_AURA).equals(Selectable.EMPTY))
				setEffect(((CosmeticAura) getSelected(Stat.SELECTED_AURA)));

			if(reapTask != null) { // Player is back, their profile no longer needs to be removed
				reapTask.terminate(true);
			}
		});
	}

	public synchronized void loggedOff() {
		if(!valid())
			return;

		AsyncExecutor.doTask(() -> {
			PacketRegistry.unrenderAllFor(this);

			saveToDB();
			makeInvalid();

			if(effect != null)
				effect.disable();
			effect = null;

			reapTimer();
		});
	}

	private void makeValid() {
		MinecraftUtil.doSyncIfNot(() -> {
			player = Bukkit.getPlayer(id);
			nmsPlayer = MinecraftUtil.convertBukkitPlayer(player);
			state = new PlayerState(this);
			name = player.getName();
		});

		online = true;
		onlineCache.add(this);
		injectPacketIntercept();
	}

	private void makeInvalid() {
		onlineCache.remove(this);
		online = false;
		player = null;
		nmsPlayer = null;
	}

	public boolean isOnSameTeamAs(CTFPlayer other) {
		return Teams.areOnSameTeam(this, other);
	}

	public boolean isOnline() {
		return online;
	}

	public void sendPacket(Packet<?> packet) {
		if(!valid()) return;
		MinecraftUtil.runOnMainThread(() -> {
			// Work that concerns Bukkit classes is done on main thread
			Player pl = getBukkitPlayer();
			if(pl == null) return;
			EntityPlayer ep = MinecraftUtil.convertBukkitPlayer(pl);
			AsyncExecutor.doTask(() -> {
				PlayerConnection connection = ep.playerConnection;
				if(!(connection != null && !connection.isDisconnected())) return;
				connection.sendPacket(packet);
			});
		});
	}

	private void setName(String name) {
		this.name = name;
	}

	private void injectPacketIntercept() {
		Channel channel = nmsPlayer.playerConnection.networkManager.channel;
		channel.pipeline().addBefore("packet_handler", player.getName(), new PacketListener(this));
	}

	/**
	 * Asks whether this player is fully valid and in the game
	 * @return
	 */
	public boolean valid() {
		return online && player != null && state != null;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CTFPlayer pl)
			return id.equals(pl.getUUID());
		return false;
	}

	public List<Ability> getSelected() {
		List<Ability> result = new ArrayList<>();
		for(SelectedAbility ab : selected) {
			if(ab != null)
				result.add(ab.getSelected());
		}
		return result;
	}

	public <T extends Ability> Set<T> getSelected(Class<T> type) {
		Set<T> result = new HashSet<>();
		for(Ability ab : getSelected())
			if(type.isAssignableFrom(ab.getClass()))
				result.add((T) ab);
		return result;
	}

	public Selectable getSelected(Stat slot) {
		for(SelectedAbility ab : selected) {
			if(ab.getSlot() == slot)
				return ab.getSelected();
		}
		return Selectable.EMPTY;
	}

	public void send(GameMessage message) {
		if(message == null) return;
		send(message.get());
	}

	public void send(Text text) {
		for(String s : text.build())
			player.sendMessage(s);
	}

	public void sendTitle(WrappedTitle title) {
		title.getDisplay().sendFor(this);
	}

	public void play(Sound sound, float volume, float pitch) {
		getBukkitPlayer().playSound(getLocation(), sound, volume, pitch);
	}

	public void play(GameSound sound) {
		getBukkitPlayer().playSound(getLocation(), sound.getSound(), sound.getVolume(), sound.getPitch());
	}

	public void openMenu(GUIBase menu) {
		if(currentMenu != null)
			closeMenu();
		menu.open();
		currentMenu = menu;
	}

	public void setMenu(GUIBase menu) {
		if(currentMenu != menu)
			currentMenu = menu;
	}

	public void closeMenu() {
		if(currentMenu != null) {
			getBukkitPlayer().closeInventory();
			currentMenu = null;
		}
	}

	public void setMenuAsClosed() {
		currentMenu = null;
	}

	public GUIBase getCurrentMenu() {
		return currentMenu;
	}

	public void refreshMenu() {
		if(currentMenu != null)
			currentMenu.refresh(this);
	}

	public void setScoreboard(GameBoard board) {
		currentSidebar = board;
		board.sendRaw(this);
	}

	public GameBoard getCurrentBoard() {
		return currentSidebar;
	}

	public void updateScoreboardLine(Stat stat) {
		updateScoreboardLine(stat.getID());
	}

	public void updateScoreboardLine(String id) {
		AsyncExecutor.doAsyncIfNot(() -> {
			if(currentSidebar == null)
				return;

			ScoreboardLine line = currentSidebar.getLine(id);
			if(line != null)
				line.updateForPlayer(this);
		});
	}

	public void setEffect(CosmeticAura cosmetic) {
		if(effect == null)
			effect = new EffectInstance(this);
		effect.newEffect(cosmetic.getAura(getState()));
	}

	/**
	 * Grabs the progress towards the next level as a float percentage,
	 * purely for display on the player's xp bar
	 */
	private float getLevelProgress() {
		int xp = getStat(Stat.XP);
		int cost = getLevelCost(getStat(Stat.LEVEL));

		return (float) xp / cost;
	}

	/**
	 * Calculates and returns the cost of the next level
	 */
	private int getLevelCost(int level) {
		return (int) (Constants.BASE_LEVEL_COST*(Constants.LEVEL_COST_MULT*level));
	}

	/**
	 * Grabs the value representing how much more xp this player
	 * needs to in order to reach the next level
	 */
	public int getXPToNext() {
		return getLevelCost(getStat(Stat.LEVEL)) - (int) getStat(Stat.XP);
	}

	private void levelTo(int level, int xpRemaining) {
		int currentLevel = getStat(Stat.LEVEL);
		if(level <= currentLevel)
			return;

		setStat(Stat.LEVEL, level);
		setStat(Stat.XP, xpRemaining);

		// Set render
		xpRender.setLevel(level);
		xpRender.setProgress(getLevelProgress());
		xpRender.send();

		send(GameMessage.MULTILINE_LEVEL_UP.fillNext(level));
		sendTitle(GameTitle.LEVEL_UP_SCREEN.get().subtitleFillNext(level));
		play(GameSound.LEVEL_UP);
	}

	public PlayerState getState() {
		return state;
	}

	public WeakReference<CTFPlayer> asWeakRef() {
		return weakReference;
	}
}
