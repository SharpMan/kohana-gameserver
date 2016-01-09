package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.protocol.client.enums.FightStateEnum;

/**
 *
 * @author Neo-Craft
 */
public class EffectLostState extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter Target : castInfos.targets) {
            Target.getStates().removeState(FightStateEnum.valueOf(castInfos.effect.value));
        }

        return -1;
    }

}
