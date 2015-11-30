package koh.game.dao.api;


import koh.game.entities.spells.LearnableSpell;
import koh.game.entities.spells.Spell;
import koh.patterns.services.api.Service;

import java.util.ArrayList;

/**
 * Created by Melancholia on 11/28/15.
 */
public abstract class SpellDAO implements Service {

    public abstract Spell findSpell(int id);

    public abstract ArrayList<LearnableSpell> findLearnableSpell(int id);

}
