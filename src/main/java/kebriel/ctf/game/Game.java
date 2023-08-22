package kebriel.ctf.game;

import kebriel.ctf.CTFMain;
import kebriel.ctf.display.scoreboards.GameBoard;
import kebriel.ctf.event.async.AsyncGamePhaseEnd;
import kebriel.ctf.event.async.AsyncGamePhaseStart;
import kebriel.ctf.event.async.AsyncPlayerDeathEvent;
import kebriel.ctf.event.async.components.CTFEvent;
import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.event.reaction.ReactPriority;
import kebriel.ctf.event.reaction.ThreadControl;
import kebriel.ctf.game.component.phase.GamePhase;
import kebriel.ctf.game.component.phase.InGame;
import kebriel.ctf.game.component.phase.Lobby;
import kebriel.ctf.game.component.phase.PostGame;
import kebriel.ctf.game.map.MapVoting;
import kebriel.ctf.internal.concurrent.AsyncExecutor;
import kebriel.ctf.internal.player.text.Text;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.PlayerState;
import org.bukkit.GameMode;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicInteger;

public class Game implements EventReactor {

    {
        EventReaction.register(this);
    }

    private static Game instance;
    private final AtomicInteger playerCount;
    private volatile GamePhase phase;
    private volatile GameBoard currentBoard;

    public static Game get() {
        return instance == null ? new Game() : instance;
    }

    private Game() {
        instance = this;
        init();
        playerCount = new AtomicInteger();
    }

    private void init() {
        playerCount.addAndGet(CTFPlayer.getAllOnline().size());
        phase = new Lobby();
        phase.start();
    }

    public void nextGamePhase() {
        GamePhase nextPhase = phase.getNextPhase();
        phase.end();
        CTFEvent.fireEvent(new AsyncGamePhaseEnd(phase));
        phase = nextPhase; // Atomic operation on volatile variable
        phase.start();
        updatePlayerStates();
        CTFEvent.fireEvent(new AsyncGamePhaseStart(phase));
    }

    public GamePhase getPhase() {
        return phase;
    }

    public boolean isVoting() {
        return MapVoting.isVoting();
    }

    public boolean isStarting() {
        return phase instanceof Lobby && !isVoting() && !((Lobby)phase).isWaiting();
    }

    public int getTimer() {
        return phase.getTimer();
    }

    /**
     * Handles all basic join functionality for CTF -- wholly supplants
     * the need for a separate PlayerJoinEvent listener class.
     *
     * @param event
     */
    @EventReact(thread = ThreadControl.MAIN, priority = ReactPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        CTFPlayer player = CTFPlayer.get(event.getPlayer());
        event.setJoinMessage(phase.getJoinMessage().toString());

        playerCount.incrementAndGet();
        player.getState().join();
        phase.updatePlayerState(player.getState());

        if(currentBoard == GameBoard.IDLE_BOARD)
            currentBoard.getLine("player_count").updateRegular();
    }

    @EventReact(thread = ThreadControl.MAIN)
    public void onLeave(PlayerQuitEvent event) {
        CTFPlayer player = CTFPlayer.get(event.getPlayer());
        event.setQuitMessage(phase.getLeaveMessage().toString());

        TeamQueue q = Teams.getPlayersQueue(player);
        if(q != null)
            q.remove(player);

        PlayerState state = player.getState();
        if(isPlaying())
            if(state.hasTakenDamage()) // Has taken damage and will be processed as dying
                CTFEvent.fireEvent(new AsyncPlayerDeathEvent(player, 1, DamageCause.CUSTOM));

        playerCount.decrementAndGet();

        player.loggedOff();

        if(currentBoard == GameBoard.IDLE_BOARD)
            currentBoard.getLine("player_count").updateRegular();
    }

    @EventReact
    public void onDamage(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player) || !isDamageAllowed()) {
            event.setCancelled(true);
            return;
        }

        AsyncExecutor.doTask(() -> CTFPlayer.get((Player) event.getEntity()).getState().registerDamage());
    }

    @EventReact
    public void onHunger(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventReact
    public void onBreak(BlockBreakEvent event) {
        if(event.getPlayer().getGameMode() != GameMode.CREATIVE && !event.getPlayer().isOp())
            event.setCancelled(true);
    }

    @EventReact
    public void onPickup(PlayerPickupItemEvent event) {
        event.setCancelled(true);
    }

    @EventReact
    public void onDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventReact
    public void onShoot(EntityShootBowEvent e) {
        if(e.getProjectile() instanceof Arrow a) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if(a.isValid()) {
                        if(a.isOnGround()) {
                            a.remove();
                            this.cancel();
                        }
                    }else {
                        this.cancel();
                    }
                }

            }.runTaskTimer(CTFMain.instance, 0, 2);
        }
    }

    public int getPlayerCount() {
        return playerCount.get();
    }

    public boolean isDeathmatch() {
        if(phase instanceof InGame inGame)
            return inGame.isDeathmatch();
        return false;
    }

    public void resetGame() {
        phase.end();
        phase = new Lobby();
        phase.start();
    }

    public void start() {
        if(isPlaying())
            return;
        phase.end();
        phase = new InGame();
        phase.start();
    }

    public void sendGlobal(Text msg) {
        for(CTFPlayer player : CTFPlayer.getAllOnline())
            player.send(msg);
    }

    public boolean isCombatAllowed() {
        return phase.isCombatAllowed();
    }

    public boolean isDamageAllowed() {
        return phase.isDamageAllowed();
    }

    public boolean isGracePeriod() {
        return phase instanceof InGame && ((InGame) phase).isGracePeriod();
    }

    public void setCurrentScoreboard(GameBoard board) {
        for(CTFPlayer player : CTFPlayer.getAllOnline())
            player.setScoreboard(board);
        currentBoard = board;
    }

    private void updatePlayerStates() {
        for(CTFPlayer player : CTFPlayer.getAllOnline())
            phase.updatePlayerState(player.getState());
    }

    public GameBoard getCurrentBoard() {
        return currentBoard;
    }

    public boolean isPlaying() {
        return phase instanceof InGame || phase instanceof PostGame;
    }
}
