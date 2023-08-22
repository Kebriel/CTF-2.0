package kebriel.ctf.game.component.phase;

import kebriel.ctf.Constants;
import kebriel.ctf.display.scoreboards.GameBoard;
import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.ThreadControl;
import kebriel.ctf.event.async.AsyncFlagCaptureEvent;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.game.Game;
import kebriel.ctf.game.Team;
import kebriel.ctf.game.Teams;
import kebriel.ctf.internal.concurrent.AsyncExecutor;
import kebriel.ctf.internal.concurrent.WorkerThread;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.PlayerState;
import kebriel.ctf.player.Stat;
import kebriel.ctf.util.JavaUtil;
import kebriel.ctf.internal.player.GameMessage;
import kebriel.ctf.internal.player.title.GameTitle;
import kebriel.ctf.util.MinecraftUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class InGame implements GamePhase {

    private enum WinMethod {
        ORGANIC_CAPTURES, DEATHMATCH_CAPTURE, HIGHEST_CAPTURES,
        HIGHEST_KILLS, RANDOMLY_SELECTED
    }

    // Represents all players who played in this game, even if they left
    private final Set<CTFPlayer> playerCache;

    private final Game game;
    private WorkerThread gameThread;
    private final AtomicInteger timer;
    private volatile boolean deathmatch;
    private boolean gracePeriod;
    private Team winner;
    private WinMethod win;

    private final Map<Team, Integer> flagCaptures = new ConcurrentHashMap<>();

    {
        game = Game.get();
        playerCache = new HashSet<>();
        timer = new AtomicInteger();

        EventReaction.register(this);
    }

    @Override
    public void start() {
        if(!AsyncExecutor.isWorkerThread())
            throw new IllegalStateException("Game thread is not on a proper worker thread!");
        gameThread = (WorkerThread) Thread.currentThread();

        playerCache.addAll(CTFPlayer.getAllOnline());

        initGracePeriod();
        GameMessage.PREGAME_GAME_HAS_STARTED.sendGlobalPrefixed();

        game.setCurrentScoreboard(GameBoard.GAME_BOARD);
    }

    @Override
    public void end() {
        handleGameEndStats(winner);
    }

    @Override
    public void updatePlayerState(PlayerState player) {
        MinecraftUtil.runOnMainThread(() -> {
            player.reset();
            player.teleportPlayer(player.getSpawn());
            player.spawn();
        });
    }

    private void initGracePeriod() {
        timer.set(Math.max(Constants.GRACE_PERIOD_DURATION, 30));
        gracePeriod = false;
        GameMessage.PREGAME_GRACE_PERIOD.sendGlobalPrefixed();

        AsyncExecutor task = new AsyncExecutor(t -> {
            switch(timer.get()) {
                case Constants.GRACE_PERIOD_DURATION, 20, 10, 5, 4, 3, 2, 1 -> GameMessage.PREGAME_GRACE_PERIOD_ENDING.sendGlobalPrefixed();
            }
            if(timer.get() == 0) {
                GameMessage.PREGAME_GRACE_PERIOD_OVER.sendGlobalPrefixed();
                gameThread.sendInfo("start");
            }
            timer.decrementAndGet();
        }).doRepeating(0, 1, TimeUnit.SECONDS);

        switch(gameThread.waitForInfo()) {
            case "start" -> startGame();
            case "reset" -> game.resetGame();
        }
        task.terminate(true);
    }

    private void startGame() {
        gracePeriod = false;
        timer.set(Constants.GAME_DURATION_SECONDS);


        AsyncExecutor task = new AsyncExecutor(t -> {
            if(timer.get() == 0) {
                if(!deathmatch) { // If it's not a deathmatch and game hasn't ended yet, start deathmatch
                    startDeathmatch(); // Deathmatch starts (sets timer to new value)
                }else{ // Game has come this far with still no team being organically victorious, forcefully end it
                    endGame();
                    gameThread.sendInfo("end");
                }
            }
            GameBoard.GAME_BOARD.getLine("game_state").updateRegular();
            timer.decrementAndGet();
        }).doRepeating(0, 1, TimeUnit.SECONDS);

        switch(gameThread.waitForInfo()) {
            // Whether resetting or ending organically, endGame() is called (see determineWinner() for more)
            case "end","reset" -> endGame();
        }
        task.terminate(true);
    }

    private void endGame() {
        determineWinner();

        GameTitle toWinningTeam = null;
        GameTitle toOtherTeams = null;

        switch(win) {
            case ORGANIC_CAPTURES -> {
                toWinningTeam = GameTitle.GAME_VICTORY_THREE_CAPTURES_TEAM;
                toOtherTeams = GameTitle.GAME_VICTORY_THREE_CAPTURES_OTHER;
            }
            case DEATHMATCH_CAPTURE -> {
                toWinningTeam = GameTitle.GAME_VICTORY_DEATHMATCH_CAPTURES_TEAM;
                toOtherTeams = GameTitle.GAME_VICTORY_DEATHMATCH_CAPTURES_OTHER;
            }
            case HIGHEST_CAPTURES -> {
                toWinningTeam = GameTitle.GAME_VICTORY_MOST_CAPTURES_TEAM;
                toOtherTeams = GameTitle.GAME_VICTORY_MOST_CAPTURES_OTHER;
            }
            case HIGHEST_KILLS -> {
                toWinningTeam = GameTitle.GAME_VICTORY_MOST_KILLS_TEAM;
                toOtherTeams = GameTitle.GAME_VICTORY_MOST_KILLS_OTHER;
            }
            case RANDOMLY_SELECTED -> {
                toWinningTeam = GameTitle.GAME_VICTORY_LOTTERY_TEAM;
                toOtherTeams = GameTitle.GAME_VICTORY_LOTTERY_OTHER;
            }
        }

        toWinningTeam.sendTo(winner.getPlayers());

        for(Team t : Teams.getTeams())
            if(!t.equals(winner))
                toOtherTeams.sendTo(t.getPlayers());

        game.nextGamePhase();
    }

    private void startDeathmatch() {
        deathmatch = true;
        timer.set(Constants.DEATHMATCH_DURATION_SECONDS);

        GameTitle.GAME_DEATHMATCH_BEGINS.sendToLobby();
    }

    @EventReact(allowedWhen = GameStage.IN_GAME)
    public void onCapture(AsyncFlagCaptureEvent event) {
        Team t = event.getPlayer().getTeam();
        if(deathmatch || t.getCaptures() == 3) {
            winner = t;
            win = deathmatch ? WinMethod.DEATHMATCH_CAPTURE : WinMethod.ORGANIC_CAPTURES;
            gameThread.sendInfo("end");
        }
    }

    private void determineWinner() {
        Set<Team> allTeams = new HashSet<>();
        for(Team t : Teams.getTeams())
            if(t.isActive())
                allTeams.add(t);

        /*
         * Rare case where there are no players left at all...
         * Simply terminate the game, and don't bother with stats or
         * determining a winner
         */
        if(allTeams.isEmpty())
            game.resetGame();

        // Highest captures wins
        boolean capturesTie = JavaUtil.areIntElementsEqual(flagCaptures.values());
        if(!capturesTie) {
            win = WinMethod.HIGHEST_CAPTURES;
            winner = getHighestCaptures(allTeams);
            return;
        }

        // Captures were tied, highest kills wins
        List<Integer> allKills = new ArrayList<>();
        for(Team t : allTeams) {
            allKills.add(t.getTotalKills());
        }
        boolean killsTie = JavaUtil.areIntElementsEqual(allKills);
        if(!killsTie) {
            win = WinMethod.HIGHEST_KILLS;
            winner = getHighestKills(allTeams);
            return;
        }

        // Kills were also tied, constituting an anomaly. Victor is randomly selected
        if(win == null) win = WinMethod.RANDOMLY_SELECTED;
        winner = JavaUtil.typeArray(allTeams, Team.class)[new Random().nextInt(allTeams.size())];
    }

    private Team getHighestKills(Set<Team> teams) {
        Team highestKills = null;
        for(Team t : teams) {
            if(highestKills == null) highestKills = t;
            if(t.getTotalKills() > highestKills.getTotalKills()) highestKills = t;
        }
        return highestKills;
    }

    private Team getHighestCaptures(Set<Team> teams) {
        Team highest = null;
        for(Team t : teams) {
            if(highest == null) highest = t;
            if(t.getCaptures() > highest.getCaptures()) highest = t;
        }
        return highest;
    }

    private void handleGameEndStats(Team winner) {
        if(winner == null)
            return;

        for (Team team : Teams.getTeams()) {
            int rewardGold = team.equals(winner) ? Constants.BASE_WIN_GOLD : Constants.BASE_PARTICIPATION_GOLD;
            int rewardXP = team.equals(winner) ? Constants.BASE_WIN_XP : Constants.BASE_PARTICIPATION_XP;

            int royal = 0;
            for(CTFPlayer p : winner.getPlayers())
                if(p.getIsSelected("perk_royalty")) royal++;

            rewardGold+=(royal*Constants.ROYALTY_EXTRA_GOLD_MISC);
            rewardXP+=(royal*Constants.ROYALTY_EXTRA_XP_MISC);

            for(CTFPlayer player : team.getPlayers()) {
                player.incrementStat(team.equals(winner) ? Stat.WINS : Stat.LOSSES);
                player.incrementStat(Stat.GAMES);
                player.addToStat(Stat.GOLD, rewardGold);
                player.addToStat(Stat.XP, rewardXP);
            }
        }
    }

    public Team getWinner() {
        return winner;
    }

    /**
     * Returns whether there remain enough players for the game to continue,
     * that being at least one online player per team
     */
    private boolean enoughPlayers() {
        int activeTeams = 0;
        for(Team t : Teams.getTeams())
            for(CTFPlayer player : t.getPlayers())
                if(player.isOnline()) {
                    activeTeams++;
                    break; // At least one player in the team is online
                }
        return activeTeams == Teams.getTeams().size();
    }

    @Override
    public GamePhase getNextPhase() {
        return new PostGame();
    }

    @Override
    public int getTimer() {
        return timer.get();
    }

    @EventReact(thread = ThreadControl.ASYNC, allowedWhen = GameStage.IN_GAME)
    public void onJoin(PlayerJoinEvent event) {
        CTFPlayer player = CTFPlayer.get(event.getPlayer());
        playerCache.add(player);
    }

    @EventReact(thread = ThreadControl.ASYNC, allowedWhen = GameStage.IN_GAME)
    public void onLeave(PlayerQuitEvent event) {
        if(!enoughPlayers()) {
            /*
             * Force ends the game if there are no longer enough players to play
             *
             * If there are no players whatsoever, the game will hard reset without
             * determining a winner or distributing certain game-related stats such
             * as games played, wins and losses. See determineWinner() for that
             * mechanism
             */
            gameThread.sendInfo("reset");
        }
    }

    /*
     * Disable friendly fire
     */
    @EventReact(allowedWhen = GameStage.IN_MAP)
    public void onAttack(EntityDamageByEntityEvent event, Player attacker, Player attacked) {
        if(CTFPlayer.get(attacker).isOnSameTeamAs(CTFPlayer.get(attacked)))
            event.setCancelled(true);
    }

    @Override
    public GameMessage getJoinMessage() {
        return GameMessage.GENERIC_PLAYER_JOINED;
    }

    @Override
    public GameMessage getLeaveMessage() {
        return GameMessage.GENERIC_PLAYER_LEFT;
    }

    @Override
    public boolean isCombatAllowed() {
        return gracePeriod;
    }

    @Override
    public boolean isDamageAllowed() {
        return true;
    }

    public boolean isDeathmatch() {
        return deathmatch;
    }

    public boolean isGracePeriod() {
        return gracePeriod;
    }
}
