/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package koh.game.entities.environments;

import java.util.function.Predicate;
import java.util.stream.Stream;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.FieldNotification;
import koh.game.entities.actors.character.FieldOperation;
import koh.game.fights.Fighter;
import koh.game.utils.Observable;
import koh.protocol.client.Message;

/**
 *
 * @author Neo-Craft
 */
public abstract class IWorldEventObserver extends Observable {

    public void registerPlayer(Player p) {
        this.Observable$register(p);
    }

    public void unregisterPlayer(Player p) {
        this.Observable$unregister(p);
    }

    public void sendToField(Message packet) {
        this.Observable$setChanged();
        this.Observable$notify(packet);
    }

    public void sendToField(FieldNotification task) {
        this.Observable$setChanged();
        this.Observable$notify(task);
        task.onFinishSend();
    }

    public void sendToField(FieldOperation op) {
        this.Observable$setChanged();
        this.Observable$notify(op);
    }

    public Iterable<Player> observable$Stream() {
        return observers.stream()::iterator;
    }

    public Iterable<Player> observable$Stream(Predicate<Player> prd) {
        return observers.stream().filter(prd)::iterator;
    }

}
