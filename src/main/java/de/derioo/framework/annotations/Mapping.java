package de.derioo.framework.annotations;




import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to define a subcommand
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Mapping {

    /**
     * The args
     * for e. g
     * <p>
     *  args = "{time} {unit} {player}
     *
     * @return the args
     */
    String args();

    /**
     * Used to create an extra permission
     * @return if it should uses an extra permission
     */
    boolean extraPermission() default false;

    /**
     * The extra permission
     * @return the permission
     */
    String permission() default "";


}
