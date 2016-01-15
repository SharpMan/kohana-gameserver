package koh.game.fights.exceptions;

import koh.game.fights.Fight;

/**
 * Created by Melancholia on 1/13/16.
 */
public abstract class FightException extends Exception {

    protected Fight fight;

    public FightException(String message, Fight fight)
    {
        super(message);
        this.fight = fight;
    }

    public abstract void finalAction();

}
