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
    public int ApplyEffect(EffectCast CastInfos) {
        BuffEffect Buff = null;
        for (Fighter Target : CastInfos.Targets) {
            Buff = new BuffState(CastInfos, Target);
            if (Target.getStates().canState(FightStateEnum.valueOf(CastInfos.Effect.value)) && !Target.getBuff().buffMaxStackReached(Buff)) {
                Target.getBuff().addBuff(Buff);
                if (Buff.applyEffect(null, null) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

}
