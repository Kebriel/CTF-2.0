package kebriel.ctf.internal.player.text;

import kebriel.ctf.player.CTFPlayer;

interface Placeholder {

    default void fill(StringBuilder str) {}

    default void fill(StringBuilder str, CTFPlayer player) {}

    default void fill(StringBuilder str, Object obj) {}

    String getRawPlaceholder();
}
