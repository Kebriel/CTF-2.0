package kebriel.ctf.internal.player.text;

import java.util.function.Supplier;

public class ConditionalMessage {

    private final Supplier<Boolean> condition;
    private final String ifTrue;
    private final String ifFalse;

    public ConditionalMessage(Supplier<Boolean> condition, String ifTrue, String ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    public String evaluateResult() {
        return condition.get() ? ifTrue : ifFalse;
    }

    @Override
    public String toString() {
        return evaluateResult();
    }
}
