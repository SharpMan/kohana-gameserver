package koh.game.fights.effects;

import koh.game.dao.DAO;
import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffPoutch;

/**
 * @author Neo-Craft
 */
public class EffectPoutch extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {

        if (castInfos.duration > 0)
            for (Fighter Target : castInfos.targets)
                Target.getBuff().addBuff(new BuffPoutch(castInfos, Target));
        else
            castInfos.getFight().launchSpell(castInfos.caster, DAO.getSpells().findSpell(castInfos.effect.diceNum).getSpellLevel(castInfos.effect.diceSide), castInfos.caster.getCellId(), true, true, true);

        System.out.println("Lance"+castInfos.duration);


        return -1;
    }

}
