package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffDecrementType;
import koh.game.fights.effects.buff.BuffEffect;
import koh.game.fights.effects.buff.BuffState;
import koh.game.fights.fighters.BombFighter;
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
