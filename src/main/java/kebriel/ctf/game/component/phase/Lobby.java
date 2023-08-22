package kebriel.ctf.game.component.phase;

import kebriel.ctf.Constants;
import kebriel.ctf.display.scoreboards.GameBoard;
import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.ThreadControl;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.game.Game;
import kebriel.ctf.game.map.MapVoting;
import kebriel.ctf.internal.concurrent.AsyncExecutor;
import kebriel.ctf.internal.concurrent.WorkerThread;
import kebriel.ctf.internal.player.GameMessage;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.PlayerState;
import kebriel.ctf.util.MinecraftUtil;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Lobby implements GamePhase {

    private boolean waiting;
    private boolean starting;
    private final AtomicInteger timer;
    private final Game game;
    private WorkerThread gameThread;

    {
        game = Game.get();
        timer = new AtomicInteger();

        EventReaction.register(this);
    }

    @Override
    public void start() {
        if(!AsyncExecutor.isWorkerThread())
            throw new IllegalStateException("Game thread is not on a proper worker thread!");
        gameThread = (WorkerThread) Thread.currentThread();

        startWaiting();
    }

    @Override
    public void end() {
        stopCountdown();
        MapVoting.reset();
    }

    private synchronized void startWaiting() {
        if(waiting)
            return;

        waiting = true;

        // There are already enough players, go straight to voting
        if(enoughPlayersToStart()) {
            startCountdown();
            return;
        }

        // Only sets to idle board if we don't jump straight to voting
        game.setCurrentScoreboard(GameBoard.IDLE_BOARD);

        timer.set(0);

        // Create separate thread to send info messages every 30 sec
        AsyncExecutor task = new AsyncExecutor(t -> {
            if(timer.get() == 30) {
                if(Thread.currentThread().isInterrupted())
                    return;

                GameMessage.LOBBY_WAITING_FOR_PLAYERS.sendGlobalPrefixed();
                timer.set(0);
            }
            timer.incrementAndGet();
        }).doRepeating(0, 1, TimeUnit.SECONDS);

        /*
         * Game thread waits here until the correct info is sent (see onJoin() in this class)
         *
         * If it already was sent since enoughPlayersToStart() was last checked, method
         * waitForInfo() won't force a wait at all and will immediately proceed
         */
        gameThread.waitForInfo("start");

        // Enough players have joined, game thread is alerted of this and is given the go-ahead to start
        startCountdown();
        task.terminate(true);
    }

    private synchronized void stopWaiting() {
        if(!waiting) throw new IllegalStateException("Cannot terminate waiting process if game isn't waiting");
        waiting = false;
    }

    synchronized private void startCountdown() {
        if(starting) // Failsafe
            return;

        stopWaiting();
        MapVoting.startVoting();
        starting = true;
        timer.set(30);

        // Create extra thread to count the timer down until the game starts
        AsyncExecutor async = new AsyncExecutor(task -> {
            switch(timer.get()) {
                case 30, 20, 10, 5, 3, 2, 1, 0 -> {
                    GameMessage.LOBBY_GAME_IS_STARTING.sendGlobalPrefixed();
                    if(Thread.currentThread().isInterrupted())
                        return;
                    if(timer.get() == 5)
                        MapVoting.finishVoting();
                    if(timer.get() == 0) {
                        gameThread.sendInfo("start_game");
                        task.terminate(false);
                    }
                }
            }
            timer.decrementAndGet();
        }).doRepeating(0, 1, TimeUnit.SECONDS);

        /*
         * Game thread waits here, either for the go-ahead to start the game or to be told that
         * there are now too few players to start (see onLeave() of this class)
         */
        String msg = gameThread.waitForInfo();
        // Sent by extra thread if countdown reaches zero without enough players leavings to terminate the game
        if(msg.equals("start")) { // Info is reset since last "start" message was sent
            game.nextGamePhase();
        }else if(msg.equals("terminate")) { // Too many players left, reset
            async.terminate(true);
            MapVoting.reset();
            stopCountdown();
            startWaiting();
        }
    }

    private synchronized void stopCountdown() {
        if(!starting) throw new IllegalStateException("Cannot terminate countdown process if game isn't starting");
        starting = false;
    }

    @Override
    public void updatePlayerState(PlayerState player) {
        MinecraftUtil.doSyncIfNot(() -> {
            player.reset();
            player.teleportPlayer(Constants.HUB);
            player.updateInventory();
        });
    }

    @Override
    public GamePhase getNextPhase() {
        return new InGame();
    }

    /*
     * This is a separate thread from the Game thread, will not be frozen
     */
    @EventReact(thread = ThreadControl.ASYNC, allowedWhen = GameStage.LOBBY)
    public void onJoin(PlayerJoinEvent event) {
        CTFPlayer player = CTFPlayer.get(event.getPlayer());
        // There are now enough players to start, notify game thread
        if(waiting && gameThread.isWaiting() && enoughPlayersToStart()) {
            if(gameThread.isWaiting()) {
                gameThread.sendInfo("start");
            }
        }
        if(MapVoting.isVoting())
            MapVoting.displayOptions(player);
    }

    @EventReact(thread = ThreadControl.ASYNC, allowedWhen = GameStage.LOBBY)
    public void onLeave(PlayerQuitEvent event) {
        // No longer enough players to start, notify Game thread
        if(starting && !enoughPlayersToStart())
            gameThread.sendInfo("terminate");
    }

    @Override
    public GameMessage getJoinMessage() {
        return waiting ? GameMessage.LOBBY_PLAYER_JOINED_WAITING : GameMessage.GENERIC_PLAYER_JOINED;
    }

    @Override
    public GameMessage getLeaveMessage() {
        return waiting || !enoughPlayersToStart() ? GameMessage.LOBBY_PLAYER_LEFT_WAITING
                : GameMessage.GENERIC_PLAYER_LEFT;
    }

    @Override
    public int getTimer() {
        return timer.get();
    }

    @Override
    public boolean isCombatAllowed() {
        return false;
    }

    @Override
    public boolean isDamageAllowed() {
        return false;
    }

    public boolean enoughPlayersToStart() {
        return game.getPlayerCount() >= Constants.MINIMUM_PLAYERS_TO_START;
    }

    public boolean isWaiting() {
        return waiting;
    }
}
