package kebriel.ctf.display.scoreboards;

import io.netty.util.internal.chmv8.ConcurrentHashMapV8.Fun;
import kebriel.ctf.internal.nms.GamePacket.UpdateScoreboardLine;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.internal.player.text.Text;

import java.util.function.Function;
import java.util.function.Supplier;

public class ScoreboardLine {

    private final String boardID;
    private final String lineID;
    private final Text contents;
    private int score;
    private Supplier<Integer> wrappedScore;
    private Function<CTFPlayer, Boolean> condition;

    public ScoreboardLine(String boardID, String lineID, Text contents) {
        this.boardID = boardID;
        this.lineID = lineID;
        this.contents = contents;
    }

    public ScoreboardLine(String boardID, String lineID, Text contents, Supplier<Integer> wrappedScore) {
        this(boardID, lineID, contents);
        this.wrappedScore = wrappedScore;
    }

    public ScoreboardLine fillValues(CTFPlayer player) {
        contents.fillPlaceholders(player);
        return this;
    }

    public String getID() {
        return lineID;
    }

    public Text getContents() {
        return contents;
    }

    public int getScore() {
        if(wrappedScore != null)
            score = wrappedScore.get();
        return score;
    }

    public ScoreboardLine makeConditional(Function<CTFPlayer, Boolean> condition) {
        this.condition = condition;
        return this;
    }

    public void updateForPlayer(CTFPlayer updateFor) {
        if(condition != null && !condition.apply(updateFor)) return;
        UpdateScoreboardLine update = new UpdateScoreboardLine(boardID, getScore(), fillValues(updateFor).toString());
        update.setReceivers(updateFor);
        update.send();
    }

    public void updateRegular() {
        new UpdateScoreboardLine(boardID, getScore(), toString()).send();
    }

    public void updateScore(int score) {
        new UpdateScoreboardLine(boardID, score, toString()).send();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ScoreboardLine line)
            return line.getID().equals(lineID);
        return false;
    }

    @Override
    public String toString() {
        return contents.toString();
    }
}
