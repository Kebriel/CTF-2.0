package kebriel.ctf.display.gui.component.button;

import kebriel.ctf.ability.PerkUntrackable;
import kebriel.ctf.display.gui.component.inventory.GameGUI;
import kebriel.ctf.player.FlagTracker;
import kebriel.ctf.game.Teams;
import kebriel.ctf.game.Teams.TeamColor;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.player.PlayerState;
import kebriel.ctf.player.item.CTFItem;
import kebriel.ctf.internal.player.GameMessage;
import kebriel.ctf.internal.player.GameSound;
import kebriel.ctf.internal.player.text.Text;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class GUIButtonTrack extends GUIButton {

    public GUIButtonTrack(CTFItem item, PlayerState pl, GameGUI gui, TeamColor which) {
        super(item.addLore(Text.get()
                        .text(pl.getTracker().getTrackedFlag().getTeam().getTeamColor() == which ? ChatColor.GREEN + "You are currently tracking this flag" : ChatColor.YELLOW + "Click to track this flag").toString())
                        .build(),
                gui, player -> {
            FlagTracker tracker = pl.getTracker();
            if(tracker.getTrackedFlag().getTeam().getTeamColor() == which) {
                player.send(GameMessage.TRACKER_ALREADY_TRACKING);
                player.play(GameSound.MENU_NO);
                return;
            }

            CTFPlayer holder = Teams.getTeam(which).getFlag().getHolder();
            if(holder != null && holder.getIsAbilitySelected(PerkUntrackable.class)) {
                player.send(GameMessage.TRACKER_CANNOT_TRACK);
                player.play(GameSound.ABILITY_FAIL);
                return;
            }

            tracker.setTarget(Teams.getTeam(which).getFlag());
            player.send(GameMessage.TRACKER_SUCCESSFULLY_TRACKING);
            player.play(GameSound.TRACKER);
        });
    }
}
