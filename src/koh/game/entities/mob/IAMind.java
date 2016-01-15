package koh.game.entities.mob;

import koh.game.fights.AI.AIProcessor;
import koh.game.fights.exceptions.FightException;
import koh.game.fights.exceptions.FighterException;

/**
 * Created by Melancholia on 1/13/16.
 */
public interface IAMind {

    void play(AIProcessor IA) throws FightException, FighterException;

}
