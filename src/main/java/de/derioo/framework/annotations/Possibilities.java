package de.derioo.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to specify the possibilities of a placeholder
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Possibilities {

    /**
     * The args
     * for e.g.
     * <p>
     * args = "{time}->~1-100~ {unit}->s;m;h {player}->~getPlayers~"
     * also a range is specifiable
     * a method as well
     * </p>
     * @return the args
     */
    String args() default "";

}
