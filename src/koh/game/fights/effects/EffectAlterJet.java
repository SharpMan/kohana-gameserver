package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffMaximiseEffects;
import koh.game.fights.effects.buff.BuffMinimizeEffects;
import static koh.protocol.client.enums.StatsEnum.MAXIMIZE_EFFECTS;

/**
 *
 * @author Neo-Craft
 */
public class EffectAlterJet extends EffectBase {

    @Override
    public int applyEffect(EffectCast CastInfos) {
        if (CastInfos.duration > 0) {
            for (Fighter Target : CastInfos.targets) {
                Target.getBuff().addBuff(CastInfos.effectType == MAXIMIZE_EFFECTS ? new BuffMaximiseEffects(CastInfos, Target) : new BuffMinimizeEffects(CastInfos, Target));
            }
        }

        return -1;
    }

}
