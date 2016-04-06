package koh.game.dao.api;


import koh.d2o.entities.SpellBomb;
import koh.game.entities.spells.LearnableSpell;
import koh.game.entities.spells.Spell;
import koh.game.entities.spells.SpellLevel;
import koh.patterns.services.api.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

/**
 * Created by Melancholia on 11/28/15.
 */
public abstract class SpellDAO implements Service {

    protected static final Logger logger = LogManager.getLogger(SpellDAO.class);

    public abstract SpellBomb findBomb(int id);

    public abstract SpellLevel findLevel(int id);

    public abstract Spell findSpell(int id);

    public abstract ArrayList<LearnableSpell> findLearnableSpell(int id);

}
