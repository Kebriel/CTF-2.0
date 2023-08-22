package kebriel.ctf.event.reaction;

public @interface Reactor {
    ReactorPersistence persistence() default ReactorPersistence.STATIC;
}
