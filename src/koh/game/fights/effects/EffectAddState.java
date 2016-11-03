package koh.game.fights.effects;

import koh.game.dao.DAO;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffDecrementType;
import koh.game.fights.effects.buff.BuffEffect;
import koh.game.fights.effects.buff.BuffState;
import koh.game.fights.fighters.BombFighter;
import koh.game.fights.fighters.SummonedFighter;
import koh.protocol.client.enums.FightStateEnum;

/**
 *
 * @author Neo-Craft
 */
public class EffectAddState extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        BuffEffect buff;
        for (Fighter target : castInfos.targets) {
            buff = new BuffState(castInfos, target);

            //TODO turn's problem
            /*if(FightStateEnum.valueOf(castInfos.effect.value) == FightStateEnum.PORTAL){
                buff.decrementType = BuffDecrementType.TYPE_BEGINTURN;
            }*/
            if((castInfos.effect.value == 244 || castInfos.effect.value == 251) && target.getCellId() == castInfos.oldCell && target instanceof SummonedFighter && target.asSummon().getGrade().getMonsterId() == 3958) { // Synchro
                final SpellLevel spell = DAO.getSpells().findSpell(5435).getLevelOrNear(target.asSummon().getGrade().getLevel());
                castInfos.getFight().launchSpell(target, spell, target.getCellId(), true, true, true, castInfos.spellId);
                continue;
                // target.tryDie(castInfos.caster.getID(), true);
            }
            if (target.getStates().canState(FightStateEnum.valueOf(castInfos.effect.value)) && !target.getBuff().buffMaxStackReached(buff)) {
                target.getBuff().addBuff(buff);
                if (buff.applyEffect(null, null) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

}
