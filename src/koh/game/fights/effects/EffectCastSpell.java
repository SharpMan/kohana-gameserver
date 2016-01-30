package koh.game.fights.effects;

import koh.game.dao.DAO;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fighter;

/**
 * Created by Melancholia on 1/3/16.
 */
public class EffectCastSpell extends EffectBase {


    @Override
    public int applyEffect(EffectCast castInfos) {
        final SpellLevel spell = DAO.getSpells().findSpell(castInfos.effect.diceNum).getSpellLevel(castInfos.effect.diceSide);

        /*if (castInfos.effect.targetMask.equalsIgnoreCase("a,A,U")) //Arbre sadi daamn the mask
            castInfos.getFight().launchSpell(castInfos.caster, spell, castInfos.cellId, true, true, false);
        else*/
            for (Fighter target : castInfos.targets)
                target.getFight().affectSpellTo(castInfos.caster, target, castInfos.effect.diceSide, castInfos.effect.diceNum);

        return -1;
    }
}
