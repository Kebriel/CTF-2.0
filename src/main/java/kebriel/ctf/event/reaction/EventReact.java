package kebriel.ctf.event.reaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventReact {
    ThreadControl thread() default ThreadControl.MAIN;
    GameStage[] allowedWhen() default {GameStage.UNDEFINED};
    ReactPriority priority() default ReactPriority.MEDIUM;
}
