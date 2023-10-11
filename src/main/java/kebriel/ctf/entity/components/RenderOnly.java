package kebriel.ctf.entity.components;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes an EntityWrapper subclass as being a render-only or
 * 'packet' entity -- i.e., an entity that shouldn't and can never
 * be actually spawned into the world, and can only be 'rendered'
 * using packets.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RenderOnly {
}
