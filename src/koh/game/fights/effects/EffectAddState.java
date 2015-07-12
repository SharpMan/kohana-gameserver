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
            if (Target.States.CanState(FightStateEnum.valueOf(CastInfos.Effect.value)) && !Target.Buffs.BuffMaxStackReached(Buff)) {
                Target.Buffs.AddBuff(Buff);
                if (Buff.ApplyEffect(null, null) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

}
