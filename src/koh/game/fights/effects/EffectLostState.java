package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.protocol.client.enums.FightStateEnum;

/**
 *
 * @author Neo-Craft
 */
public class EffectLostState extends EffectBase {

    @Override
    public int applyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.targets) {
            Target.getStates().removeState(FightStateEnum.valueOf(CastInfos.effect.value));
        }

        return -1;
    }

}
