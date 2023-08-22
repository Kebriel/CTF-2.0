package kebriel.ctf.internal.player.text;

import kebriel.ctf.player.CTFPlayer;
import kebriel.ctf.util.JavaUtil;

import java.util.function.Function;
import java.util.function.Supplier;

public class Placeholders {

    protected static class BasicPlaceholder implements Placeholder {

        private static final String PLACEHOLDER = "$ph$";

        private Supplier<Object> value;

        BasicPlaceholder(Supplier<Object> value) {
            this.value = value;
        }

        @Override
        public void fill(StringBuilder str) {
            JavaUtil.builderReplaceFirst(str, PLACEHOLDER, String.valueOf(value.get()));
        }

        @Override
        public String getRawPlaceholder() {
            return PLACEHOLDER;
        }
    }

    protected static class PlayerDataPlaceholder implements Placeholder {

        private static final String PLACEHOLDER = "$PDph$";

        private final Function<CTFPlayer, Object> playerValue;

        PlayerDataPlaceholder(Function<CTFPlayer, Object> playerValue) {
            this.playerValue = playerValue;
        }

        @Override
        public void fill(StringBuilder str) {}

        @Override
        public void fill(StringBuilder str, CTFPlayer player) {
            JavaUtil.builderReplaceFirst(str, PLACEHOLDER, String.valueOf(playerValue.apply(player)));
        }

        @Override
        public String getRawPlaceholder() {
            return PLACEHOLDER;
        }
    }

    protected static class MessagePlaceholder implements Placeholder {

        private static final String PLACEHOLDER = "$MSGph$";

        private final ConditionalMessage msg;

        MessagePlaceholder(ConditionalMessage msg) {
            this.msg = msg;
        }

        @Override
        public void fill(StringBuilder str) {
            JavaUtil.builderReplaceFirst(str, PLACEHOLDER, msg.evaluateResult());
        }

        @Override
        public String getRawPlaceholder() {
            return PLACEHOLDER;
        }
    }

    protected static class TypedPlaceholder implements Placeholder {

        private static final String PLACEHOLDER = "$TYPph$";

        private final Class<?> type;

        TypedPlaceholder(Class<?> type) {
            this.type = type;
        }

        public Class<?> getType() {
            return type;
        }

        @Override
        public void fill(StringBuilder str, Object fill) {
            JavaUtil.builderReplaceFirst(str, PLACEHOLDER, fill);
        }

        @Override
        public String getRawPlaceholder() {
            return PLACEHOLDER;
        }
    }
}
