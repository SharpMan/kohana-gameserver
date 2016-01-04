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
        BuffEffect Buff = null;
        for (Fighter Target : CastInfos.targets) {
            Buff = new BuffState(CastInfos, Target);
            if (Target.getStates().canState(FightStateEnum.valueOf(CastInfos.effect.value)) && !Target.getBuff().buffMaxStackReached(Buff)) {
                Target.getBuff().addBuff(Buff);
                if (Buff.applyEffect(null, null) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

}
