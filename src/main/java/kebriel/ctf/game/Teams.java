package kebriel.ctf.game;

import io.netty.util.internal.ConcurrentSet;
import kebriel.ctf.event.async.AsyncGamePhaseEnd;
import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.event.reaction.ReactPriority;
import kebriel.ctf.game.component.PlayerCollection;
import kebriel.ctf.internal.player.GameMessage;
import kebriel.ctf.internal.player.GameSound;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.util.JavaUtil;
import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Teams implements EventReactor {

	private static final Set<Team> teams;
	private static final Set<TeamQueue> queues;

	static {
		queues = Collections.synchronizedSet(new HashSet<>());
		teams = Collections.synchronizedSet(new HashSet<>());

		// Register whichever teams will actually exist
		teams.add(new Team(TeamColor.BLUE));
		teams.add(new Team(TeamColor.RED));

		// Load corresponding team queues
		for(Team t : teams)
			queues.add(t.getCorrespondingQueue());

		EventReaction.register(new Teams());
	}

	public static Set<Team> getTeams() {
		return teams;
	}

	/**
	 * Gets a team by TeamColor, if it exists
	 */
	public static Team getTeam(TeamColor color) {
		for(Team team : teams) {
			if(team.getTeamColor() == color) return team;
		}
		return null;
	}

	/**
	 * Gets a team queue by team color, if it exists
	 */
	public static TeamQueue getQueue(TeamColor color) {
		for(TeamQueue q : queues) {
			if(q.getCorrespondingTeam().getTeamColor() == color) return q;
		}
		return null;
	}

	/**
	 * Sorts all players into teams based on basic equality logic, prioritizing
	 * players' queued referencing
	 *
	 * Should only be run once, that being just before game start
	 */
	private static void sortTeams() {
		List<CTFPlayer> extraPlayers = new ArrayList<>(CTFPlayer.getAllOnline());

		TeamQueue[] qs = queues.toArray(new TeamQueue[0]);

		balanceQueues(qs);

		for(TeamQueue q : qs) {
			for(CTFPlayer p : q.getPlayers()) extraPlayers.remove(p); // Remove queued players from 'extra'
			q.getCorrespondingTeam().addAll(q.getPlayers());
			q.clear();
		}

		Collections.shuffle(extraPlayers);

		//Evenly distribute remaining players
		for(CTFPlayer extra : extraPlayers)
			sortPlayer(extra);
	}

	/**
	 * Balance the queues in preparation for team sorting
	 */
	private static void balanceQueues(TeamQueue... queues) {
		int total = 0;
		for(TeamQueue q : queues) total+=q.getSize();

		int desiredLength = total/queues.length;
		for(TeamQueue q : queues) {
			int toDistribute = q.getSize() - desiredLength;
			if(toDistribute <= 0) continue;

			while(q.getSize() > desiredLength+1) { // Allow a margin of variance to the tune of 1
				TeamQueue min = (TeamQueue) findSmallestBesides(q);
				if(q.getSize() > min.getSize() + 1)
					min.add(q.takeLast());
			}
		}
	}

	/**
	 * Returns the smallest PlayerCollection from the given PlayerCollections
	 */
	private static PlayerCollection findSmallest(PlayerCollection... queues) {
		PlayerCollection min = queues[0];
		for (PlayerCollection q : queues) {
			if (q.getSize() < min.getSize()) {
				min = q;
			}
		}
		return min;
	}

	/**
	 * Returns a Set of all players that are in all teams, exempting the
	 * provided team(s)
	 */
	public static Set<CTFPlayer> getAllPlayersInTeamsBesides(Team... exempt) {
		Set<CTFPlayer> players = new ConcurrentSet<>();
		for(Team t : exempt) {
			for(Team team : getAllTeamsBesides(t)) {
				players.addAll(team.getPlayers());
			}
		}
		return players;
	}

	/**
	 * Returns a Set of all existing teams, exempting the provided
	 * team
	 */
	public static Set<Team> getAllTeamsBesides(Team exempt) {
		Set<Team> others = new HashSet<>();
		for(Team team : teams) {
			if(!team.equals(exempt)) {
				others.add(team);
			}
		}
		return others;
	}

	/**
	 * Returns the smallest PlayerCollection *besides* whichever PlayerCollection is
	 * provided as an argument
	 */
	private static PlayerCollection findSmallestBesides(PlayerCollection exempt, PlayerCollection... queues) {
		if(queues.length == 0) return null;
		if(queues.length == 1) return queues[0];

		PlayerCollection min = findSmallest(queues);
		while(min.equals(exempt)) min = findSmallest(queues);
		return min;
	}

	/**
	 * Utility method returning whether a player is currently
	 * in any TeamQueue
	 */
	public static boolean isPlayerQueued(CTFPlayer player) {
		return getPlayersQueue(player) != null;
	}

	/**
	 * Utility method returning whether a player is
	 * currently on any team
	 */
	public static boolean isPlayerOnATeam(CTFPlayer player) {
		return getPlayersTeam(player) != null;
	}

	/**
	 * Utility method returning the team queue that a player
	 * is on, if they are on one
	 */
	public static TeamQueue getPlayersQueue(CTFPlayer player) {
		for(TeamQueue q : queues) {
			if(q.containsPlayer(player)) return q;
		}
		return null;
	}

	/**
	 * Utility method returning the team that a player
	 * is on, if they are on one
	 */
	public static Team getPlayersTeam(CTFPlayer player) {
		for(Team team : teams) {
			if(team.containsPlayer(player)) return team;
		}
		return null;
	}

	/**
	 * Sorts a specific player randomly into the available
	 * teams
	 *
	 * Meant to be run when a player joins mid-game
	 * @param player the player to sort
	 */
	public static void sortPlayer(CTFPlayer player) {
		Team t = (Team) findSmallest(JavaUtil.typeArray(teams, Team.class));
		t.add(player);
		t.updateRenderWith(player);
	}

	/**
	 * Returns whether two players are on the same team
	 */
	public static boolean areOnSameTeam(CTFPlayer first, CTFPlayer second) {
		for(Team team : teams) {
			if(team.containsBoth(first, second)) return true;
		}
		return false;
	}

	/**
	 * Removes a player from whatever queue they're on, if they're
	 * on one
	 */
	public static void clearFromQueue(CTFPlayer player) {
		player.send(Teams.isPlayerQueued(player) ? GameMessage.LOBBY_CLEARED_QUEUE : GameMessage.LOBBY_CANT_CLEAR_QUEUE);
		player.play(Teams.isPlayerQueued(player) ? GameSound.MENU_YES : GameSound.MENU_NO);
		if(Teams.isPlayerQueued(player))
			Teams.getPlayersQueue(player).remove(player);
	}

	/**
	 * Only runs if game is currently 'in map' (phases InGame OR PostGame)
	 * @param event
	 */
	@EventReact(allowedWhen = GameStage.IN_MAP, priority = ReactPriority.HIGH)
	public void onJoin(PlayerJoinEvent event) {
		CTFPlayer player = CTFPlayer.get(event.getPlayer());

		// If player isn't already on a team (leave and rejoin in the same game)
		if(!isPlayerOnATeam(player))
			sortPlayer(player);

		/*
		 * Make sure this player sees the proper nametags of all players
		 * on all other teams, and that they all see this player's appropriate
		 * nametag
		 */
		for(Team t : teams)
			if(!t.equals(player.getTeam())) // To avoid redundant re-rendering
				t.renderFor(player);
	}

	/**
	 * Preemptively sort players into teams just as Lobby is ending
	 */
	@EventReact(allowedWhen = GameStage.LOBBY, priority = ReactPriority.HIGH)
	public void preloadBeforeGame(AsyncGamePhaseEnd event) {
		sortTeams();
	}

	/**
	 * A very basic enum representing a team by color
	 *
	 * Saves a corresponding ChatColor and byte material code
	 * for easy reference
	 */
	public enum TeamColor {

		BLUE(ChatColor.BLUE, 11), RED(ChatColor.RED, 14);

		private final ChatColor display;
		private final int colorID;

		TeamColor(ChatColor display, int colorID) {
			this.display = display;
			this.colorID = colorID;
		}

		public ChatColor getDisplayColor() {
			return display;
		}

		public byte getColorID() {
			return (byte) colorID;
		}
	}

}
