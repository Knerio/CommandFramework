package de.derioo.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {

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
