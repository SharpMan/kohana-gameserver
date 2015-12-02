package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.protocol.client.enums.FightStateEnum;

/**
 *
 * @author Neo-Craft
 */
public class EffectLostState extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.Targets) {
            Target.states.removeState(FightStateEnum.valueOf(CastInfos.Effect.value));
        }

        return -1;
    }

}
