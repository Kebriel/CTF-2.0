package kebriel.ctf.game.map;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import kebriel.ctf.display.scoreboards.GameBoard;
import kebriel.ctf.game.Game;
import kebriel.ctf.game.map.GameMaps.GameMap;
import kebriel.ctf.internal.concurrent.AsyncExecutor;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.internal.player.GameMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MapVoting {

	private final static int MAX_NUMBER_OF_MAPS = 3;
	private static volatile MapVote currentVoting;
	private static final Game game;

	static {
		game = Game.get();
		if(MAX_NUMBER_OF_MAPS > GameMaps.getMapCache().size())
			throw new IllegalStateException("Set number of maps to include in each voting period cannot be greater than the number of existing maps");
	}

	public static void vote(CTFPlayer voter, GameMap map) {
		AsyncExecutor.doAsyncIfNot(() -> {
			if(currentVoting == null || currentVoting.isFinished()) return;
			currentVoting.addPlayerVote(voter, map);

			// For scoreboard updating
			String whichMap = "map";
			for(int i = 0; i < currentVoting.getCandidates().size(); i++)
				if(currentVoting.getCandidates().get(i+1).equals(map)) {
					whichMap+=(i+1);
					break;
				}

			GameBoard.VOTING_BOARD.getLine(whichMap).updateRegular();
		});
	}

	public synchronized static void startVoting() {
		if(currentVoting != null && !currentVoting.isFinished())
			return;

		currentVoting = new MapVote();
		currentVoting.startNew();

		displayOptions();

		game.setCurrentScoreboard(GameBoard.VOTING_BOARD);
	}

	public synchronized static void displayOptions() {
		if(currentVoting == null)
			return;

		for(CTFPlayer player : CTFPlayer.getAllOnline())
			currentVoting.displayOptions(player);
	}

	public synchronized static void displayOptions(CTFPlayer player) {
		if(currentVoting == null)
			return;

		currentVoting.displayOptions(player);
	}

	public synchronized static void finishVoting() {
		if(currentVoting == null)
			return;

		currentVoting.finish();
		GameMessage.LOBBY_VOTING_HAS_ENDED.sendGlobalPrefixed();

		game.setCurrentScoreboard(GameBoard.IDLE_BOARD);
	}

	public synchronized static GameMap getWinner() {
		if(currentVoting == null)
			return null;

		return currentVoting.getWinner();
	}

	public static boolean isVoting() {
		return currentVoting != null && currentVoting.finished;
	}

	public synchronized static List<GameMap> getCandidates() {
		return currentVoting.getCandidates();
	}

	public synchronized static void reset() {
		if(currentVoting == null) return;
		currentVoting.terminate();
		currentVoting = null;

		game.setCurrentScoreboard(GameBoard.IDLE_BOARD);
	}

	public synchronized static List<Map.Entry<GameMap, Integer>> getMapsSortedByVotes() {
		List<Map.Entry<GameMap, Integer>> sortedEntries = new ArrayList<>(currentVoting.getVotes().entrySet());
		sortedEntries.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));
		return sortedEntries;
	}

	public static boolean hasPlayerVoted(CTFPlayer player) {
		return currentVoting.hasPlayerVoted(player);
	}

	public static boolean hasFinished() {
		return currentVoting != null && currentVoting.finished;
	}

	/**
	 * The isolating of map voting to certain object instances is done with the
	 * intention of creating the capability to 'save' previous votes. So long
	 * as the same CTF instance is running, the plugin will become able to remember,
	 * among other things, which maps won, got the second and third highest votes, etc.
	 * This would be to prevent the same map from appearing as a voting candidate again
	 * for some number of voting sessions
	 */
	private static class MapVote {

		private final Map<GameMap, Integer> votes;
		private final Map<CTFPlayer, GameMap> votesFor;
		private final List<GameMap> candidates;
		private volatile GameMap winner;
		private volatile boolean finished;

		private MapVote() {
			votes = new ConcurrentHashMap<>();
			votesFor = new ConcurrentHashMap<>();
			candidates = new CopyOnWriteArrayList<>();
		}

		public synchronized void startNew() {
			candidates.addAll(selectCandidates());
			for(GameMap m : candidates)
				votes.put(m, 0);
		}

		private synchronized void terminate() {
			if(finished)
				return;

			votes.clear();
			votesFor.clear();
			finished = true;
		}

		private void addPlayerVote(CTFPlayer voter, GameMap vote) {
			votes.put(vote, votes.get(vote)+1);
			votesFor.put(voter, vote);
		}

		public boolean hasPlayerVoted(CTFPlayer prof) {
			return votesFor.keySet().contains(prof);
		}

		private void displayOptions(CTFPlayer to) {
			to.send(GameMessage.LOBBY_VOTE_FOR_A_MAP);
			int c = 0;
			for(GameMap m : candidates) {
				c++;
				to.send(GameMessage.LOBBY_MAP_LIST_ENTRY);
			}
			to.send(GameMessage.LOBBY_USE_VOTECMD_TO_VOTE);
		}

		private GameMap getWinner() {
			return winner;
		}

		private synchronized void finish() {
			winner = getMapsSortedByVotes().get(0).getKey();
			finished = true;
			winner.makeCurrent();
		}

		public boolean isFinished() {
			return finished;
		}

		private synchronized List<GameMap> selectCandidates() {
			List<GameMap> candidates = new ArrayList<>();
			List<GameMap> orig = new ArrayList<>(List.copyOf(GameMaps.getMapCache()));
			for(int i = 0; i <= MAX_NUMBER_OF_MAPS; i++) {
				GameMap selected = orig.get(new Random().nextInt(orig.size()));
				orig.remove(selected);
				candidates.add(selected);
			}
			return candidates;
		}

		private List<GameMap> getCandidates() {
			return candidates;
		}

		public int getVotesForMap(GameMap map) {
			return votes.get(map);
		}

		protected Map<GameMap, Integer> getVotes() {
			return votes;
		}
	}

}
