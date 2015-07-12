package koh.game.entities.actors.character;

import koh.game.entities.actors.Player;
import koh.protocol.client.Message;

/**
 *
 * @author Alleos13
 */
public abstract class FieldNotification {

    public Message packet;

    public FieldNotification(Message packet) {
        this.packet = packet;
    }

    public void onFinishSend() {
        this.packet = null;
        try {
            this.finalize();
        } catch (Throwable tr) {
        }
    }

    public abstract boolean can(Player perso);
}
