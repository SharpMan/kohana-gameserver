package koh.game.network.websocket;

/**
 * Created by Melancholia on 2/23/16.
 */

import koh.protocol.client.enums.CommPacketEnum;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface HandlerAttribute {

    CommPacketEnum ID();
}
