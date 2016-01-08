package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffEffect;
import koh.game.fights.effects.buff.BuffState;
import koh.protocol.client.enums.FightStateEnum;

/**
 *
 * @author Neo-Craft
 */
public class EffectAddState extends EffectBase {

    @Override
    public int applyEffect(EffectCast CastInfos) {
        BuffEffect buff;
        for (Fighter target : CastInfos.targets) {
            buff = new BuffState(CastInfos, target);
            if (target.getStates().canState(FightStateEnum.valueOf(CastInfos.effect.value)) && !target.getBuff().buffMaxStackReached(buff)) {
                target.getBuff().addBuff(buff);
                if (buff.applyEffect(null, null) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

}
