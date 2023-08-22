package kebriel.ctf.event.reaction;

/**
 * Simplified and inverted form of Bukkit's EventPriority
 *
 * As a result of inversion, higher priority EventReact methods
 * get to run their code first
 */
public enum ReactPriority {
    HIGH, MEDIUM, LOW
}
