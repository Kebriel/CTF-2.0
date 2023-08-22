package kebriel.ctf.internal;

import kebriel.ctf.Constants;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.internal.concurrent.AsyncExecutor;
import kebriel.ctf.player.CTFPlayer;

public class BackgroundProcess {

	private static Thread autosave;
	private static Thread autoclean;

	/**
	 * Autosaves the stats of online players in order to ensure that
	 * minimal data is lost in the event of a server crash. In all
	 * other cases, player data is saved when the player leaves the
	 * server -- including by timeouts and the like
	 *
	 * See AUTOSAVE_INTERVAL in Constants for more info
	 */
	public static void startAutosave() {
		if(autosave.isAlive() || autosave.isInterrupted()) return;
		AsyncExecutor.doTimer(() -> {
			if(autosave == null) autosave = Thread.currentThread();

			while(!autosave.isInterrupted()) {
				for(CTFPlayer prof : CTFPlayer.getAllCachedProfiles())
					if(prof.isOnline()) prof.saveToDB();
			}
		}, Constants.AUTOSAVE_INTERVAL);
	}

	/**
	 * Currently, the Autoclean process simply serves the purpose of purging
	 * any expired/excess EventReaction instances
	 */
	public static void startAutoclean() {
		if(autoclean.isAlive() || autoclean.isInterrupted()) return;
		/*
		 * Purges any loose/'expired' EventReaction instances, namely, at the time of
		 * writing, extra FlagTracker instances for players who are no longer
		 * online and/or that map to PlayerState instances that have been garbage
		 * collected
		 */
		AsyncExecutor.doTimer(() -> {
			if(autoclean == null) autoclean = Thread.currentThread();
			EventReaction.purgeExpired();
		}, 80);
	}

	/**
	 * End threads for code cleanliness reasons/good practice
	 *
	 * Called in onDisable()
	 */
	public static void stopAll() {
		autosave.interrupt();
		autoclean.interrupt();
	}

}
