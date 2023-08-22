package kebriel.ctf.game;

import kebriel.ctf.event.async.AsyncFlagCaptureEvent;
import kebriel.ctf.event.async.AsyncGamePhaseEnd;
import kebriel.ctf.event.async.AsyncPlayerKillEvent;
import kebriel.ctf.event.reaction.EventReact;
import kebriel.ctf.event.reaction.EventReaction;
import kebriel.ctf.event.reaction.EventReactor;
import kebriel.ctf.event.reaction.GameStage;
import kebriel.ctf.event.reaction.ReactPriority;
import kebriel.ctf.game.component.PlayerCollection;
import kebriel.ctf.game.Teams.TeamColor;
import kebriel.ctf.game.flag.Flag;
import kebriel.ctf.player.Stat;
import kebriel.ctf.internal.nms.GamePacket.AlterPlayerNametag;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.util.ColorMappings;
import kebriel.ctf.util.JavaUtil;
import org.bukkit.ChatColor;

public class Team extends PlayerCollection implements EventReactor {

    private final TeamColor color;
    private final TeamQueue queue;
    private final Flag flag;
    private int flagCaptures;
    private int totalKills;
    private CTFPlayer topKiller;
    private CTFPlayer topCapturer;

    private final AlterPlayerNametag coloredNamesRender;

    public Team(TeamColor color) {
        this.color = color;
        queue = new TeamQueue(this);

        flag = new Flag(this);

        coloredNamesRender = AlterPlayerNametag.setColor(getPlayers(), getNameRaw(), getChatColor());

        EventReaction.register(this);
    }

    public String getNameRaw() {
        return color.toString();
    }

    public String getNameFull() {
        return JavaUtil.capitalizeFirstLetter(getChatColor() + color.toString().toLowerCase()) + " Team";
    }

    public TeamColor getTeamColor() {
        return color;
    }

    public ChatColor getChatColor() {
        return ColorMappings.teamToChatColor(color);
    }

    public TeamQueue getCorrespondingQueue() {
        return queue;
    }

    public int getTotalKills() {
        return totalKills;
    }

    public int getCaptures() {
        return flagCaptures;
    }

    private void grabEndGameStats() {
        topKiller = getTopStat(Stat.GAME_KILLS);
        topCapturer = getTopStat(Stat.FLAGS_CAPTURED);
    }

    private CTFPlayer getTopStat(Stat stat) {
        if(!stat.isTemporary()) return null;
        CTFPlayer top = null;
        for(CTFPlayer player : getPlayers()) {
            if(top == null || ((int) player.getStat(stat)) > ((int) top.getStat(stat))) top = player;
        }
        return top;
    }

    public Flag getFlag() {
        return flag;
    }

    public CTFPlayer getTopKiller() {
        return topKiller;
    }

    public CTFPlayer getTopCapturer() {
        return topCapturer;
    }

    public void setupFlag() {
        flag.setup();
    }

    /**
     * Sends the initial ColorNameTag packet to the entire
     * lobby
     *
     */
    public void startRender() {
        coloredNamesRender.render();
    }

    /**
     * Refreshes the packet coloring players' nametags for the
     * whole lobby
     */
    public void rerenderNames() {
        coloredNamesRender.updateRender();
    }

    public void renderFor(CTFPlayer player) {
        coloredNamesRender.renderFor(player);
    }

    /**
     * Updates the rendered player nametags now with
     * this player rendering as part of the team. Adds
     * this player to the 'target list' of the packet,
     * and then re-sends the packet to all intended receivers
     * (the whole lobby)
     * @param add
     */
    public void updateRenderWith(CTFPlayer add) {
        coloredNamesRender.addTarget(add);
        rerenderNames();
    }

    /**
     * As ColorNameTag implements Revertable, the colored
     * name tags can easily be 'derendered' (sends a 'delete'
     * scoreboard team packet to the whole lobby)
     */
    public void endRender() {
        coloredNamesRender.derender();
    }

    public boolean isActive() {
        boolean active = true;
        for(CTFPlayer player : getPlayers())
            if(!player.isOnline())
                active = false;
        return active;
    }

    @EventReact(allowedWhen = GameStage.IN_GAME)
    public void countKill(AsyncPlayerKillEvent event) {
        if(event.getPlayer().getTeam().equals(this))
            totalKills++;
    }

    @EventReact(allowedWhen = GameStage.IN_GAME)
    public void countCapture(AsyncFlagCaptureEvent event) {
        if(event.getPlayer().getTeam().equals(this))
            flagCaptures++;
    }

    /**
     * Lobby is about to end, prepare this Team for the game
     *
     * Set priority to LOW to ensure that players are properly
     * sorted into teams first
     * @param event
     */
    @EventReact(allowedWhen = GameStage.LOBBY, priority = ReactPriority.LOW)
    public void preloadBeforeGame(AsyncGamePhaseEnd event) {
        startRender();
        setupFlag();
    }

    @EventReact(allowedWhen = GameStage.IN_GAME)
    public void onGameEnd(AsyncGamePhaseEnd event) {
        grabEndGameStats();
    }

    @EventReact(allowedWhen = GameStage.POST_GAME)
    public void onPostGameEnd(AsyncGamePhaseEnd event) {
        flag.gameReset();
        endRender();
        clear();
        // Reset team-specific stats
        flagCaptures = 0;
        totalKills = 0;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Team team) {
            if(team.getTeamColor() == color) return true;
        }
        return false;
    }
}
