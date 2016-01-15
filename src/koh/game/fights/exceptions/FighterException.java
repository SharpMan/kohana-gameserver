package koh.game.fights.exceptions;

import koh.game.fights.Fight;
import koh.game.fights.Fighter;

/**
 * Created by Melancholia on 1/13/16.
 */
public abstract class FighterException extends Exception {

    protected Fight fight;
    protected Fighter fighter;

    public FighterException(String message, Fight fight, Fighter fighter)
    {
        super(message);
        this.fight = fight;
        this.fighter = fighter;
    }

    public abstract void finalAction();

}
