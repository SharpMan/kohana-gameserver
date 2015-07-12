package koh.game.network.handlers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author Neo-Craft
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface HandlerAttribute {

    int ID();
}
