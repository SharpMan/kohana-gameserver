package koh.game.fights.effects;

import koh.game.dao.DAO;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fighter;

/**
 * Created by Melancholia on 1/3/16.
 */
public class EffectCastSpell extends EffectBase {


    @Override
    public int applyEffect(EffectCast CastInfos) {
        SpellLevel spell = DAO.getSpells().findSpell(CastInfos.effect.diceNum).getSpellLevel(CastInfos.effect.diceSide);

        for(Fighter target : CastInfos.targets){
            target.getFight().affectSpellTo(CastInfos.caster,target,CastInfos.effect.diceSide,CastInfos.effect.diceNum);
        }
        return -1;
    }
}
