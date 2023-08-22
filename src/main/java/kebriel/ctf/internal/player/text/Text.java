package kebriel.ctf.internal.player.text;

import kebriel.ctf.internal.player.text.Placeholders.BasicPlaceholder;
import kebriel.ctf.internal.player.text.Placeholders.MessagePlaceholder;
import kebriel.ctf.internal.player.text.Placeholders.PlayerDataPlaceholder;
import kebriel.ctf.internal.player.text.Placeholders.TypedPlaceholder;
import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.util.JavaUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class Text implements Cloneable {

    private static final String BASIC_PLACEHOLDER = "%#";
    private static final String PRIMARY_PLACEHOLDER = "$PR";
    private static final String SECONDARY_PLACEHOLDER = "$SC";

    protected List<StringBuilder> lines;
    private StringBuilder currentLine;
    private List<Placeholder> placeholders;
    private ChatColor primaryColor;
    private ChatColor secondaryColor;
    private boolean filled;

    public static Text get() {
        return new Text();
    }

    public Text() {
        currentLine = new StringBuilder();
        lines = new ArrayList<>();
        placeholders = new ArrayList<>();
    }

    public Text text(Object text) {
        currentLine.append(text);

        return this;
    }

    public Text add(Object text) {
        currentLine.append(text);
        return this;
    }

    public Text newLine() {
        lines.add(currentLine);
        currentLine = new StringBuilder();
        return this;
    }

    public Text emptyLine() {
        newLine();
        text(" ");
        return this;
    }

    public Text prefix(Prefix prefix) {
        currentLine.insert(0, prefix.get());
        return this;
    }

    public Text singleQuoted(String text) {
        currentLine.append("'" + text + "'");
        return this;
    }

    public Text quoted(String text) {
        currentLine.append("\"" + text + "\"");
        return this;
    }

    public Text color(ChatColor color) {
        if(color != null && color.isColor()) currentLine.append(color);
        return this;
    }

    public Text white(Object text) {
        currentLine.append(ChatColor.WHITE).append(text);
        return this;
    }

    public Text green(Object text) {
        currentLine.append(ChatColor.GREEN).append(text);
        return this;
    }

    public Text darkGreen(Object text) {
        currentLine.append(ChatColor.DARK_GREEN).append(text);
        return this;
    }

    public Text blue(Object text) {
        currentLine.append(ChatColor.BLUE).append(text);
        return this;
    }

    public Text darkBlue(Object text) {
        currentLine.append(ChatColor.DARK_BLUE).append(text);
        return this;
    }

    public Text aqua(Object text) {
        currentLine.append(ChatColor.AQUA).append(text);
        return this;
    }

    public Text darkAqua(Object text) {
        currentLine.append(ChatColor.DARK_AQUA).append(text);
        return this;
    }

    public Text purple(Object text) {
        currentLine.append(ChatColor.LIGHT_PURPLE).append(text);
        return this;
    }

    public Text darkPurple(Object text) {
        currentLine.append(ChatColor.DARK_PURPLE).append(text);
        return this;
    }

    public Text gold(Object text) {
        currentLine.append(ChatColor.GOLD).append(text);
        return this;
    }

    public Text black(Object text) {
        currentLine.append(ChatColor.BLACK).append(text);
        return this;
    }

    public Text gray(Object text) {
        currentLine.append(ChatColor.GRAY).append(text);
        return this;
    }

    public Text darkGray(Object text) {
        currentLine.append(ChatColor.DARK_GRAY).append(text);
        return this;
    }

    public Text red(Object text) {
        currentLine.append(ChatColor.RED).append(text);
        return this;
    }

    public Text darkRed(Object text) {
        currentLine.append(ChatColor.DARK_RED).append(text);
        return this;
    }

    public Text yellow(Object text) {
        currentLine.append(ChatColor.YELLOW).append(text);
        return this;
    }

    public Text format(ChatColor format, String text) {
        if(format != null && format.isFormat()) currentLine.append(format);
        currentLine.append(text);
        return this;
    }

    public Text boldColor(ChatColor color, Object text) {
        currentLine.append(String.valueOf(color) + ChatColor.BOLD);
        currentLine.append(text);
        return this;
    }

    public Text italicColor(ChatColor color, String text) {
        currentLine.append(String.valueOf(color) + ChatColor.ITALIC);
        currentLine.append(text);
        return this;
    }

    public Text underlineColor(ChatColor color, String text) {
        currentLine.append(String.valueOf(color) + ChatColor.UNDERLINE);
        currentLine.append(text);
        return this;
    }

    public Text strikethroughColor(ChatColor color, String text) {
        currentLine.append(String.valueOf(color) + ChatColor.STRIKETHROUGH);
        currentLine.append(text);
        return this;
    }

    public Text magicColor(ChatColor color, String text) {
        currentLine.append(color).append(ChatColor.MAGIC);
        currentLine.append(text);
        return this;
    }

    public Text reset(String text) {
        currentLine.append(ChatColor.RESET).append(text);
        return this;
    }

    public Text placeholder() {
        currentLine.append(BASIC_PLACEHOLDER);
        return this;
    }

    public enum BlurbType {

        XP(ChatColor.AQUA, "XP"), GOLD(ChatColor.GOLD, "g"),
        GOLD_REWARD(BlurbType.GOLD.color, BlurbType.GOLD.suffix, "+"),
        XP_REWARD(BlurbType.XP.color, BlurbType.XP.suffix, "+");

        private final ChatColor color;
        private final String suffix;
        private String prefix = "";

        BlurbType(ChatColor color, String suffix) {
            this.color = color;
            this.suffix = suffix;
        }

        BlurbType(ChatColor color, String suffix, String prefix) {
            this(color, suffix);
            this.prefix = prefix;
        }

        public Text process(Text apply) {
            return apply.color(color)
                    .text(prefix)
                    .placeholder()
                    .text(suffix);
        }
    }

    public Text placeholder(BlurbType type) {
        return type.process(this);
    }

    public Text fillNext(Object fill) {
        // Iterates through all lines until a placeholder is found and filled
        for(StringBuilder line : lines)
            if(JavaUtil.builderReplaceFirst(line, BASIC_PLACEHOLDER, String.valueOf(fill)))
                break;
        return this;
    }

    public Text primary(Object s) {
        currentLine.append(PRIMARY_PLACEHOLDER).append(s);
        return this;
    }

    public Text secondary(Object s) {
        currentLine.append(SECONDARY_PLACEHOLDER).append(s);
        return this;
    }

    public Text definePrimary(ChatColor color) {
        primaryColor = color;
        return this;
    }

    public Text defineSecondary(ChatColor color) {
        secondaryColor = color;
        return this;
    }

    public Text insert(Supplier<Object> value) {
        BasicPlaceholder holder = new BasicPlaceholder(value);
        placeholders.add(holder);
        currentLine.append(holder.getRawPlaceholder());
        return this;
    }

    public Text insert(Function<CTFPlayer, Object> value) {
        PlayerDataPlaceholder holder = new PlayerDataPlaceholder(value);
        placeholders.add(holder);
        currentLine.append(holder.getRawPlaceholder());
        return this;
    }

    public Text insert(ConditionalMessage msg) {
        MessagePlaceholder holder = new MessagePlaceholder(msg);
        placeholders.add(holder);
        currentLine.append(holder.getRawPlaceholder());
        return this;
    }

    protected Text fillPlaceholders() {
        for(StringBuilder line : lines)
            for(Placeholder holder : placeholders)
                holder.fill(line);
        filled = true;
        return this;
    }

    public Text fillPlaceholders(CTFPlayer player) {
        fillPlaceholders();
        for(StringBuilder line : lines)
            for(Placeholder holder : placeholders)
                holder.fill(line, player);
        filled = true;
        return this;
    }

    public Text fillPlaceholders(DataSupply data) {
        fillPlaceholders();
        List<Pair<DataField, Object>> dataPairs = data.unpackDataPairs();
        for(StringBuilder line : lines) {
            List<Pair<DataField, Object>> clear = new ArrayList<>();
            for(Pair<DataField, Object> pair : dataPairs) {
                for(Placeholder holder : placeholders)
                    if(holder instanceof TypedPlaceholder typed)
                        if(typed.getType().equals(pair.getLeft().getType())) {
                            typed.fill(line, pair.getRight());
                            clear.add(pair);
                        }
            }
            dataPairs.removeAll(clear);
        }
        filled = true;
        return this;
    }

    public boolean isFilled() {
        return filled;
    }

    public boolean requiresPlayerData() {
        for(Placeholder placeholder : placeholders)
            return placeholder instanceof PlayerDataPlaceholder;
        return false;
    }

    private void fillColors() {
        for(StringBuilder line : lines) {
            if(primaryColor != null)
                JavaUtil.builderReplaceAll(currentLine, PRIMARY_PLACEHOLDER, primaryColor);
            if(secondaryColor != null)
                JavaUtil.builderReplaceAll(currentLine, SECONDARY_PLACEHOLDER, secondaryColor);
        }
    }

    public String[] build() {
        String[] results = new String[lines.size()];
        for(int i = 0; i < lines.size(); i++) {
            results[i] = lines.get(i).toString();
        }
        return results;
    }

    @Override
    public String toString() {
        fillPlaceholders();
        fillColors();
        return currentLine.toString();
    }

    @Override
    public Text clone() {
        try {
            Text cloned = (Text) super.clone();
            cloned.lines = new ArrayList<>(this.lines);
            cloned.placeholders = new ArrayList<>(this.placeholders);
            cloned.currentLine = new StringBuilder(currentLine);
            return cloned;
        } catch(CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
        return this;
    }

}
