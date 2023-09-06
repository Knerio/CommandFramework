package de.derioo.framework.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to setup a command
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandProperties {

    /**
     * the permission
     * default empty String (no permission)
     * @return the perm
     */
    String permission() default "";

    /**
     * The name of the command
     * @return the name
     */
    String name();


}
