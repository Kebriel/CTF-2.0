package kebriel.ctf.game.component.phase;

import kebriel.ctf.Constants;
import kebriel.ctf.game.Game;
import kebriel.ctf.game.Teams;
import kebriel.ctf.game.Team;
import kebriel.ctf.internal.concurrent.AsyncExecutor;
import kebriel.ctf.internal.concurrent.WorkerThread;
import kebriel.ctf.player.PlayerState;
import kebriel.ctf.player.Stat;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.internal.player.GameMessage;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PostGame implements GamePhase {

    private final AtomicInteger timer;
    private final Game game;
    private WorkerThread gameThread;

    {
        game = Game.get();
        timer = new AtomicInteger();
    }

    @Override
    public void start() {
        if(!AsyncExecutor.isWorkerThread())
            throw new IllegalStateException("Game thread is not on a proper worker thread!");
        gameThread = (WorkerThread) Thread.currentThread();

        init();
    }

    @Override
    public void end() {
        for(CTFPlayer player : CTFPlayer.getAllOnline())
            player.resetGameStats();
    }

    @Override
    public void updatePlayerState(PlayerState player) {}

    private void init() {
        timer.set(Constants.ENDGAME_DURATION_SECONDS);

        CTFPlayer topKiller = getTopKiller();
        CTFPlayer topCapturer = getTopCapturer();

        AsyncExecutor task = new AsyncExecutor(t -> {
            switch(timer.get()) {
                case 7 -> game.sendGlobal(GameMessage.MULTILINE_ENDGAME_STATS_PLAYERS.get()
                        .fillNext(topKiller.getNameFull())
                        .fillNext(topKiller.getStat(Stat.GAME_KILLS))
                        .fillNext(topCapturer.getNameFull())
                        .fillNext(topCapturer.getStat(Stat.GAME_CAPTURES)));
                case 0 -> gameThread.sendInfo("end");
            }
            timer.decrementAndGet();
        }).doRepeating(0, 1, TimeUnit.SECONDS);

        gameThread.waitForInfo("end");
        task.terminate(true);
        game.nextGamePhase();
    }

    private CTFPlayer getTopKiller() {
        CTFPlayer topKiller = null;
        for(Team t : Teams.getTeams()) {
            if(topKiller == null || ((int)t.getTopKiller().getStat(Stat.GAME_KILLS)) > ((int)topKiller.getStat(Stat.GAME_KILLS))) {
                topKiller = t.getTopKiller();
            }
        }
        return topKiller;
    }

    private CTFPlayer getTopCapturer() {
        CTFPlayer topCapturer = null;
        for(Team t : Teams.getTeams()) {
            if(topCapturer == null || ((int)t.getTopCapturer().getStat(Stat.GAME_CAPTURES)) > ((int)topCapturer.getStat(Stat.GAME_CAPTURES))) {
                topCapturer = t.getTopCapturer();
            }
        }
        return topCapturer;
    }

    @Override
    public GamePhase getNextPhase() {
        return new Lobby();
    }

    @Override
    public int getTimer() {
        return timer.get();
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
        return false;
    }

    @Override
    public boolean isDamageAllowed() {
        return false;
    }
}
