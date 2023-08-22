package kebriel.ctf;

import kebriel.ctf.command.*;
import kebriel.ctf.event.listeners.*;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.game.Game;
import kebriel.ctf.internal.concurrent.AsyncExecutor;
import kebriel.ctf.internal.BackgroundProcess;
import kebriel.ctf.internal.sql.SQLManager;
import kebriel.ctf.util.MinecraftUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class CTFMain extends JavaPlugin {

	public static CTFMain instance;

	@Override
	public void onEnable() {
		instance = this;

		initStageOne();
		initStageTwo();
		initStageThree();
	}

	private void initStageOne() {
		World w = MinecraftUtil.getBukkitWorld();
		SQLManager.loadMaps();

		// Set appropriate GameRules as a failsafe
		w.setGameRuleValue("randomTickSpeed", "0");
		w.setGameRuleValue("keepInventory", "true");
		w.setGameRuleValue("doFireTick", "false");
		w.setGameRuleValue("doMobSpawning", "false");
		w.setGameRuleValue("doDaylightCycle", "false");
		w.setGameRuleValue("mobGriefing", "false");

		cmds();
		listeners();
	}

	private void initStageTwo() {

	}

	private void initStageThree() {
		BackgroundProcess.startAutosave();
		BackgroundProcess.startAutoclean();
		Game.get();
	}

	@Override
	public void onDisable() {
		SQLManager.saveMaps();
		shutdown();
	}

	private void cmds() {
		getCommand("stats").setExecutor(new StatsCommand());
		getCommand("map").setExecutor(new MapCommand());
		getCommand("vote").setExecutor(new VoteCommand());
		getCommand("game").setExecutor(new GameCommand());
		getCommand("shout").setExecutor(new ShoutCommand());
		getCommand("cosmetic").setExecutor(new CosmeticCommand());
	}

	private void listeners() {
		Bukkit.getPluginManager().registerEvents(new EventReaction(), this);
		Bukkit.getPluginManager().registerEvents(new onLogin(), this);
		Bukkit.getPluginManager().registerEvents(new onMove(), this);
		Bukkit.getPluginManager().registerEvents(new onLethalDamage(), this);
		Bukkit.getPluginManager().registerEvents(new onInventoryClick(), this);
		Bukkit.getPluginManager().registerEvents(new onInventoryClose(), this);
		Bukkit.getPluginManager().registerEvents(new onConsumePotion(), this);
		Bukkit.getPluginManager().registerEvents(new onTeleport(), this);
		Bukkit.getPluginManager().registerEvents(new onChat(), this);
	}

	private void shutdown() {
		BackgroundProcess.stopAll();
		AsyncExecutor.shutdown();
	}
}
