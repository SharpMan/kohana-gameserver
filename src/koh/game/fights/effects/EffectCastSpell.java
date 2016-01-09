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
        SpellLevel spell = DAO.getSpells().findSpell(castInfos.effect.diceNum).getSpellLevel(castInfos.effect.diceSide);

        for(Fighter target : castInfos.targets){
            target.getFight().affectSpellTo(castInfos.caster,target, castInfos.effect.diceSide, castInfos.effect.diceNum);
        }
        return -1;
    }
}
