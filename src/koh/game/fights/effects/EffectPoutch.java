package koh.game.fights.effects;

import koh.game.dao.DAO;
import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffPoutch;
import lombok.extern.log4j.Log4j2;

/**
 * @author Neo-Craft
 */
@Log4j2
public class EffectPoutch extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {

        if (castInfos.duration > 0)
            for (Fighter Target : castInfos.targets)
                Target.getBuff().addBuff(new BuffPoutch(castInfos, Target));
        else
            castInfos.getFight().launchSpell(castInfos.caster, DAO.getSpells().findSpell(castInfos.effect.diceNum).getSpellLevel(castInfos.effect.diceSide), castInfos.caster.getCellId(), true, true, true);

        log.debug("duration {} ",castInfos.duration);


        return -1;
    }

}
