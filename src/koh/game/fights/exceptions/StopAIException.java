package koh.game.fights.exceptions;

import koh.game.fights.Fight;
import koh.game.fights.Fighter;

/**
 * Created by Melancholia on 1/13/16.
 */
public class StopAIException extends FighterException {

    public StopAIException(String message, Fight fight, Fighter fighter) {
        super(message, fight, fighter);
    }

    @Override
    public void finalAction() {

    }
}
